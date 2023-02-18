package jp.sagalab.jftk.force.surface;

import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.transform.Transformable;

public interface FrictionSurface extends Transformable<FrictionSurface> {

    /**
     * この表面へ投影された点を返します。
     * @param _p 点
     * @return 投影された点
     */
    Point projection(Point _p);

    /**
     * この表面との距離を返します。
     * @param _p 点
     * @return 距離
     */
    double distance(Point _p);

    /**
     * この表面の法線ベクトルを返します。
     * @param _p 点
     * @return 法線ベクトル
     */
    Vector normal(Point _p);
}
