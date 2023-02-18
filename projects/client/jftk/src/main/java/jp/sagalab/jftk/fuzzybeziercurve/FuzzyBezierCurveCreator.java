package jp.sagalab.jftk.fuzzybeziercurve;

import jp.sagalab.jftk.Matrix;
import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.curve.BezierCurve;
import jp.sagalab.jftk.curve.ParametricEvaluable;
import jp.sagalab.jftk.curve.interporation.BezierCurveInterpolator;
import jp.sagalab.jftk.fuzzysplinecurve.FuzzySplineCurveCreater;

public class FuzzyBezierCurveCreator {

    public static FuzzyBezierCurveCreator create() {
        return new FuzzyBezierCurveCreator(
                3, 0.1,
                0.008581 * 6, 0.007742 * 0.25
        );
    }

    public static FuzzyBezierCurveCreator create(double _curveTimeLength) {
        return new FuzzyBezierCurveCreator(
                3, _curveTimeLength,
                0.008581 * 6, 0.007742 * 0.25
        );
    }

    public static FuzzyBezierCurveCreator create(int _degree, double _curveTimeLength, double _velocityCoefficient, double _accelerationCoefficient) {
        if (_degree < 0) {
            throw new IllegalArgumentException("invalid degree : " + _degree);
        }
        if (_curveTimeLength <= 0) {
            throw new IllegalArgumentException("invalid curve time length : " + _degree);
        }
        if (_velocityCoefficient < 0) {
            throw new IllegalArgumentException("invalid velocity coefficient : " + _degree);
        }
        if (_accelerationCoefficient < 0) {
            throw new IllegalArgumentException("invalid acceleration coefficient : " + _degree);
        }
        return new FuzzyBezierCurveCreator(_degree, _curveTimeLength, _velocityCoefficient, _accelerationCoefficient);
    }

    public BezierCurve createFBC(Point[] _points) {
        if (_points.length <= m_degree) {
            return null;
        }
        BezierCurve bezier = BezierCurveInterpolator.interpolate(_points, m_degree);
        return createFBC(bezier);
    }

    public BezierCurve createFBC(BezierCurve _bezier) {
        BezierCurve vb = _bezier.differentiate();
        BezierCurve ab = vb.differentiate();

        Point origin = Point.createXYZ(0, 0, 0);
        Point[] points = vb.evaluateAll(m_pointNum, ParametricEvaluable.EvaluationType.TIME);
        double[] fuzziness = new double[m_pointNum];
        for (int i = 0; i < points.length; ++i) {
            Point point = points[i];
            double v = origin.distance(vb.evaluateAt(point.time())) * 10;
            double a = origin.distance(ab.evaluateAt(point.time())) * 100;
            fuzziness[i] = m_velocityCoefficient * v + m_accelerationCoefficient * a;
        }

        double[] times = BezierCurveInterpolator.createNormalizedTimes(points);
        Matrix matrix = BezierCurveInterpolator.createWeightMatrix(times, m_degree);
        double[] elements = FuzzySplineCurveCreater.nnls(matrix, fuzziness);
        Point[] controlPoints = _bezier.controlPoints();
        for (int i = 0; i < controlPoints.length; ++i) {
            Point p = controlPoints[i];
            controlPoints[i] = Point.createXYZTF(p.x(), p.y(), p.z(), p.time(), elements[i]);
        }

        return BezierCurve.create(controlPoints, _bezier.range());
    }

    FuzzyBezierCurveCreator(int _degree, double _curveTimeLength, double _velocityCoefficient, double _accelerationCoefficient) {
        this.m_degree = _degree;
        this.m_pointNum = (int) Math.max(_curveTimeLength * 100, 4);
        this.m_velocityCoefficient = _velocityCoefficient;
        this.m_accelerationCoefficient = _accelerationCoefficient;
    }

    /** 生成するベジェ曲線の次数 */
    private final int m_degree;
    /** ファジネス生成に用いる点数 */
    private final int m_pointNum;
    /** ファジネス生成速度係数 */
    private final double m_velocityCoefficient;
    /** ファジネス生成加速度係数 */
    private final double m_accelerationCoefficient;

}
