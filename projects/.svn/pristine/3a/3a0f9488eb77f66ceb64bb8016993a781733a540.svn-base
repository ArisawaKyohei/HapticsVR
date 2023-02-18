/*
 * StylusController.java
 * 仮想スタイラスの座標値、姿勢を操作するDeviceListener実装
 * Oct. 2016 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.pbl2018;

import jp.ac.muroran_it.csse.deviceclient.DeviceEvent;
import jp.ac.muroran_it.csse.deviceclient.DeviceListener;

public class StylusController implements DeviceListener {
    /** 地震源ビューアのVR空間状態 */
    private final OpenDataVRSpaceState state;

    /**
     * 操作する地震源ビューアのVR空間状態を指定するコンストラクタ。
     * @param state 地震源ビューアのVR空間状態。
     */
    public StylusController(OpenDataVRSpaceState state) {
        this.state = state;
    }

    /**
     * デバイスプレスイベントへの対応。
     * スタイラスのボタンを押したときに呼ばれる。
     * @param event デバイスプレスイベント。
     */
    @Override
    public void devicePressed(DeviceEvent event) {
        // 仮想スタイラスのボタンを押下
        state.setStylusPressed(true);
    }

    /**
     * デバイスムーブイベントへの対応。
     * スタイラスの座標値を変更したときに呼ばれる。
     * @param event デバイスムーブイベント。
     */
    @Override
    public void deviceMoved(DeviceEvent event) {
        // 仮想スタイラスの座標値を更新
        state.setStylusPosition(event.getX(), event.getY(), event.getZ());
    }

    /**
     * デバイススウェイイベントへの対応。
     * スタイラスの姿勢を変更したときに呼ばれる。
     * @param event デバイススウェイイベント。
     */
    @Override
    public void deviceSwayed(DeviceEvent event) {
        // 仮想スタイラスの姿勢を更新
        state.setStylusPosture(event.getPX(), event.getPY(), event.getPZ());
    }

    /**
     * デバイスリリースイベントへの対応。
     * スタイラスのボタンを離したときに呼ばれる。
     * @param event デバイスリリースイベント。
     */
    @Override
    public void deviceReleased(DeviceEvent event) {
        // 仮想スタイラスのボタンを解放
        state.setStylusPressed(false);
    }
}
