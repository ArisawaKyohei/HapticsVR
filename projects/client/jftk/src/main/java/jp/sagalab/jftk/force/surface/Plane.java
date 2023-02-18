package jp.sagalab.jftk.force.surface;

import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.transform.TransformMatrix;

public class Plane implements FrictionSurface {

    public static Plane create(Point _p, Vector _normal) {
        if (_p == null) {
            throw new IllegalArgumentException("_p is null");
        }
        Vector normal = _normal.normalize();
        if (Double.isInfinite(1 / normal.length())) {
            throw new IllegalArgumentException("_normal length is zero.");
        }
        return new Plane(_p, normal);
    }

    /**
     * 平面を生成します。
     * @param _alpha 平面上の任意の点A
     * @param _beta  平面上の任意の点B
     * @param _gamma 平面上の任意の点C
     * @return 平面
     */
    public static Plane create(Point _alpha, Point _beta, Point _gamma) {
        return Plane.create(_alpha, Vector.createNormal(_alpha, _beta, _gamma));
    }

    /**
     * 基準点を返します。
     * @return 基準点
     */
    public Point base() {
        return m_base;
    }

    @Override
    public Point projection(Point _p) {
        return _p.move(m_normalVector.magnify(-distance(_p)));
    }

    @Override
    public double distance(Point _p) {
        Vector v = Vector.createSE(m_base, _p);
        return m_normalVector.dot(v);
    }

    @Override
    public Vector normal(Point _p) {
        return m_normalVector;
    }

    private Plane(Point _base, Vector _normal) {
        this.m_base = _base;
        this.m_normalVector = _normal;
    }

    /** 基準点 */
    private final Point m_base;
    /** 法線ベクトル */
    private final Vector m_normalVector;

    @Override
    public FrictionSurface transform(TransformMatrix _matrix) {
        return new Plane(m_base.transform(_matrix), m_normalVector.transform(_matrix.rotatalize()).normalize());
    }
}
