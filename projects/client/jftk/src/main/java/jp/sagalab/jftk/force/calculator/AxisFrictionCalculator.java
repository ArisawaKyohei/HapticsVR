package jp.sagalab.jftk.force.calculator;

import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.TruthValue;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.curve.BezierCurve;
import jp.sagalab.jftk.curve.ParametricEvaluable;
import jp.sagalab.jftk.force.FuzzyValue;
import jp.sagalab.jftk.fragmentation.FuzzyFragmentation;
import jp.sagalab.jftk.fuzzybeziercurve.FuzzyBezierCurveCreator;

import java.util.ArrayList;

import static jp.sagalab.jftk.fragmentation.FuzzyFragmentation.State.UNKNOWN;

/**
 * 軸別静止摩擦力の計算をします。
 */
public class AxisFrictionCalculator implements ForceCalculator {

    /**
     * 軸別静止摩擦力計算機を生成します。
     * 既定値はGeomagicの座標系を元に設定されています。
     * @return 計算機
     */
    public static AxisFrictionCalculator create() {
        return new AxisFrictionCalculator(
                /* 秒 */1e-3, /* 秒 */0.1,
                /* N/mm */ 0.5, /* N */0.5, 0.95,
                TruthValue.create(0.5, 0.5), 0.025,
                FuzzyBezierCurveCreator.create()
        );
    }

    /**
     * パラメータを指定して軸別静止摩擦力計算機を生成します
     * @param _inputInterval               入力点列のサンプリング間隔 (s)
     * @param _curveTimeLength             曲線の生成に用いる点列の時間長 (s)
     * @param _stiffness                   バネ定数 (N/[距離])
     * @param _maxFrictionForce            最大静止摩擦力 (N)
     * @param _maxFrictionThreshold        最大静止摩擦超過閾値
     * @param _fragmentationThreshold      フラグメンテーションの閾値
     * @param _fragmentationTimeResolution フラグメンテーションの時間解像度 (s)
     * @param _fbcCreator                  FBC生成器
     * @return 計算機
     */
    public static AxisFrictionCalculator create(
            double _inputInterval,
            double _curveTimeLength,
            double _stiffness,
            double _maxFrictionForce,
            double _maxFrictionThreshold,
            TruthValue _fragmentationThreshold,
            double _fragmentationTimeResolution,
            FuzzyBezierCurveCreator _fbcCreator
    ) {
        if (_inputInterval <= 0) {
            throw new IllegalArgumentException("invalid input interval : " + _inputInterval);
        }
        if (_curveTimeLength <= 0) {
            throw new IllegalArgumentException("invalid curve time length : " + _inputInterval);
        }
        if (_fbcCreator == null) {
            throw new IllegalArgumentException("fbc creator must not be null");
        }
        return new AxisFrictionCalculator(
                _inputInterval, _curveTimeLength, _stiffness,
                _maxFrictionForce, _maxFrictionThreshold,
                _fragmentationThreshold, _fragmentationTimeResolution,
                FuzzyBezierCurveCreator.create(_curveTimeLength)
        );
    }

    /**
     * ファジィベジェ曲線返す。
     * @return ファジィベジェ曲線
     */
    public BezierCurve getFbc() {
        return fbc;
    }

    /**
     * x軸方向の状態を返す。
     * @return 静止摩擦状態
     */
    public boolean getXFlag() {
        return flags[0];
    }

    /**
     * y軸方向の状態を返す。
     * @return 静止摩擦状態
     */
    public boolean getYFlag() {
        return flags[1];
    }

    /**
     * z軸方向の状態を返す。
     * @return 静止摩擦状態
     */
    public boolean getZFlag() {
        return flags[2];
    }

    @Override
    public Vector calculate(Point _point) {
        // 一定時間間隔でサンプリングする
        if (_point.time() - preTime > m_inputInterval) {
            points.add(_point);
            preTime = _point.time();
        }
        // 一定時間以前の点を削除する
        points.removeIf((p) -> p.time() < _point.time() - m_curveTimeLength);

        Point current = _point;
        fbc = m_fbcCreator.createFBC(points.toArray(new Point[0]));
        // fbcが生成された場合は停止性を計算
        if (fbc != null) {
            int num = (int) (m_curveTimeLength / m_fragmentationTimeResolution);
            Point[] points = fbc.evaluateAll(num, ParametricEvaluable.EvaluationType.TIME);
            // 各軸方向の値に分解
            FuzzyValue[] x = new FuzzyValue[points.length];
            FuzzyValue[] y = new FuzzyValue[points.length];
            FuzzyValue[] z = new FuzzyValue[points.length];
            for (int i = 0; i < points.length; ++i) {
                Point p = points[i];
                x[i] = FuzzyValue.create(p.x(), p.fuzziness());
                y[i] = FuzzyValue.create(p.y(), p.fuzziness());
                z[i] = FuzzyValue.create(p.z(), p.fuzziness());
            }

            // 各軸の停止性を計算
            labels[0] = fragmentation(x);
            labels[1] = fragmentation(y);
            labels[2] = fragmentation(z);
            current = points[points.length - 1];
        }

        if (anchor == null) {
            anchor = current;
        }

        Vector vector = Vector.createSE(current, anchor).magnify(m_stiffness);
        // 各軸の状態を更新して最終的な力を返す。
        double x = updateState(0, current, vector);
        double y = updateState(1, current, vector);
        double z = updateState(2, current, vector);

        return Vector.createXYZ(x, y, z);
    }

    /**
     * チャンクのラベルを返す
     * @param values ファジィ数
     * @return チャンクラベル
     */
    private FuzzyFragmentation.State fragmentation(FuzzyValue[] values) {
        double nec = 1.0;
        double pos = 1.0;
        FuzzyValue last = values[values.length - 1];
        for (int i = 0; i < values.length - 1; ++i) {
            TruthValue truthValue = last.includedIn(values[i]);
            nec = Math.min(nec, truthValue.necessity());
            pos = Math.min(pos, truthValue.possibility());
        }
        if (nec < m_fragmentationThreshold.necessity() && pos < m_fragmentationThreshold.possibility()) {
            return FuzzyFragmentation.State.MOVE;
        } else if (m_fragmentationThreshold.necessity() < nec && m_fragmentationThreshold.possibility() < pos) {
            return FuzzyFragmentation.State.STAY;
        } else {
            return UNKNOWN;
        }
    }

    /**
     * 各軸の状態を更新して最終的な力を返す。
     * @param index   軸 (0:x, 1:y, 2:z)
     * @param current 　現在のファジィポイント
     * @param vector  力
     * @return 軸の力
     */
    private double updateState(int index, Point current, Vector vector) {
        FuzzyValue value;
        if (index == 0 /* x */) {
            value = FuzzyValue.create(vector.x(), current.fuzziness() * m_stiffness);
        } else if (index == 1 /* y */) {
            value = FuzzyValue.create(vector.y(), current.fuzziness() * m_stiffness);
        } else if (index == 2 /* z */) {
            value = FuzzyValue.create(vector.z(), current.fuzziness() * m_stiffness);
        } else {
            return 0.0;
        }
        double nec = calcForceNecessity(value);
        double resultValue = value.getVertex() * (1 - nec);

        if (flags[index]) {
            if (nec >= m_maxFrictionThreshold && labels[index] != FuzzyFragmentation.State.STAY) {
                flags[index] = false;
                resultValue = 0.0;
            }
        } else {
            if (labels[index] == FuzzyFragmentation.State.STAY) {
                flags[index] = true;
                if (index == 0 /* x */) {
                    anchor = Point.createXYZ(current.x(), anchor.y(), anchor.z());
                } else if (index == 1 /* y */) {
                    anchor = Point.createXYZ(anchor.x(), current.y(), anchor.z());
                } else if (index == 2 /* z */) {
                    anchor = Point.createXYZ(anchor.x(), anchor.y(), current.z());
                }
            } else {
                resultValue = 0.0;
            }
        }
        return resultValue;
    }

    /**
     * ファジィ力が最大静止摩擦力を超えている可能性値の計算
     * @param ff ファジィ力
     * @return 可能性値
     */
    private double calcForceNecessity(FuzzyValue ff) {
        return Math.min(Math.max((Math.abs(ff.getVertex()) - m_maxFrictionForce) / ff.getFuzziness(), 0.0), 1.0);
    }

    private AxisFrictionCalculator(
            double _inputInterval,
            double _curveTimeLength,
            double _stiffness,
            double _maxFrictionForce,
            double _maxFrictionThreshold,
            TruthValue _fragmentationThreshold,
            double _fragmentationTimeResolution,
            FuzzyBezierCurveCreator _fbcCreator
    ) {
        this.m_inputInterval = _inputInterval;
        this.m_curveTimeLength = _curveTimeLength;
        this.m_stiffness = _stiffness;
        this.m_maxFrictionForce = _maxFrictionForce;
        this.m_maxFrictionThreshold = _maxFrictionThreshold;
        this.m_fragmentationThreshold = _fragmentationThreshold;
        this.m_fragmentationTimeResolution = _fragmentationTimeResolution;
        this.m_fbcCreator = _fbcCreator;
    }

    /** ファジィベジェ曲線 */
    private BezierCurve fbc = null;
    /** サンプリング点列 */
    private final ArrayList<Point> points = new ArrayList<>();
    /** 前回サンプリング時間 */
    private double preTime = 0.0;
    /** アンカーポイント */
    private Point anchor = null;
    /** 静止摩擦状態　[x, y, z] */
    private final boolean[] flags = new boolean[]{false, false, false};
    /** 停止性 [x, y, z] */
    private final FuzzyFragmentation.State[] labels = new FuzzyFragmentation.State[]{UNKNOWN, UNKNOWN, UNKNOWN};

    /** 入力点列のサンプリング間隔 (s) */
    private final double m_inputInterval;
    /** 曲線の生成に用いる点列の時間長 (s) */
    private final double m_curveTimeLength;
    /** バネ定数 (N/[距離]) */
    private final double m_stiffness;
    /** 最大静止摩擦力 (N) */
    private final double m_maxFrictionForce;
    /** 最大静止摩擦超過閾値 */
    private final double m_maxFrictionThreshold;
    /** FBC生成器 */
    private final FuzzyBezierCurveCreator m_fbcCreator;

    /** フラグメンテーションの閾値 */
    private final TruthValue m_fragmentationThreshold;
    /** フラグメンテーションの時間解像度 (s) */
    private final double m_fragmentationTimeResolution;

}
