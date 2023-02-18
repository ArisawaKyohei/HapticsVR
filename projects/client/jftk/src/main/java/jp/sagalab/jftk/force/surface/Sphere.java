package jp.sagalab.jftk.force.surface;

import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.transform.TransformMatrix;

public class Sphere implements FrictionSurface {

    public static Sphere create(Point _p, double _radius) {
        if (_p == null) {
            throw new IllegalArgumentException("_p is null");
        }
        if (_radius < 0) {
            throw new IllegalArgumentException("radius less than 0.");
        }
        return new Sphere(_p, _radius);
    }

    /**
     * 基準点を返します。
     * @return 基準点
     */
    public Point base() {
        return m_base;
    }

    /**
     * 半径を返します。
     * @return 半径
     */
    public double radius() {
        return m_radius;
    }

    @Override
    public Point projection(Point _p) {
        Vector vector = normal(_p).magnify(m_radius);
        return m_base.move(vector);
    }

    @Override
    public double distance(Point _p) {
        return Math.abs(m_base.distance(_p) - m_radius);
    }

    @Override
    public Vector normal(Point _p) {
        return Vector.createSE(m_base, _p).normalize();
    }

    private Sphere(Point _base, double _radius) {
        this.m_base = _base;
        this.m_radius = _radius;
    }

    /** 基準点 */
    private final Point m_base;
    /** 半径 */
    private final double m_radius;

    @Override
    public FrictionSurface transform(TransformMatrix _matrix) {
        return new Sphere(m_base.transform(_matrix), m_radius * _matrix.scalalize());
    }
}
