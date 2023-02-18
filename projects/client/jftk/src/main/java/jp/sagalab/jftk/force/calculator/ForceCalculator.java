package jp.sagalab.jftk.force.calculator;

import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Vector;

/**
 * 座標をもとに力を計算するインタフェースです。
 */
public interface ForceCalculator {

    /**
     * 力を計算する
     * @param _point 座標
     * @return 力
     */
    Vector calculate(Point _point);
}
