package jp.sagalab.jftk.force.calculator;

import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.TruthValue;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.curve.BezierCurve;
import jp.sagalab.jftk.curve.ParametricEvaluable;
import jp.sagalab.jftk.force.FuzzyValue;
import jp.sagalab.jftk.force.surface.FrictionSurface;
import jp.sagalab.jftk.fragmentation.FuzzyFragmentation;
import jp.sagalab.jftk.fuzzybeziercurve.FuzzyBezierCurveCreator;

import java.util.ArrayList;
import java.util.HashMap;

import static jp.sagalab.jftk.fragmentation.FuzzyFragmentation.State.UNKNOWN;

public class SurfaceFrictionCalculator implements ForceCalculator {

    /**
     * 表面静止摩擦力計算機を生成します
     * 既定値はGeomagicの座標系を元に設定されています。
     * @return 計算機
     */
    public static SurfaceFrictionCalculator create() {
        return new SurfaceFrictionCalculator(
                /* 秒 */1e-3, /* 秒 */0.1,
                /* N/mm *//*0.5*/350, /* N */1.5, 0.95,
                TruthValue.create(0.5, 0.5), 0.025,
                FuzzyBezierCurveCreator.create()
        );
    }

    /**
     * パラメータを指定して表面静止摩擦力計算機を生成します
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
    public static SurfaceFrictionCalculator create(
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
        return new SurfaceFrictionCalculator(
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
     * 設定されている表面を返します。
     * @return 表面
     */
    public ArrayList<FrictionSurface> getSurfaces() {
        return new ArrayList<>(surfaces);
    }

    /**
     * 表面ごとの静止摩擦状態を返します。
     * @return 静止摩擦状態
     */
    public HashMap<FrictionSurface, Boolean> getFlags() {
        return flags;
    }

    /**
     * 表面を追加します。
     * @param _surface 表面
     */
    public void add(FrictionSurface _surface) {
        surfaces.add(_surface);
        flags.put(_surface, false);
        labels.put(_surface, UNKNOWN);
    }

    /**
     * 表面を追加します。
     * @param _surfaces 表面
     */
    public void addAll(FrictionSurface... _surfaces) {
        for (FrictionSurface surface : _surfaces) {
            add(surface);
        }
    }

    /**
     * 表面を削除します。
     * @param _surface 表面
     */
    public void remove(FrictionSurface _surface) {
        surfaces.remove(_surface);
        flags.remove(_surface);
        labels.remove(_surface);
    }

    /**
     * 表面を全て削除します。
     */
    public void removeAll() {
        surfaces.clear();
        flags.clear();
        labels.clear();
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
            current = points[points.length - 1];
            // 平面ごとに処理をする
            for (FrictionSurface surface : surfaces) {
                FuzzyValue[] distance = new FuzzyValue[points.length];
                for (int i = 0; i < points.length; ++i) {
                    Point p = points[i];
                    // 平面との距離を計算
                    distance[i] = FuzzyValue.create(surface.distance(p), p.fuzziness());
                }
                // 平面ごとの停止性を更新
                labels.put(surface, fragmentation(distance));
            }
        }
        return updateState(current);
    }

    /**
     * チャンクのラベルを返す
     * @param values ファジィ数
     * @return チャンクラベル
     */
    private FuzzyFragmentation.State fragmentation(FuzzyValue[] values) {
        FuzzyValue last = values[values.length - 1];
        double nec = Math.max((last.getFuzziness() - last.getVertex()) / last.getFuzziness(), 0.0);
        double pos = nec;
        // 必然性値が0の場合は平面上に無い
        if (nec != 0) {
            for (int i = 0; i < values.length - 1; ++i) {
                TruthValue truthValue = last.includedIn(values[i]);
                nec = Math.min(nec, truthValue.necessity());
                pos = Math.min(pos, truthValue.possibility());
            }
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
     * @param current 　現在のファジィポイント
     * @return 合成力
     */
    private Vector updateState(Point current) {
        Vector resultValue = Vector.createXYZ(0, 0, 0);
        // 平面ごとに計算
        for (FrictionSurface surface : surfaces) {
            // この平面のファジィ力
            Vector v = Vector.createSE(current, surface.projection(current));
            FuzzyValue value = FuzzyValue.create(v.length() * m_stiffness, current.fuzziness() * m_stiffness);
            double nec = calcForceNecessity(value);
            boolean flag = flags.get(surface);
            FuzzyFragmentation.State label = labels.get(surface);
            if (flag) {
                if (nec >= m_maxFrictionThreshold && label != FuzzyFragmentation.State.STAY) {
                    flags.put(surface, false);
                    System.out.println(m_maxFrictionForce + " " + m_stiffness);
                    // この平面の力は計算しない
                    continue;
                }
            } else {
                if (label == FuzzyFragmentation.State.STAY) {
                    flags.put(surface, true);
                } else {
                    // この平面の力は計算しない
                    continue;
                }
            }
            // この平面による力を合成
            resultValue = resultValue.compose(v.magnify(1 - nec));
        }
        return resultValue.magnify(m_stiffness);
    }

    /**
     * ファジィ力が最大静止摩擦力を超えている可能性値の計算
     * @param ff ファジィ力
     * @return 可能性値
     */
    private double calcForceNecessity(FuzzyValue ff) {
        return Math.min(Math.max((Math.abs(ff.getVertex()) - m_maxFrictionForce) / ff.getFuzziness(), 0.0), 1.0);
    }

    private SurfaceFrictionCalculator(
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

    /** 表面 */
    private final ArrayList<FrictionSurface> surfaces = new ArrayList<>();
    /** 表面ごとの静止摩擦状態 */
    private final HashMap<FrictionSurface, Boolean> flags = new HashMap<>();
    /** 表面ごとの停止性 */
    private final HashMap<FrictionSurface, FuzzyFragmentation.State> labels = new HashMap<>();

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
