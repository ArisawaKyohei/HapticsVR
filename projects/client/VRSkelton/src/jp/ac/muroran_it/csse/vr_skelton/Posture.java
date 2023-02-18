/*
 * Posture.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;

/**
 * 各座標軸周りの回転量により表現される三次元空間上の姿勢。
 */
public class Posture {
    /** X軸周りの回転量 */
    private final double x;

    /** y軸周りの回転量 */
    private final double y;

    /** z軸周りの回転量 */
    private final double z;

    /**
     * x軸周りの回転量、y軸周りの回転量、z軸周りの回転量を指定するコンストラクタ。
     * @param x x軸周りの回転量。
     * @param y y軸周りの回転量。
     * @param z z軸周りの回転量。
     */
    public Posture(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * x軸周りの回転量の取得。
     * @return x軸周りの回転量。
     */
    public double getX() {
        return x;
    }

    /**
     * y軸周りの回転量の取得。
     * @return y軸周りの回転量。
     */
    public double getY() {
        return y;
    }

    /**
     * z軸周りの回転量の取得。
     * @return z軸周りの回転量。
     */
    public double getZ() {
        return z;
    }
}
