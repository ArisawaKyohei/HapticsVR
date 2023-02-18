/*
 * DeviceEvent.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.deviceclient;

/**
 * デバイスの状態に変化があったことを示すイベント。
 */
public class DeviceEvent {
    /** デバイスイベントタイプ */
    public static enum DeviceEventType {
        /** ボタンプレス */
        BUTTON_PRESSED,
        /** ボタンリリース */
        BUTTON_RELEASED,
        /** デバイスムーブ */
        SENSOR_MOVED,
        /** デバイススウェイ */
        SENSOR_SWAYED
    }

    /** ボタンID */
    private final int buttonID;

    /** デバイス座標値 */
    private final double[] position;

    /** デバイス姿勢 */
    private final double[] posture;

    /** イベント発生時刻 */
    private final long time;

    /** イベントタイプ */
    private final DeviceEventType type;

    /**
     * ボタンID、デバイス座標値、デバイス姿勢、イベント発生時刻、イベントタイプを指定するコンストラクタ。
     * @param buttonID ボタンID。
     * @param position デバイス座標値。
     * @param posture デバイス姿勢。
     * @param time イベント発生時刻。
     * @param type イベントタイプ。
     */
    private DeviceEvent(int buttonID, double[] position, double[] posture, long time, DeviceEventType type) {
        this.buttonID = buttonID;
        this.position = position;
        this.posture = posture;
        this.time = time;
        this.type = type;
    }

    /**
     * ボタンプレスイベントの生成。
     * @param buttonID ボタンID。
     * @param time イベント発生時刻。
     * @return ボタンプレスイベント。
     */
    public static DeviceEvent createPressedEvent(int buttonID, long time) {
        return new DeviceEvent(buttonID, null, null, time, DeviceEventType.BUTTON_PRESSED);
    }

    /**
     * ボタンリリースイベントの生成。
     * @param buttonID ボタンID。
     * @param time イベント発生時刻。
     * @return ボタンリリースイベント。
     */
    public static DeviceEvent createReleasedEvent(int buttonID, long time) {
        return new DeviceEvent(buttonID, null, null, time, DeviceEventType.BUTTON_RELEASED);
    }

    /**
     * デバイスムーブイベントの生成。
     * @param position デバイス座標値。
     * @param time イベント発生時刻。
     * @return デバイスムーブイベント。
     */
    public static DeviceEvent createMovedEvent(double[] position, long time) {
        return new DeviceEvent(-1, position, null, time, DeviceEventType.SENSOR_MOVED);
    }

    /**
     * デバイススウェイイベントの生成。
     * @param posture デバイス姿勢。
     * @param time イベント発生時刻。
     * @return デバイススウェイイベント。
     */
    public static DeviceEvent createSwayedEvent(double[] posture, long time) {
        return new DeviceEvent(-1, null, posture, time, DeviceEventType.SENSOR_SWAYED);
    }

    /**
     * ボタンIDの取得。
     * @return ボタンID。
     */
    public int getButtonID() {
        return buttonID;
    }

    /**
     * デバイスのx座標値の取得。
     * @return デバイスのx座標値。
     */
    public double getX() {
        if (position != null) {
            return position[0];
        }
        return 0;
    }

    /**
     * デバイスのy座標値の取得。
     * @return デバイスのy座標値。
     */
    public double getY() {
        if (position != null) {
            return position[1];
        }
        return 0;
    }

    /**
     * デバイスのz座標値の取得。
     * @return デバイスのz座標値。
     */
    public double getZ() {
        if (position != null) {
            return position[2];
        }
        return 0;
    }

    /**
     * デバイスのx軸周りの回転量の取得。
     * @return デバイスのx軸周りの回転量。
     */
    public double getPX() {
        if (posture != null) {
            return posture[0];
        }
        return 0;
    }

    /**
     * デバイスのy軸周りの回転量の取得。
     * @return デバイスのy軸周りの回転量。
     */
    public double getPY() {
        if (posture != null) {
            return posture[1];
        }
        return 0;
    }

    /**
     * デバイスのz軸周りの回転量の取得。
     * @return デバイスのz軸周りの回転量。
     */
    public double getPZ() {
        if (posture != null) {
            return posture[2];
        }
        return 0;
    }

    /**
     * イベント発生時刻の取得。
     * @return イベント発生時刻。
     */
    public long getTime() {
        return time;
    }

    /**
     * イベントタイプの取得。
     * @return イベントタイプ。
     */
    public DeviceEventType getType() {
        return type;
    }
}
