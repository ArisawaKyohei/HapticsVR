package jp.sagalab.jftk.recognition;

import jp.sagalab.jftk.Plane;
import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Sigmoid;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.curve.*;
import jp.sagalab.jftk.curve.rough.CircularRoughCurve;
import jp.sagalab.jftk.curve.rough.EllipticRoughCurve;
import jp.sagalab.jftk.curve.rough.FreeRoughCurve;
import jp.sagalab.jftk.curve.rough.LinearRoughCurve;
import jp.sagalab.jftk.fragmentation.IdentificationFragment;

import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 楕円弧幾何曲線列化で使用する開曲線同定アルゴリズムの実装.
 * Fscを線分，円弧，楕円弧または開自由曲線のいづれかとして同定する．
 * 可能性値のみを利用する．
 * リファレンスモデルは始終点を必ず通る．
 * 重みを制限することで深い幾何曲線における形状の破綻を抑えることができる．
 * 上記の独自の推論ルールを持つためrecognizeの_ruleはnullでも良い．
 * リファレンスモデル生成メソッドはほぼコピペ．
 *
 * @author ito tomohiko
 */
public class OpenRecognizer implements Recognizable {

    private final int fmps;
    private final double minimumWeight;

    public OpenRecognizer() {
        this(30, -0.999);
    }

    public OpenRecognizer(int fmps, double minimumWeight) {
        this.fmps = fmps;
        this.minimumWeight = minimumWeight;
    }

    /**
     * 代表点列の探索を行います。
     *
     * @param _curve パラメトリック曲線
     * @return 代表点列
     */
    private static Point[] searchRepresentationPoints(ParametricCurve _curve) {
        // 直線性を用いて評価点列化
        Point[] evalPoints = _curve.evaluateAllByOptimized(99, 0.001);

        Point rp0 = evalPoints[0];
        Point rp2 = evalPoints[evalPoints.length - 1];

        Point[] slatePoints = new Point[evalPoints.length - 2];
        System.arraycopy(evalPoints, 1, slatePoints, 0, slatePoints.length);
        // 最遠点候補の取得
        Point rp1 = getBisectingPoint(slatePoints);

        return new Point[]{rp0, rp1, rp2};
    }

    /**
     * 入力点列から作られる面積を2等分する点を求めます。
     *
     * @param _points 入力点列
     * @return 面積を2等分する点
     */
    private static Point getBisectingPoint(Point[] _points) {
        Point center = _points[0].internalDivision(_points[_points.length - 1], 1, 1);
        // 中点と入力点列から生成される面積の配列
        double[] summationList = getSummationList(_points, center);
        double startSum;
        double endSum;
        int start = 0;
        int end = summationList.length - 1;
        int mid = (summationList.length) / 2;

        // 2分探索
        while (true) {
            startSum = 0;
            endSum = 0;
            for (int i = 0; i < mid; ++i) {
                startSum += summationList[i];
            }
            for (int i = mid; i < summationList.length; ++i) {
                endSum += summationList[i];
            }
            if (startSum > endSum) {
                end = mid;
                mid = start + (end - start) / 2;
            } else if (startSum < endSum) {
                start = mid;
                mid = start + (end - start) / 2;
            } else {
                break;
            }

            if ((end - start) <= 1 && startSum > endSum) {
                mid = start;
                break;
            } else if ((end - start) <= 1 && startSum < endSum) {
                mid = end;
                break;
            }
        }

        // 中点を基準とした両側の面積を計算
        double areaA = 0;
        for (int i = 0; i < mid; ++i) {
            areaA += summationList[i];
        }
        double areaB = 0;
        for (int i = mid; i < summationList.length; ++i) {
            areaB += summationList[i];
        }

        // 最遠点
        Point bisectingPoint;

        if (areaA > areaB) {
            // areaAの面積を更新
            areaA = 0;
            for (int i = 0; i < mid - 1; ++i) {
                areaA += summationList[i];
            }
            // 面積を2等分する点が含まれる微小平行四辺形の面積
            double sum = summationList[mid - 1];
            if (sum == 0) {
                // 微小平行四辺形が0ならばmidの点を返す
                return _points[mid];
            }
            // 比率の設定
            double ratioA = (areaB - areaA + sum) / (2 * sum);
            double ratioB = 1 - ratioA;
            bisectingPoint = _points[mid - 1].internalDivision(_points[mid], ratioA, ratioB);
        } else {
            // areaBの面積を更新
            areaB = 0;
            for (int i = mid + 1; i < summationList.length; ++i) {
                areaB += summationList[i];
            }
            // 面積を2等分する点が含まれる微小平行四辺形の面積
            double sum = summationList[mid];
            if (sum == 0) {
                // 微小平行四辺形が0ならばmidの点を返す
                return _points[mid];
            }
            // 比率の設定
            double ratioA = (areaB - areaA + sum) / (2 * sum);
            double ratioB = 1 - ratioA;
            bisectingPoint = _points[mid].internalDivision(_points[mid + 1], ratioA, ratioB);
        }
        return bisectingPoint;
    }

    /**
     * 微小な平行四辺形の面積のリストを返す
     *
     * @param _point  　点列
     * @param _center 候補点列の始終点を結んだ直線の中点
     * @return 微小な平行四辺形の配列
     */
    private static double[] getSummationList(Point[] _point, Point _center) {
        double[] result = new double[_point.length - 1];
        for (int i = 0; i < _point.length - 1; ++i) {
            result[i] = getParallelogramSummation(_center, _point[i], _point[i + 1]);
        }
        return result;
    }

    /**
     * 3点から外積により平行四辺形の面積を求める
     *
     * @param _base  基点(辺の始点)
     * @param _baseA 基点から辺Aの終点
     * @param _baseB 基点から辺Bの終点
     * @return 面積
     */
    private static double getParallelogramSummation(Point _base, Point _baseA, Point _baseB) {
        Vector vecA = Vector.createSE(_baseA, _base);
        Vector vecB = Vector.createSE(_baseB, _base);
        double result = vecA.cross(vecB).length();
        return result;
    }

    /**
     * 重みを導出します。
     *
     * @param _rp 代表点列
     * @return 重み
     */
    private static double[] calculateBestWeight(Point[] _rp, ParametricCurve _curve) {
        Point mid = _rp[0].internalDivision(_rp[2], 1, 1);

        double alphaPlusBeta = _rp[1].distance(mid);
        // 分割点の設定 分割点は、_curveの始点、代表点3点、_curveの終点とする
        Point[] breakPoints = new Point[]{
                _curve.evaluateAtStart(), _rp[0], _rp[1], _rp[2], _curve.evaluateAtEnd()
        };

        double[] weights = new double[breakPoints.length - 1];
        // alpha + beta == 0 なら計算するまでもない
        if (alphaPlusBeta > 0) {
            double d_2 = Math.pow(_rp[0].distance(mid), 2);

            Vector normal = Vector.createNormal(_rp[0], _rp[1], _rp[2]);
            Vector normal2 = Vector.createSE(_rp[0], _rp[2]).cross(normal);
            // 重み候補
            if (normal2.length() > 0) {
                for (int i = 0; i < weights.length; ++i) {
                    // 補助点とそこを通る平面を構築
                    Point[] aids = _curve.part(Range.create(breakPoints[i].time(), breakPoints[i + 1].time())).evaluateAll(20, ParametricEvaluable.EvaluationType.TIME);
                    Point aid = getBisectingPoint(aids);
                    Point t = Plane.create(aid, normal2).intersectWith(mid, _rp[1]);
                    if (t == null) {
                        continue;
                    }
                    double alpha = t.distance(_rp[1]);
                    double beta = alphaPlusBeta - alpha;
                    double c = t.distance(aid);

                    double numerator = 2 * alpha * beta * d_2;
                    double denominator = alphaPlusBeta * alphaPlusBeta * c * c - alpha * alpha * d_2;
                    // 重みの計算
                    double w = numerator / denominator - 1;

                    if (!Double.isNaN(w)) {
                        w = Math.min(Math.max(-0.999, w), 0.999);
                    }
                    weights[i] = w;
                }
            }
        }

        return new double[]{
                weights[1], weights[2], Math.max(weights[0], weights[3])
        };
    }

    @Override
    public RecognitionResult recognize(IdentificationFragment _identificationFragment, SplineCurve _fsc, Map<String, Sigmoid> _rule) {
        QuadraticBezierCurve linear = createLinearReference(_fsc);
        QuadraticBezierCurve circular = createCircularReference(_fsc);
        QuadraticBezierCurve elliptic = createEllipticReference(_fsc);

        double pL = linear.includedIn(_fsc, fmps).possibility();
        double pC = circular.includedIn(_fsc, fmps).possibility();
        double pE = elliptic.includedIn(_fsc, fmps).possibility();

        Map<PrimitiveType, Double> grades = new EnumMap<>(PrimitiveType.class);
        grades.put(PrimitiveType.LINE, pL);
        grades.put(PrimitiveType.CIRCULAR_ARC, Math.min(1 - pL, pC));
        grades.put(PrimitiveType.ELLIPTIC_ARC, Math.min(Math.min(1 - pL, 1 - pC), pE));
        grades.put(PrimitiveType.OPEN_FREE_CURVE, Math.min(Math.min(1 - pL, 1 - pC), 1 - pE));

        Map.Entry<PrimitiveType, Double> result = grades.entrySet().stream()
                .sorted(Map.Entry.<PrimitiveType, Double>comparingByValue().reversed())
                .collect(Collectors.toList())
                .get(0);

        RecognitionResult r;
        switch (result.getKey()) {
            case LINE:
                r = LinearRecognitionResult.create(
                        LinearRoughCurve.create(linear), PrimitiveType.LINE, grades);
                break;
            case CIRCULAR_ARC:
                r = CircularRecognitionResult.create(
                        CircularRoughCurve.create(circular, false, NQuartersType.GENERAL), PrimitiveType.CIRCULAR_ARC, grades);
                break;
            case ELLIPTIC_ARC:
                r = EllipticRecognitionResult.create(
                        EllipticRoughCurve.create(elliptic, false, NQuartersType.GENERAL), PrimitiveType.ELLIPTIC_ARC, grades);
                break;
            default:
                r = FreeCurveRecognitionResult.create(FreeRoughCurve.create(_fsc, false), PrimitiveType.OPEN_FREE_CURVE, grades);
                break;
        }
        return r;
    }

    private QuadraticBezierCurve createLinearReference(SplineCurve _fsc) {
        Point start = _fsc.evaluateAtStart();
        Point end = _fsc.evaluateAtEnd();
        Point mid = start.internalDivision(end, 1, 1);
        return QuadraticBezierCurve.create(start, mid, end, 0, Range.zeroToOne());
    }

    private QuadraticBezierCurve createCircularReference(SplineCurve _fsc) {
        Point p0 = _fsc.evaluateAtStart();
        Point p2 = _fsc.evaluateAtEnd();
        Point mid = p0.internalDivision(p2, 1, 1);
        Vector normal = Vector.createSE(p0, p2).normalize();
        // 中間点の初期値はrp0とrp2のパラメータ的に中間の点をセットしておく
        Point intersection = _fsc.evaluateAt((p0.time() + p2.time()) * 0.5);
        if (!Double.isInfinite(1 / normal.length())) {
            // _rp0と_rp2の垂直二等分面
            Plane plane = Plane.create(mid, normal);
            ParametricCurve part = _fsc.part(Range.create(p0.time(), p2.time()));
            // 部分区間内での交点群を導出
            Point[] intersections = part.intersectionWith(plane);
            // 交点は見つからなかったときは曲線が点に縮退しているときのはずなので、なんでも良いはず
            if (intersections.length > 0) {
                intersection = intersections[0];
            }
        }
        Point p1 = intersection;

        double L2 = Math.pow(mid.distance(p2), 2);
        double H2 = Math.pow(mid.distance(p1), 2);

        double weight = (L2 - H2) / (L2 + H2);
        if (Double.isNaN(weight)) {
            weight = 0;
        }

        return QuadraticBezierCurve.create(p0, p1, p2, Math.max(minimumWeight, weight), Range.zeroToOne());
    }

    private QuadraticBezierCurve createEllipticReference(SplineCurve _fsc) {
        Point[] rp = searchRepresentationPoints(_fsc);

        QuadraticBezierCurve elliptic = null;

        double[] weights = calculateBestWeight(rp, _fsc);
        double maxPos = Double.NEGATIVE_INFINITY;

        for (double w : weights) {
            if (Double.isNaN(w)) {
                continue;
            }
            // 可能性値が最も高くなるものを選ぶ
            QuadraticBezierCurve model = QuadraticBezierCurve.create(
                    rp[0], rp[1], rp[2], w, Range.zeroToOne());
            double pos = model.includedIn(_fsc, fmps).possibility();
            if (pos > maxPos) {
                elliptic = model;
                maxPos = pos;
            }
        }

        return elliptic;
    }
}
