/*
 * Position.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;

/**
 * 三次元空間上の座標値。
 */
public class Position {
    /** x座標値 */
    private final double x;

    /** y座標値 */
    private final double y;

    /** z座標値 */
    private final double z;

    /**
     * x座標値、y座標値、z座標値を指定するコンストラクタ。
     * @param x x座標値。
     * @param y y座標値。
     * @param z z座標値。
     */
    public Position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * x座標値の取得。
     * @return x座標値。
     */
    public double getX() {
        return x;
    }

    /**
     * y座標値の取得。
     * @return y座標値。
     */
    public double getY() {
        return y;
    }

    /**
     * z座標値の取得。
     * @return z座標値。
     */
    public double getZ() {
        return z;
    }

    /**
     * この座標値を指定した分だけ平行移動させた座標値の生成。
     * @param x x方向平行移動成分。
     * @param y y方向平行移動成分。
     * @param z z方向平行移動成分。
     * @return 平行移動させた座標値。
     */
    public Position translate(double x, double y, double z) {
        return new Position(this.x + x, this.y + y, this.z + z);
    }

    /**
     * この座標値を指定した角度だけx軸について回転させた座標値の生成。
     * @param angle 回転角（ラジアン）。
     * @return 回転させた座標値。
     */
    public Position rotateX(double angle) {
        double newY = y * Math.cos(angle) - z * Math.sin(angle);
        double newZ = y * Math.sin(angle) + z * Math.cos(angle);
        return new Position(x, newY, newZ);
    }

    /**
     * この座標値を指定した角度だけy軸について回転させた座標値の生成。
     * @param angle 回転角（ラジアン）。
     * @return 回転させた座標値。
     */
    public Position rotateY(double angle) {
        double newX = x * Math.cos(angle) + z * Math.sin(angle);
        double newZ = -x * Math.sin(angle) + z * Math.cos(angle);
        return new Position(newX, y, newZ);
    }

    /**
     * この座標値を指定した角度だけz軸について回転させた座標値の生成。
     * @param angle 回転角（ラジアン）。
     * @return 回転させた座標値。
     */
    public Position rotateZ(double angle) {
        double newX = x * Math.cos(angle) - y * Math.sin(angle);
        double newY = x * Math.sin(angle) + y * Math.cos(angle);
        return new Position(newX, newY, z);
    }

    /**
     * この座標値と指定した座標値との距離の算出。
     * @param _other 座標値。
     * @return 座標値の間の距離。
     */
    public double calculateDistance(Position _other) {
        return Math.sqrt(
          Math.pow(x - _other.getX(), 2.0)
          + Math.pow(y - _other.getY(), 2.0)
          + Math.pow(z - _other.getZ(), 2.0));
    }
}
