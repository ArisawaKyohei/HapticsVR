package jp.sagalab.haptics;

public interface EventListener {

    /**
     * ボタンプレスイベントを受信した時に呼ばれる
     * @param buttonId ボタンID
     * @param time タイムスタンプ (ns)
     */
    default void onPressed(int buttonId, long time) {

    }

    /**
     * ボタンリリースイベントを受信した時に呼ばれる
     * @param buttonId ボタンID
     * @param time タイムスタンプ (ns)
     */
    default void onReleased(int buttonId, long time) {

    }

    /**
     * 座標更新イベントを受信した時に呼ばれる
     * @param x x座標 (mm)
     * @param y y座標 (mm)
     * @param z z座標 (mm)
     * @param time タイムスタンプ (ns)
     */
    default void position(double x, double y, double z, long time) {

    }

    /**
     * 姿勢更新イベントを受信した時に呼ばれる
     * @param px x周りの角度 (degree)
     * @param py y周りの角度 (degree)
     * @param pz z周りの角度 (degree)
     * @param time タイムスタンプ (ns)
     */
    default void posture(double px, double py, double pz, long time) {

    }

    /**
     * 力更新イベントを受信した時に呼ばれる
     * @param fx x方向の力 (N)
     * @param fy y方向の力 (N)
     * @param fz z方向の力 (N)
     * @param time タイムスタンプ (ns)
     */
    default void force(double fx, double fy, double fz, long time) {

    }

    /**
     * 接続が切断された時に呼ばれる
     */
    default void disconnected() {

    }
}
