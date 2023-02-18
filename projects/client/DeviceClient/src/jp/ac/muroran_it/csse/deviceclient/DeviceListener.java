/*
 * DeviceListener.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.deviceclient;

/**
 * デバイスイベントを受け取るためのインタフェース。
 */
public interface DeviceListener {
    /**
     * デバイスプレスイベントへの対応。
     * デバイスのボタンを押したときに呼ばれる。
     * @param event デバイスイベント。
     */
    void devicePressed(DeviceEvent event);

    /**
     * デバイスムーブイベントへの対応。
     * デバイスの座標値を変更したときに呼ばれる。
     * @param event デバイスムーブイベント。
     */
    void deviceMoved(DeviceEvent event);

    /**
     * デバイススウェイイベントへの対応。
     * デバイスの姿勢を変更したときに呼ばれる。
     * @param event デバイススウェイイベント。
     */
    void deviceSwayed(DeviceEvent event);

    /**
     * デバイスリリースイベントへの対応。
     * デバイスのボタンを離したときに呼ばれる。
     * @param event デバイスリリースイベント。
     */
    void deviceReleased(DeviceEvent event);
}
