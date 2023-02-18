/*
 * ViewpointController.java
 * 仮想視点センサの座標値、姿勢を操作するDeviceListener実装
 * Oct. 2016 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.pbl2018;

import jp.ac.muroran_it.csse.deviceclient.DeviceEvent;
import jp.ac.muroran_it.csse.deviceclient.DeviceListener;

public class ViewpointController implements DeviceListener {
    /** 地震源ビューアのVR空間状態 */
    private final OpenDataVRSpaceState state;

    /**
     * 操作する地震源ビューアのVR空間状態を指定するコンストラクタ。
     * @param state 地震源ビューアのVR空間状態。
     */
    public ViewpointController(OpenDataVRSpaceState state) {
        this.state = state;
    }

    /**
     * デバイスプレスイベントへの対応。
     * 視点センサにはボタンがないため、呼ばることはない。
     * @param event デバイスプレスイベント。
     */
    @Override
    public void devicePressed(DeviceEvent event) {
    }

    /**
     * デバイスムーブイベントへの対応。
     * 視点センサの座標値を変更したときに呼ばれる。
     * @param event デバイスムーブイベント。
     */
    @Override
    public void deviceMoved(DeviceEvent event) {
        // 仮想視点センサの座標値を更新
        state.setViewpointPosition(event.getX(), event.getY(), event.getZ());
    }

    /**
     * デバイススウェイイベントへの対応。
     * 視点センサの姿勢を変更したときに呼ばれる。
     * @param event デバイススウェイイベント。
     */
    @Override
    public void deviceSwayed(DeviceEvent event) {
        // 仮想視点センサの姿勢を更新
        state.setViewpointPosture(event.getPX(), event.getPY(), event.getPZ());
    }

    /**
     * デバイスリリースイベントへの対応。
     * 視点センサにはボタンがないため、呼ばることはない。
     * @param event デバイスリリースイベント。
     */
    @Override
    public void deviceReleased(DeviceEvent event) {
    }
}
