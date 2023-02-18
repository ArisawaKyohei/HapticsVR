package jp.sagalab;

import jp.ac.muroran_it.csse.vr_skelton.Config;
import jp.ac.muroran_it.csse.vr_skelton.GLFrame;
import jp.ac.muroran_it.csse.vr_skelton.VRSpaceState;
import jp.sagalab.controller.HapticsStylusController;
import jp.sagalab.controller.KeyController;
import jp.sagalab.controller.ViewportController;
import jp.sagalab.haptics.HapticsClient;
import jp.sagalab.jftk.force.calculator.AxisFrictionCalculator;
import jp.sagalab.jftk.force.calculator.SurfaceFrictionCalculator;
import jp.sagalab.model.AppModel;
import jp.sagalab.view.GraphicsPanel;
import jp.sagalab.view.StateView;
import jp.sagalab.view.StateViewVR;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Main {
    // Hapticsサーバ名
    //private static final String hapticsServer = "127.0.0.1";
    private static final String hapticsServer = "localhost";
    // Hapticsポート番号
    private static final int hapticsPort = 9993;
    // Libertyサーバ名
    private static final String libertyServer = "wakana"; // LibertyServer
//    private static final String libertyServer = "localhost"; // DummyDaviceServer
    // Libertyポート番号
    private static final int libertyPort = 11113;
    // Libertyデバイス番号
    private static final int deviceNum = 1;

    public static void main(String[] args) {
        // Hapticsクライアントを設定
        HapticsClient hapticsClient = new HapticsClient();
        HapticsClient viewportClient = new HapticsClient();
        if (!hapticsClient.connect(hapticsServer, hapticsPort, 0)) {
            System.err.println("HapticsServerへの接続に失敗しました。");
            return;
        }
        if (!viewportClient.connect(libertyServer, libertyPort, deviceNum)) {
            System.err.println("HapticsServerへの接続に失敗しました。");
            return;
        }

        Config.Mode displayMode = Config.Mode.VIVE;
        //Config.Mode displayMode = Config.Mode.WINDOW_1400_1050;

        //Config.Mode displayMode = Config.Mode.WINDOW_2160_1200;
        // 軸別静止摩擦
//        AppModel model = new AppModel(AxisFrictionCalculator.create());
        // 表面静止摩擦
        AppModel model = new AppModel(SurfaceFrictionCalculator.create());

        final StateViewVR view = new StateViewVR(model,displayMode);

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final GLFrame frame = GLFrame.createAndShow(displayMode);

                // 各種コントローラーを生成
                HapticsStylusController hapticsStylusController = new HapticsStylusController(model, hapticsClient);
                ViewportController viewportController = new ViewportController(model, viewportClient);
                hapticsClient.addListener(hapticsStylusController);
                viewportClient.addListener(viewportController);
                KeyController keyController = new KeyController(model);
                frame.addKeyListener(keyController);


                frame.addGLEventListener(view);
                // ウィンドウの可視化
                frame.setVisible(true);
            }
        });
    }
}
