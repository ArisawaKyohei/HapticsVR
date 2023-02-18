/*
 * VRSpaceView.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;

import java.awt.Color;
import java.awt.Font;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.awt.TextRenderer;
import jp.ac.muroran_it.csse.vr_skelton.Config.Mode;

/**
 * VR空間状態に基づいた描画を可能にするGLEventListenerの実装。
 */
public class VRSpaceView implements GLEventListener {
    /**
     * VR空間状態
     */
    private final VRSpaceState state;

    /**
     * テキストレンダラ
     */
    private final TextRenderer renderer;

    /**
     * シーケンシャルステレオ表示を有効にする場合はtrue
     */
    private boolean stereo;

    /**
     * 目の間隔（cm）
     */
    private double eyeSpan = 6.0;

    /**
     * 輻輳角（度）
     */
    private final double angleOfVergence = -Math.toDegrees(Math.atan2(eyeSpan * 0.5, 50.0));

    /**
     * 左右の目の映像を入れ替える場合はtrue
     */
    private boolean swapEyes = false;

    /**
     * ディスプレイ表示モード
     */
    private Mode displayMode;

    /**
     * Greenを基準としたバレルディストーションの歪み係数
     */
    double alpha = 0.3;
    
    /**
     * バレルディストーションによる色収差変換用の係数
     */
    double offset;
    
    /**
     * テクスチャバッファ
     */
    IntBuffer m_texture = IntBuffer.wrap(new int[2]);

    /**
     * フレームバッファ
     */
    IntBuffer m_framebuffer = IntBuffer.wrap(new int[2]);

    /**
     * デプスバッファ
     */
    IntBuffer depthbuffer = IntBuffer.wrap(new int[2]);

    /**
     * ディスプレイリスト
     */
    private int listID = 0;

    /**
     * VR空間状態とシーケンシャルステレオ表示の設定を指定するコンストラクタ。 （2012年度までのVRアプリケーションで使用）
     *
     * @param state VR空間状態。
     * @param stereo シーケンシャルステレオ表示を有効にする場合はtrue。
     */
    @Deprecated
    protected VRSpaceView(VRSpaceState state, boolean stereo) {
        this.state = state;

        // テキストレンダラの生成
        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 24));

        // シーケンシャルステレオ表示の設定
        this.stereo = stereo;

        if (this.stereo) {
            this.displayMode = Mode.Z800;
        } else {
            this.displayMode = Mode.WINDOW_800_600;
        }
    }

    /**
     * VR空間状態とディスプレイ表示モードの設定を指定するコンストラクタ。
     *
     * @param state VR空間の状態。
     * @param displayMode ディスプレイ表示モード STEREO_PROJECTOR or Z800 or
     * YOUTUBE_SIDE_BY_SIDE or VIVE or WINDOW_800_600
     */
    protected VRSpaceView(VRSpaceState state, Mode displayMode) {
        this.state = state;

        // テキストレンダラの生成
        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 24));

        // シーケンシャルステレオ表示の設定
        this.stereo = displayMode.isStereo();

        // ディスプレイ表示モードの設定
        this.displayMode = displayMode;

    }

    /**
     * OpenGLの初期設定。 GLオブジェクトが初期化されるときに呼ばれる。
     *
     * @param drawable このイベントの発信元。
     */
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        // 初期化色の設定
        gl.glClearColor(0f, 0f, 0f, 1f);

        //左目の初期化
        gl.glGenTextures(2, m_texture);
        setTextureParams(gl, m_texture.get(0));

        gl.glGenRenderbuffers(2, depthbuffer);
        setRenderBuffer(gl, depthbuffer.get(0));

        gl.glGenFramebuffers(2, m_framebuffer);
        setFrameBuffer(gl, m_framebuffer.get(0), m_texture.get(0), depthbuffer.get(0));

        //右目の初期化   
        setTextureParams(gl, m_texture.get(1));
        setRenderBuffer(gl, depthbuffer.get(1));
        setFrameBuffer(gl, m_framebuffer.get(1), m_texture.get(1), depthbuffer.get(1));

        // 両目の映像を描画するポリゴンをディスプレイリストに登録
        listID = gl.glGenLists(1);
        compile(gl);

        // 深度テストの有効化
        gl.glEnable(GL.GL_DEPTH_TEST);
        // 光源を有効化
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);

        // 初期化深度の設定
        gl.glClearDepth(32000);

        // アルファブレンディングを有効化
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    /**
     * ウィンドウサイズの更新。 ウィンドウのサイズや位置が変更されたときに呼ばれる。
     *
     * @param drawable このイベントの発信元。
     * @param x ウィンドウの位置のx座標値。
     * @param y ウィンドウの位置のy座標値。
     * @param width ウィンドウの幅。
     * @param height ウィンドウの高さ。
     */
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        setTextureParams(gl, m_texture.get(0)); // 左
        setTextureParams(gl, m_texture.get(1)); // 右
        setRenderBuffer(gl, depthbuffer.get(0)); // 左
        setRenderBuffer(gl, depthbuffer.get(1)); // 右
        setFrameBuffer(gl, m_framebuffer.get(0), m_texture.get(0), depthbuffer.get(0)); // 左
        setFrameBuffer(gl, m_framebuffer.get(1), m_texture.get(1), depthbuffer.get(1)); // 右
    }

    /**
     * ディスプレイ設定変更への対応。 ディスプレイ表示モードや表示するディスプレイが変更されたときに呼ばれる。
     *
     * @param drawable このイベントの発信元。
     * @param modeChanged ディスプレイ表示モードが変更された場合はtrue。
     * @param deviceChanged 表示するディスプレイが変更された場合はtrue。
     */

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    /**
     * 描画。 ウィンドウが再描画されるときに呼ばれる。
     *
     * @param drawable このイベントの発信元。
     */
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();

        switch (displayMode) {
            case STEREO_PROJECTOR:
                if (swapEyes) {
                    // 右バッファに左目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
                    setLeftViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.STEREO_PROJECTOR.getWidth() / Mode.STEREO_PROJECTOR.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawLeftLabel(gl, Mode.STEREO_PROJECTOR.getWidth(), Mode.STEREO_PROJECTOR.getHeight());
                    // 左バッファに右目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_LEFT);
                    setRightViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.STEREO_PROJECTOR.getWidth() / Mode.STEREO_PROJECTOR.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawRightLabel(gl, Mode.STEREO_PROJECTOR.getWidth(), Mode.STEREO_PROJECTOR.getHeight());
                } else {
                    // 左バッファに左目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_LEFT);
                    setLeftViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.STEREO_PROJECTOR.getWidth() / Mode.STEREO_PROJECTOR.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawLeftLabel(gl, Mode.STEREO_PROJECTOR.getWidth(), Mode.STEREO_PROJECTOR.getHeight());
                    // 右バッファに右目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
                    setRightViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.STEREO_PROJECTOR.getWidth() / Mode.STEREO_PROJECTOR.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawRightLabel(gl, Mode.STEREO_PROJECTOR.getWidth(), Mode.STEREO_PROJECTOR.getHeight());
                }
                break;
            case Z800:
                if (swapEyes) {
                    // 右バッファに左目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
                    setLeftViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.Z800.getWidth() / Mode.Z800.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawLeftLabel(gl, Mode.Z800.getWidth(), Mode.Z800.getHeight());
                    // 左バッファに右目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_LEFT);
                    setRightViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.Z800.getWidth() / Mode.Z800.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawRightLabel(gl, Mode.Z800.getWidth(), Mode.Z800.getHeight());
                } else {
                    // 左バッファに左目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_LEFT);
                    setLeftViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.Z800.getWidth() / Mode.Z800.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawLeftLabel(gl, Mode.Z800.getWidth(), Mode.Z800.getHeight());
                    // 右バッファに右目の描画
                    gl.glDrawBuffer(GL2.GL_BACK_RIGHT);
                    setRightViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.Z800.getWidth() / Mode.Z800.getHeight());
                    drawScene(gl);
                    draw2DTextSpaceObjects(gl);
                    drawRightLabel(gl, Mode.Z800.getWidth(), Mode.Z800.getHeight());
                }
                break;
            case YOUTUBE_SIDE_BY_SIDE:
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                gl.glEnable(GL.GL_DEPTH_TEST);

                // 描画バッファの指定
                gl.glDrawBuffer(GL.GL_BACK);
                // 左バッファに左目の描画
                gl.glViewport(0, 0, Mode.YOUTUBE_SIDE_BY_SIDE.getWidth() / 2, Mode.YOUTUBE_SIDE_BY_SIDE.getHeight());
                setLeftViewpoint(gl, 3.0, 150.0, 32.0, (double) (Mode.YOUTUBE_SIDE_BY_SIDE.getWidth() / 2) / Mode.YOUTUBE_SIDE_BY_SIDE.getHeight());
                drawScene(gl);
                draw2DTextSpaceObjects(gl);
                drawLeftLabel(gl, Mode.YOUTUBE_SIDE_BY_SIDE.getWidth(), Mode.YOUTUBE_SIDE_BY_SIDE.getHeight());
                // 右バッファに右目の描画
                gl.glViewport(Mode.YOUTUBE_SIDE_BY_SIDE.getWidth() / 2, 0, Mode.YOUTUBE_SIDE_BY_SIDE.getWidth() / 2, Mode.YOUTUBE_SIDE_BY_SIDE.getHeight());
                setRightViewpoint(gl, 3.0, 150.0, 32.0, (double) (Mode.YOUTUBE_SIDE_BY_SIDE.getWidth() / 2) / Mode.YOUTUBE_SIDE_BY_SIDE.getHeight());
                drawScene(gl);
                draw2DTextSpaceObjects(gl);
                drawRightLabel(gl, Mode.YOUTUBE_SIDE_BY_SIDE.getWidth(), Mode.YOUTUBE_SIDE_BY_SIDE.getHeight());
                gl.glDisable(GL.GL_DEPTH_TEST);
                break;
            case VIVE:
                //左表示
                gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, m_framebuffer.get(0));
                //光源の有効
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glEnable(GL2.GL_LIGHT0);
                gl.glEnable(GL.GL_DEPTH_TEST);
                gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

                gl.glViewport(0, 0, (int)((Mode.VIVE.getWidth() / 2)/(1-alpha)), (int)((Mode.VIVE.getWidth() / 2)/(1-alpha)));
                
                setLeftViewpoint(gl, 0.1, 1000.0, 70, 1);

                drawScene(gl);
                gl.glDisable(GL.GL_DEPTH_TEST);

                //右表示
                gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, m_framebuffer.get(1));
                gl.glEnable(GL.GL_DEPTH_TEST);
                gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

                gl.glViewport(0, 0, (int)((Mode.VIVE.getWidth() / 2)/(1-alpha)), (int)((Mode.VIVE.getWidth() / 2)/(1-alpha)));
                 
                setRightViewpoint(gl, 0.1, 1000.0, 70, 1);

                drawScene(gl);
                draw2DTextSpaceObjects(gl);
                gl.glDisable(GL.GL_DEPTH_TEST);

                //光源の無効
                gl.glDisable(GL2.GL_LIGHT0);
                gl.glDisable(GL2.GL_LIGHTING);

                //ポリゴン描画
                gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
                gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                gl.glViewport(0, 0, Mode.VIVE.getWidth(), Mode.VIVE.getHeight());             
                setCenterViewporigon(gl);
                gl.glEnable(GL.GL_TEXTURE_2D);
                gl.glCallList(listID);

                gl.glDisable(GL.GL_TEXTURE_2D);

                gl.glFlush();

                break;
            case WINDOW_800_600:
                // 描画バッファの指定
                gl.glDrawBuffer(GL.GL_BACK);
                // 単眼カメラの設定
                setCenterViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.WINDOW_800_600.getWidth() / Mode.WINDOW_800_600.getHeight());
                // シーンの描画
                drawScene(gl);
                draw2DTextSpaceObjects(gl);
                break;
            case WINDOW_1400_1050:
                // 描画バッファの指定
                gl.glDrawBuffer(GL.GL_BACK);
                // 単眼カメラの設定
                setCenterViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.WINDOW_1400_1050.getWidth() / Mode.WINDOW_1400_1050.getHeight());
                // シーンの描画
                drawScene(gl);
                draw2DTextSpaceObjects(gl);
                break;
            case WINDOW_2160_1200:
                // 描画バッファの指定
                gl.glDrawBuffer(GL.GL_BACK);
                // 単眼カメラの設定
                setCenterViewpoint(gl, 3.0, 150.0, 32.0, (double) Mode.WINDOW_2160_1200.getWidth() / Mode.WINDOW_2160_1200.getHeight());
                // シーンの描画
                drawScene(gl);
                draw2DTextSpaceObjects(gl);
                break;
            default:
                break;
        }
    }

    /**
     * 単眼表示用視野の設定。
     *
     * @param gl1 GLオブジェクト。
     * @param near　ニアクリップ距離
     * @param far　ファークリップ距離
     * @param fov　水平視野角
     * @param aspectRatio アスペクト比
     */
    protected void setCenterViewpoint(GL gl1, double near, double far, double fov, double aspectRatio) {
        GL2 gl = gl1.getGL2();
        // 画角などの設定
        gl.glMatrixMode(GL2.GL_PROJECTION); // 投影行列設定モードへ移行
        gl.glLoadIdentity();               // 投影行列の初期化
        setPerspective(gl, near, far, fov, aspectRatio);                // 投影行列の設定

        // 視点の座標値と姿勢の設定
        gl.glMatrixMode(GL2.GL_MODELVIEW); // モデルビュー行列設定モードへ移行
        gl.glLoadIdentity();              // モデルビュー行列の初期化
        setViewpoint(gl);                 // 視点の座標値と姿勢の設定
    }

    /**
     * 左目表示用視野の設定。
     *
     * @param gl1 GLオブジェクト。
     * @param near　ニアクリップ距離
     * @param far　ファークリップ距離
     * @param fov　水平視野角
     * @param aspectRatio アスペクト比
     */
    protected void setLeftViewpoint(GL gl1, double near, double far, double fov, double aspectRatio) {
        GL2 gl = gl1.getGL2();
        // 画角などの設定
        gl.glMatrixMode(GL2.GL_PROJECTION); // 投影行列設定モードへ移行
        gl.glLoadIdentity();               // 投影行列の初期化
        setPerspective(gl, near, far, fov, aspectRatio);                // 投影行列の設定

        // 視点の座標値と姿勢の設定
        gl.glMatrixMode(GL2.GL_MODELVIEW);                   // モデルビュー行列設定モードへ移行
        gl.glLoadIdentity();                                // モデルビュー行列の初期化
        gl.glRotated(angleOfVergence * 0.5, 0.0, 1.0, 0.0); // 視線を内向きに回転
        gl.glTranslated(eyeSpan * 0.5, 0.0, 0.0);           // 視点を左目の座標値に移動
        setViewpoint(gl);                                   // 視点の座標値と姿勢の設定
    }

    /**
     * 右目表示用視野の設定。
     *
     * @param gl1 GLオブジェクト。
     * @param near　ニアクリップ距離
     * @param far　ファークリップ距離
     * @param fov　水平視野角
     * @param aspectRatio アスペクト比
     */
    protected void setRightViewpoint(GL gl1, double near, double far, double fov, double aspectRatio) {
        GL2 gl = gl1.getGL2();
        // 画角などの設定
        gl.glMatrixMode(GL2.GL_PROJECTION); // 投影行列設定モードへ移行
        gl.glLoadIdentity();               // 投影行列の初期化
        setPerspective(gl, near, far, fov, aspectRatio);                // 投影行列の設定

        // 視点の座標値位置と姿勢の設定
        gl.glMatrixMode(GL2.GL_MODELVIEW);                    // モデルビュー行列設定モードへ移行
        gl.glLoadIdentity();                                 // モデルビュー行列の初期化
        gl.glRotated(-angleOfVergence * 0.5, 0.0, 1.0, 0.0); // 視線を内向きに回転
        gl.glTranslated(-eyeSpan * 0.5, 0.0, 0.0);           // 視点を右目の座標値に移動
        setViewpoint(gl);                                    // 視点の座標値と姿勢の設定
    }

    /**
     * ポリゴン描画用視野の設定。
     *
     * @param gl1 GLオブジェクト。
     */
    protected void setCenterViewporigon(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 画角などの設定
        gl.glMatrixMode(GL2.GL_PROJECTION); // 投影行列設定モードへ移行
        gl.glLoadIdentity();               // 投影行列の初期化
        setOrtho();                // 投影行列の設定

        // 視点の座標値と姿勢の設定
        gl.glMatrixMode(GL2.GL_MODELVIEW); // モデルビュー行列設定モードへ移行
        gl.glLoadIdentity();              // モデルビュー行列の初期化
        setViewpoint(gl);// 視点の座標値と姿勢の設定
    }

    /**
     * 投影行列の設定。
     *
     * @param gl GLオブジェクト。
     * @param near　ニアクリップ距離
     * @param far　ファークリップ距離
     * @param fov　水平視野角
     * @param aspectRatio アスペクト比
     */
    protected void setPerspective(GL gl, double near, double far, double fov, double aspectRatio) {
        new GLU().gluPerspective(fov / aspectRatio, aspectRatio, near, far);
    }

    /**
     * 平行投影用の投影行列を設定します。
     */
    protected void setOrtho() {
        // パラメータ設定
        //左右に並べた2枚の規格化プレート（-2 <= x <= +2 , -1 <= y <= +1）
        //Mode.VIVE.getWidth() * Mode.VIVE.getHeight() の大きさの画面で丁度捉えられるように
        //上下は少し余る（viveのディスプレイが2:1ではないので）
        double ratio =(double) (Mode.VIVE.getWidth() / 2) / Mode.VIVE.getHeight();

        double right = 2;  // 右上
        double left = -2; // 左下
        double top = 1 / ratio;  // 右上
        double bottom = -1 / ratio;//左下                

        // 投影行列を調整
        new GLU().gluOrtho2D(left, right, bottom, top);                
    }

    /**
     * 視点の座標値と姿勢の設定。
     *
     * @param gl1 GLオブジェクト。
     */
    protected void setViewpoint(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 仮想視点センサの座標値と姿勢を取得
        Position viewpointPosition = state.getViewpointPosition();
        Posture viewpointPosture = state.getViewpointPosture();

        // 仮想空間の座標系を仮想視点センサの座標系に一致するように設定
        gl.glRotated(-90, 0.0, 0.0, 1.0);
        gl.glRotated(180, 0.0, 1.0, 0.0);

        // 視点を仮想視点センサの座標値から左右の目の間の座標値まで平行移動
        gl.glTranslated(13.0, 0.0, 0.0);

        // 視点を仮想視点センサの座標値と姿勢に一致するように設定
        gl.glRotated(-viewpointPosture.getZ(), 1.0, 0.0, 0.0);
        gl.glRotated(-viewpointPosture.getY(), 0.0, 1.0, 0.0);
        gl.glRotated(-viewpointPosture.getX(), 0.0, 0.0, 1.0);
        gl.glTranslated(-viewpointPosition.getX(), -viewpointPosition.getY(), -viewpointPosition.getZ());
    }

    /**
     * シーンの描画。
     *
     * @param gl GLオブジェクト。
     */
    protected void drawScene(GL gl) {
        switch (displayMode) {
            case STEREO_PROJECTOR:
            case Z800:
            case WINDOW_800_600:
            case WINDOW_1400_1050:
            case WINDOW_2160_1200:
                // 描画バッファのクリア
                gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
                break;
            case YOUTUBE_SIDE_BY_SIDE:
            case VIVE:
                // 描画バッファのクリアをしない
                break;
            default:
                break;
        }

        // VR空間の描画
        drawVRSpace(gl);

        // モデル空間の描画
        drawModelSpace(gl);
    }

    /**
     * VR空間の描画。
     *
     * @param gl1 GLオブジェクト。
     */
    protected void drawVRSpace(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 現在のモデルビュー行列を保存
        gl.glPushMatrix();

        // VR空間のオブジェクト群の描画
        drawVRSpaceObjects(gl);

        // 保存したモデルビュー行列を復帰
        gl.glPopMatrix();
    }

    /**
     * モデル空間の描画。
     *
     * @param gl1 GLオブジェクト。
     */
    protected void drawModelSpace(GL gl1) {
        GL2 gl = gl1.getGL2();
        // モデル空間の座標値と姿勢を取得
        Position modelSpacePosition = state.getModelSpacePosition();
        Posture modelSpacePosture = state.getModelSpacePosture();

        // 現在のモデルビュー行列を保存
        gl.glPushMatrix();

        // GLの座標系をモデル空間の座標値と姿勢に一致するように設定
        gl.glTranslated(modelSpacePosition.getX(), modelSpacePosition.getY(), modelSpacePosition.getZ());
        gl.glRotated(-modelSpacePosture.getX(), 1.0, 0.0, 0.0);
        gl.glRotated(-modelSpacePosture.getY(), 0.0, 1.0, 0.0);
        gl.glRotated(-modelSpacePosture.getZ(), 0.0, 0.0, 1.0);

        // モデル空間のオブジェクト群の描画
        drawModelSpaceObjects(gl);

        // 保存したモデルビュー行列を復帰
        gl.glPopMatrix();
    }

    /**
     * 左目用の画面であることを示すラベルの描画。
     *
     * @param gl GLオブジェクト。
     * @param width ウィンドウの幅
     * @param height ウィンドウの高さ
     */
    protected void drawLeftLabel(GL gl, int width, int height) {
        renderer.setColor(Color.gray);          // ラベルの色の設定
        renderer.beginRendering(width, height); // ラベルの描画範囲を指定
        renderer.draw("L", 10, 10);             // ラベルの描画
        renderer.endRendering();                // ラベルの描画終了
    }

    /**
     * 右目用の画面であることを示すラベルの描画。
     *
     * @param gl GLオブジェクト。
     * @param width ウィンドウの幅
     * @param height ウィンドウの高さ
     */
    protected void drawRightLabel(GL gl, int width, int height) {
        renderer.setColor(Color.gray);          // ラベルの色の設定
        renderer.beginRendering(width, height); // ラベルの描画範囲を指定
        renderer.draw("R", width - 30, 10);     // ラベルの描画
        renderer.endRendering();                // ラベルの描画終了
    }

    /**
     * 左右の目の入れ替え。
     */
    public void swapEyes() {
        swapEyes = !swapEyes;
    }

    /**
     * VR空間中のオブジェクト群の描画。
     *
     * @param gl GLオブジェクト。
     */
    public void drawVRSpaceObjects(GL gl) {
    }

    /**
     * モデル空間中のオブジェクト群の描画。
     *
     * @param gl GLオブジェクト。
     */
    public void drawModelSpaceObjects(GL gl) {
    }

    /**
     * 2DのTextオブジェクト群の描画。
     *
     * @param gl GLオブジェクト。
     */
    public void draw2DTextSpaceObjects(GL gl) {
    }

    /**
     * テクスチャのパラメータ設定
     *
     * @param gl
     * @param texture テクスチャバッファ
     */
    private void setTextureParams(GL gl, int texture) {
        gl.glBindTexture(GL.GL_TEXTURE_2D, texture);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL2.GL_CLAMP_TO_BORDER);
        gl.glTexParameterf(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL2.GL_CLAMP_TO_BORDER);
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, (int)((Mode.VIVE.getWidth()/2)/(1-alpha)), (int)((Mode.VIVE.getWidth()/2)/(1-alpha)), 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, null);//予め拡大
        gl.glBindTexture(GL.GL_TEXTURE_2D, 0);
    }

    /**
     * レンダーバッファの設定
     *
     * @param gl1
     * @param depth デプスバッファ
     */
    private void setRenderBuffer(GL gl1, int depth) {
        GL2 gl = gl1.getGL2();
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, depth);
        gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL2.GL_DEPTH_COMPONENT, Mode.VIVE.getWidth(), Mode.VIVE.getHeight());
        gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, 0);
    }

    /**
     * フレームバッファの設定
     *
     * @param gl1
     * @param frameBuffer フレームバッファ
     * @param texture テクスチャバッファ
     * @param renderbuffer レンダーバッファ
     */
    private void setFrameBuffer(GL gl1, int frameBuffer, int texture, int renderbuffer) {
        GL2 gl = gl1.getGL2();
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, frameBuffer);
        gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, texture, 0);
        gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, renderbuffer);
        gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
    }

    /**
     * ポリゴンの置く座標を計算
     *
     * @param x 元のx座標
     * @param y 元のy座標
     * @param color 色のフラグ
     * @return 歪計算後の座標
     */
    DoubleBuffer distortion(double x, double y, double color) {
        if (color == 1) {
            double r2 = x * x + y * y;
            double r = Math.sqrt(r2);
            offset =-0.01;//調整中 
            double X = x/(1 + (alpha + offset) * r);
            double Y = y/(1 + (alpha + offset) * r);
            DoubleBuffer buffer = DoubleBuffer.wrap(new double[] {X, Y});
            return buffer;

        } else if (color == 2) {
            double r2 = x * x + y * y;
            double r = Math.sqrt(r2);
            offset =0.0;
            double X = x/(1 + (alpha + offset) * r);
            double Y = y/(1 + (alpha + offset) * r);         
            DoubleBuffer buffer = DoubleBuffer.wrap(new double[] {X, Y});
            return buffer;

        } else if (color == 3) {
            double r2 = x * x + y * y;
            double r = Math.sqrt(r2);
            offset =0.01;//調整中
            double X = x/(1 + (alpha + offset) * r);
            double Y = y/(1 + (alpha + offset) * r); 
            DoubleBuffer buffer = DoubleBuffer.wrap(new double[] {X, Y});
            return buffer;

        } else {

            double r2 = x * x + y * y;
            double r = Math.sqrt(r2);
            offset =0.0;
            alpha = 0.3;

            double X = x/(1 + (alpha + offset) * r);
            double Y = y/(1 + (alpha + offset) * r);
            
            DoubleBuffer buffer = DoubleBuffer.wrap(new double[] {X,Y});
       
            return buffer;
        }
    }

    /**
     * テクスチャを貼るポリゴンを作る
     *
     * @param gl1
     * @param color 色のフラグ
     */
    void drawPlate(GL gl1, double color) {
        GL2 gl = gl1.getGL2();
        //分割数
        double resolution = 64;
        //ポリゴンのサイズ
        //規格化されたプレートサイズ ( -1 <= x <= 1 ) , ( -1 <= y <= 1 )を拡大
        double plateSize = 2/(1-alpha);

        //分割幅
        double dx = plateSize / resolution;
        double dy = plateSize / resolution;

        gl.glBegin(GL.GL_TRIANGLES);
        for (int j = -(int) resolution / 2; j < (int) resolution / 2; j++) {
            for (int k = -(int) resolution / 2; k < (int) resolution / 2; k++) {
                //下の三角形
                gl.glTexCoord2d(positionXConverte(dx * j, plateSize), positionYConverte(dy * k, plateSize));//テクスチャのどこを対応づけるかの座標
                gl.glVertex2dv(distortion(dx * j, dy * k, color));//ポリゴンを置く座標の指定
                gl.glTexCoord2d(positionXConverte(dx * (j + 1), plateSize), positionYConverte(dy * k, plateSize));
                gl.glVertex2dv(distortion(dx * (j + 1), dy * k, color));
                gl.glTexCoord2d(positionXConverte(dx * j, plateSize), positionYConverte(dy * (k + 1), plateSize));
                gl.glVertex2dv(distortion(dx * j, dy * (k + 1), color));

                //上の三角形
                gl.glTexCoord2d(positionXConverte(dx * (j + 1), plateSize), positionYConverte(dy * k, plateSize));
                gl.glVertex2dv(distortion(dx * (j + 1), dy * k, color));
                gl.glTexCoord2d(positionXConverte(dx * (j + 1), plateSize), positionYConverte(dy * (k + 1), plateSize));
                gl.glVertex2dv(distortion(dx * (j + 1), dy * (k + 1), color));
                gl.glTexCoord2d(positionXConverte(dx * j, plateSize), positionYConverte(dy * (k + 1), plateSize));
                gl.glVertex2dv(distortion(dx * j, dy * (k + 1), color));
            }
        }

        gl.glEnd();
    }

    /**
     * ポリゴンをディスプレイリストに登録する
     *
     * @param gl1
     */
    private void compile(GL gl1) {
        GL2 gl = gl1.getGL2();

        double r = 1;
        double g = 2;
        double b = 3; 

        float polygonDepth = (float) 0;

        //中央から左右へのオフセット距離
        float polygonDistance = 1f;     

        gl.glNewList(listID, GL2.GL_COMPILE);
        //左
        gl.glBindTexture(GL.GL_TEXTURE_2D, m_texture.get(0));
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        gl.glLoadIdentity();
        gl.glTranslated(-polygonDistance, 0f, -polygonDepth);
        gl.glColor3f(1, 0, 0);
        drawPlate(gl, r);
        gl.glColor3f(0, 1, 0);
        drawPlate(gl, g);
        gl.glColor3f(0, 0, 1);
        drawPlate(gl, b);
        gl.glDisable(GL.GL_BLEND);

        //右
        gl.glBindTexture(GL.GL_TEXTURE_2D, m_texture.get(1));
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE);
        gl.glLoadIdentity();
        gl.glTranslated(polygonDistance, 0f, -polygonDepth);
        gl.glColor3f(1, 0, 0);
        drawPlate(gl, r);
        gl.glColor3f(0, 1, 0);
        drawPlate(gl, g);
        gl.glColor3f(0, 0, 1);
        drawPlate(gl, b);

        gl.glDisable(GL.GL_BLEND);
        gl.glEndList();
    }

    public double positionXConverte(double x, double size) {
        double conX;

        conX = x / size + 0.5;

        return conX;
    }

    public double positionYConverte(double y, double size) {
        double conY;

        conY = y / size + 0.5;

        return conY;
    }

}
