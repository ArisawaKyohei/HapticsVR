/*
 * GLFrame.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;

import java.awt.Cursor;
import java.awt.DisplayMode;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import jp.ac.muroran_it.csse.vr_skelton.Config.Mode;

/**
 * OpenGLのカンバスをもつウィンドウ。
 */
public class GLFrame extends Frame {
    /**
     * 描画カンバス
     */
    protected final GLCanvas canvas;

    /**
     * シーケンシャルステレオ表示の設定を指定するコンストラクタ。
     *
     * @param stereo シーケンシャルステレオ表示を有効にする場合はtrue。
     */
    protected GLFrame(boolean stereo) {
        super();

        // OpenGLの設定
        GLCapabilities glCaps = new GLCapabilities(GLProfile.getDefault());
        glCaps.setDoubleBuffered(true); // ダブルバッファを有効化
        glCaps.setStereo(stereo);       // シーケンシャルステレオ表示を有効化

        // 描画カンバスの生成
        canvas = new GLCanvas(glCaps);
        canvas.setFocusable(false);

        // ウィンドウにカンバスを追加
        add(canvas);

        // ウィンドウを閉じるときの後処理を設定
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * ウィンドウを生成して可視化する。 シーケンシャルステレオ表示を有効にする場合はフルスクリーン表示、無効にする場合はウィンドウ表示となる。
     * (2012年度までのVRアプリケーションで使用)
     *
     * @param stereo ステレオ表示を有効にする場合はtrue。
     * @return 生成して可視化されたウィンドウ。
     */
    @Deprecated
    public static GLFrame createAndShow(boolean stereo) {
        final GLFrame frame = new GLFrame(stereo);
        if (stereo) {
            // ディスプレイ環境の情報の取得
            GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice screenDevice = graphicsEnvironment.getDefaultScreenDevice();
            DisplayMode modifiedMode = new DisplayMode(800, 600, 32, 60);

            // 画面解像度の変更が可能であれば変更
            if (screenDevice.isDisplayChangeSupported()) {
                screenDevice.setDisplayMode(modifiedMode);
            }
            // フルスクリーン化して可視化
            frame.setUndecorated(true);
            screenDevice.setFullScreenWindow(frame);

            // カーソルの非表示
            Image dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(dummyImage, new Point(), "invisible");
            frame.setCursor(cursor);
        } else {
            // ウィンドウのサイズと位置の設定
            frame.setResizable(false);
            frame.setSize(800, 600);
            frame.setLocationRelativeTo(null);
            // ウィンドウの可視化
            frame.setVisible(true);
        }

        // 再描画スレッドの開始
        Thread redrawThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    frame.canvas.repaint();
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GLFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        redrawThread.start();
        return frame;
    }

    /**
     * ウィンドウを生成して可視化する。
     * STEREO_PROJECTORモード、Z800モード、YOUTUBE_SIDE_BY_SIDEモードではフルスクリーン表示、VIVEモード、WINDOW_800_600モードの場合はウィンドウ表示となる。
     *
     * @param displayMode ディスプレイ表示モード。
     * @return 生成して可視化されたウィンドウ。
     */
    public static GLFrame createAndShow(Mode displayMode) {
        final GLFrame frame = new GLFrame(displayMode.isStereo());
        // ディスプレイ環境の情報の取得

        switch (displayMode) {
            case STEREO_PROJECTOR:
            case Z800:
            case YOUTUBE_SIDE_BY_SIDE:{
                GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice screenDevice = graphicsEnvironment.getDefaultScreenDevice();
                DisplayMode modifiedMode = new DisplayMode(800, 600, 32, 60);
                
                // 画面解像度の変更が可能であれば変更
                if (screenDevice.isDisplayChangeSupported()) {
                    screenDevice.setDisplayMode(modifiedMode);
                }
                // フルスクリーン化して可視化
                frame.setUndecorated(true);
                screenDevice.setFullScreenWindow(frame);                
               
                // カーソルの非表示
                Image dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(dummyImage, new Point(), "invisible");
                frame.setCursor(cursor);

                break;
            }
            case VIVE:{
                GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
                GraphicsDevice screenDevice = graphicsEnvironment.getDefaultScreenDevice();
                Rectangle screenBounds = screenDevice.getDefaultConfiguration().getBounds();
                
                // タイトルバーの非表示
                frame.setUndecorated( true );
                
                // ウィンドウの位置・大きさを設定
                frame.setBounds( screenBounds.x, screenBounds.y, Mode.VIVE.getWidth(), Mode.VIVE.getHeight() );//VIVEディスプレイサイズ
                // カーソルの非表示
                Image dummyImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
                Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(dummyImage, new Point(), "invisible");
                frame.setCursor(cursor);

                break;
            }
            case WINDOW_800_600:{
                // ウィンドウのサイズと位置の設定
                frame.setResizable(false);
                frame.setSize(Mode.WINDOW_800_600.getWidth(), Mode.WINDOW_800_600.getHeight());
                frame.setLocationRelativeTo(null);

                break;
            }
            case WINDOW_1400_1050:{
                // ウィンドウのサイズと位置の設定
                frame.setResizable(false);
                frame.setSize(Mode.WINDOW_1400_1050.getWidth(), Mode.WINDOW_1400_1050.getHeight());
                frame.setLocationRelativeTo(null);

                break;
            }
            default:
                break;
        }

        // 再描画スレッドの開始
        Thread redrawThread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    frame.canvas.repaint();
                    System.out.println("###########");
                    try {
                        sleep(10);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(GLFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        };
        redrawThread.start();
        return frame;
    }

    /**
     * 描画カンバスにリスナーを追加する。
     *
     * @param listener 追加するリスナー。
     */
    public void addGLEventListener(GLEventListener listener) {
        canvas.addGLEventListener(listener);
    }
    
    
    

}
