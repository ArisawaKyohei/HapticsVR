/*
 * ModelSpaceController.java
 * モデル空間を操作するDeviceListener実装
 * Oct. 2016 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.pbl2018;

import jp.ac.muroran_it.csse.deviceclient.DeviceEvent;
import jp.ac.muroran_it.csse.deviceclient.DeviceListener;
import jp.ac.muroran_it.csse.vr_skelton.Position;
import jp.ac.muroran_it.csse.vr_skelton.Posture;

public class ModelSpaceController implements DeviceListener {
    /** オープンデータビューアのVR空間状態 */
    private final OpenDataVRSpaceState state;

    /**
     * 操作するオープンデータビューアのVR空間状態を指定するコンストラクタ。
     * @param state VR空間状態。
     */
    public ModelSpaceController(OpenDataVRSpaceState state) {
        this.state = state;
    }

    /**
     * デバイスプレスイベントへの対応。
     * SpacePilotのボタンを押したときに呼ばれる。
     * @param event デバイスプレスイベント。
     */
    @Override
    public void devicePressed(DeviceEvent event) {
    }

    /**
     * デバイスムーブイベントへの対応。
     * SpacePilotの座標値を変更したときに呼ばれる。
     * @param event デバイスムーブイベント。
     */
    @Override
    public void deviceMoved(DeviceEvent event) {
        Position contentsPosition = state.getModelSpacePosition();

        // モデル空間のy座標値にデバイスのy座標値に比例した値を加算
        double x = contentsPosition.getX();
        double y = contentsPosition.getY() + event.getY() * 0.001;
        double z = contentsPosition.getZ();
        state.setModelSpacePosition(x, y, z);
    }

    /**
     * デバイススウェイイベントへの対応。
     * SpacePilotの姿勢を変更したときに呼ばれる。
     * @param event デバイススウェイイベント。
     */
    @Override
    public void deviceSwayed(DeviceEvent event) {
        Posture contentsPosture = state.getModelSpacePosture();

        // モデル空間のy軸周りの回転量からデバイスのy軸周りの回転量に比例した値を減算
        double x = contentsPosture.getX();
        double y = contentsPosture.getY() - event.getPY() * 0.005;
        double z = contentsPosture.getZ();
        state.setModelSpacePosture(x, y, z);
    }

    /**
     * デバイスリリースイベントへの対応。
     * SpacePilotのボタンを離したときに呼ばれる。
     * @param event デバイスリリースイベント。
     */
    @Override
    public void deviceReleased(DeviceEvent event) {
    }
}
