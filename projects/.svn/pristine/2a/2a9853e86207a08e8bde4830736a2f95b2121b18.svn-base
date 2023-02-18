/*
 * ViewpointController.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.simple_viewer;

import jp.ac.muroran_it.csse.deviceclient.DeviceEvent;
import jp.ac.muroran_it.csse.deviceclient.DeviceListener;
import jp.ac.muroran_it.csse.vr_skelton.VRSpaceState;

/**
 * 仮想視点センサの座標値、姿勢を操作するDeviceListener実装。
 */
public class ViewpointController implements DeviceListener {
    /** VR空間状態 */
    private final VRSpaceState state;

    /**
     * 操作するVR空間状態を指定するコンストラクタ。
     * @param state VR空間状態。
     */
    public ViewpointController(VRSpaceState state) {
        this.state = state;
    }

    /* ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ #7 ▼▼▼ */
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
    /* ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ #7 ▲▲▲ */
}
