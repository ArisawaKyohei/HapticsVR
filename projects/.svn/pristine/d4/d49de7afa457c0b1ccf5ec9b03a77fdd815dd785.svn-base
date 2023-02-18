/*
 * Main.java
 * メインクラス
 * Oct. 2016 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.pbl2018;

import java.io.File;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import javax.swing.SwingUtilities;
import jp.ac.muroran_it.csse.deviceclient.DeviceClient;
import jp.ac.muroran_it.csse.vr_skelton.Config;
import jp.ac.muroran_it.csse.vr_skelton.GLFrame;

/**
 * メイン関数を含んだ起動クラス。
 */
public class Main {
    /**
     * メイン関数。
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        // ステレオ表示の設定とDeviceClientの設定を読み込み
        File configFile = new File("/usr/local/pbl/device.cfg");
        Config config = null;
        if (configFile.exists()) {
            // 設定ファイルがある場合は設定ファイルから読み込み
            config = Config.loadFile(configFile);
        } else {
            // 設定ファイルがない場合はデフォルト設定を読み込み
            config = Config.loadDefault();
        }

        // VR空間状態とその表示を設定
        final Config.Mode displayMode = config.getdisplayMode();

        // オープンデータビューアのVR空間状態と表示を設定
        final OpenDataVRSpaceState state = new OpenDataVRSpaceState(Main.class.getName());
        final OpenDataVRSpaceView view = new OpenDataVRSpaceView(state, displayMode);

        // VR空間状態を初期化
        setupVRSpaceState(state);

        // デバイスを設定
        setupDevice(state, config);

        // ウィンドウを設定
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // ウィンドウを生成して可視化
                final GLFrame frame = GLFrame.createAndShow(displayMode);
                // キーリスナーの設定
                final KeyListener keyListener = new KeyListener() {
                    /**
                     * キープレスイベントへの対応。
                     * キーボードのキーが押されたときに呼ばれる。
                     * @param e キーイベント。
                     */
                    @Override
                    public void keyPressed(KeyEvent e) {
                        switch (e.getKeyCode()) {
                            case KeyEvent.VK_ESCAPE:
                                // ESCキーでウィンドウを閉じる
                                frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
                                break;
                            case KeyEvent.VK_SPACE:
                                // SPACEキーで左右の目の映像を入れ替え
                                view.swapEyes();
                                break;
                        }
                    }

                    /**
                     * キーリリースイベントへの対応。
                     * キーボードのキーが離されたときに呼ばれる。
                     * @param e キーイベント。
                     */
                    @Override
                    public void keyReleased(KeyEvent e) {
                    }

                    /**
                     * キータイプイベントへの対応。
                     * キーボードのキーが入力されたときに呼ばれる。
                     * @param e キーイベント。
                     */
                    @Override
                    public void keyTyped(KeyEvent e) {
                    }
                };
                // ウィンドウにキーリスナーを設定
                frame.addKeyListener(keyListener);
                // ウィンドウにGLイベントリスナーを設定
                frame.addGLEventListener(view);
            }
        });
    }

    /**
     * VR空間状態(State)の初期設定。
     * @param state VR空間状態。
     */
    private static void setupVRSpaceState(OpenDataVRSpaceState state) {
        // 仮想スタイラスの初期位置を設定
        state.setStylusPosition(0.0, 25.0, -15.0);
        state.setStylusPosture(-140.0, 30.0, 0.0);
        // 仮想視点センサの初期位置を設定
        state.setViewpointPosition(0.0, 55.0, 20.0);
        state.setViewpointPosture(90.0, 20.0, 180.0);
    }

    /**
     * デバイスの設定。
     * @param state VR空間状態。
     * @param config 設定。
     */
    private static void setupDevice(OpenDataVRSpaceState state, Config config) {
        // 仮想視点センサ操作デバイスを設定
        ViewpointController viewpointController = new ViewpointController(state);
        DeviceClient headMountDisplay = new DeviceClient();
        headMountDisplay.connect(config.getLibertyIP(), config.getLibertyPort(), config.getViewpointSensor());
        headMountDisplay.addDeviceListener(viewpointController);

        // 仮想スタイラス制御デバイスを設定
        StylusController stylusController = new StylusController(state);
        DeviceClient stylus = new DeviceClient();
        stylus.connect(config.getLibertyIP(), config.getLibertyPort(), config.getStylus());
        stylus.addDeviceListener(stylusController);

        // モデル空間操作デバイスを設定
        ModelSpaceController modelSpaceController = new ModelSpaceController(state);
        DeviceClient spacePilot = new DeviceClient();
        spacePilot.connect(config.getSpacePilotIP(), config.getSpacePilotPort(), 0);
        spacePilot.addDeviceListener(modelSpaceController);
    }
}
