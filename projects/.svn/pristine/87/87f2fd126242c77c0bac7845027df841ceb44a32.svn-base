/*
 * VRSpaceView.java
 * オープンデータビューアのVR空間状態の内容を描画するVRSpaceView実装
 * Oct. 2016 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.pbl2018;

import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.j2d.TextRenderer;
import java.awt.Color;
import java.awt.Font;
import javax.media.opengl.GL;
import jp.ac.muroran_it.csse.vr_skelton.Position;
import jp.ac.muroran_it.csse.vr_skelton.Posture;
import jp.ac.muroran_it.csse.vr_skelton.VRSpaceView;
import jp.ac.muroran_it.csse.vr_skelton.Config.Mode;
import jp.ac.muroran_it.csse.vr_skelton.ModelSpaceObject;

public class OpenDataVRSpaceView extends VRSpaceView {
    /** テキストレンダラ */
    private final TextRenderer renderer;

    /** オープンデータビューアビューアのVR空間状態 */
    private final OpenDataVRSpaceState state;

    /**
     * 描画するオープンデータビューアビューアのVR空間状態とステレオ表示の設定を指定するコンストラクタ。
     * @param state オープンデータビューアビューアのVR空間状態。
     * @param stereo ステレオ表示を有効にする場合はtrue。
     */
    public OpenDataVRSpaceView(OpenDataVRSpaceState state, Mode stereo) {
        super(state, stereo);
        this.state = state;
        // レンダリングする文字のフォント、字体、サイズを指定
        renderer = new TextRenderer(new Font("SansSerif", Font.PLAIN, 60));
        
        
    }

    /**
     * VRSpaceViewのdrawScene()の中で、VR空間中のオブジェクト群を描画するときに呼ばれる。
     * @param gl GLオブジェクト。
     */
    @Override
    public void drawVRSpaceObjects(GL gl) {
        // 光源を設定
        setLighting(gl);
        // スタイラスを描画
        drawStylus(gl);
    }

    /**
     * 光源の設定。
     * @param gl GLオブジェクト。
     */
    public void setLighting(GL gl) {
        // 点光源の座標値を設定
        float lightPosition[] = {0f, 70f, 0f, 1.0f};
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, lightPosition, 0);

        // 環境光を設定
        float[] lightAmbient = new float[] {0.2f, 0.2f, 0.2f, 1.0f};
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, lightAmbient, 0);
    }

    /**
     * スタイラスの描画。
     * @param gl GLオブジェクト。
     */
    private void drawStylus(GL gl) {
        // 仮想スタイラスの座標値と姿勢を取得
        Position stylusPosition = state.getStylusPosition();
        Posture stylusPosture = state.getStylusPosture();

        // GLの座標系を仮想スタイラスの座標値と姿勢に一致するように設定
        gl.glTranslated(stylusPosition.getX(), stylusPosition.getY(), stylusPosition.getZ());
        gl.glRotated(stylusPosture.getX(), 0.0, 0.0, 1.0);
        gl.glRotated(stylusPosture.getY(), 0.0, 1.0, 0.0);
        gl.glRotated(stylusPosture.getZ(), 1.0, 0.0, 0.0);

        // スタイラスオブジェクトを描画
        drawStylusObjects(gl);
    }

    /**
     * スタイラスオブジェクトの描画。
     * @param gl GLオブジェクト。
     */
    public void drawStylusObjects(GL gl) {
        // スタイラスの反射特性を設定
        float[] ambientDiffuse = new float[] {1.0f, 1.0f, 1.0f, 1.0f};
        if (state.isStylusPressed()) {
            // スタイラスのボタンが押されているときは赤色
            ambientDiffuse = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
        }
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT_AND_DIFFUSE, ambientDiffuse, 0);

        // スタイラスと同じ座標値、姿勢に円錐を描画
        gl.glRotated(90.0, 0.0, 1.0, 0.0);
        gl.glTranslated(0.0, 0.0, -16.0);
        new GLUT().glutSolidCone(0.5, 16, 16, 1);
    }

    /**
     * VRSpaceViewのdrawScene()の中で、モデル空間中のオブジェクトを描画するときに呼ばれる。
     * @param gl GLオブジェクト。
     */
    @Override
    public void drawModelSpaceObjects(GL gl) {
        // State内のオブジェクトリストに格納されている空間オブジェクトを１つずつ描画する
        for(ModelSpaceObject obj : state.getVRSpaceObjects()){
            obj.drawObject(gl);
        }
    }
    
    
    /**
     * VRSpaceViewのdisplay()の中で、2DのTextオブジェクト群を描画するときに呼ばれる。
     * @param gl 
     */
    @Override
    public void draw2DTextSpaceObjects(GL gl) {
        // スクリーン下部に補助情報を描画
        drawInformationToBottom(state.getInformation());
    }

    /**
     * 情報を表す文字列を画面下部に描画。
     * @param strings 表示される情報
     */
    private void drawInformationToBottom(String[] strings) {
            // 渡された文字列が空ならば何もしない
            if(strings == null){
                //System.out.println("None");
                return;
            }
            // 描画する文字の色を指定
            renderer.setColor(Color.white);
            // 描画領域を指定
            renderer.beginRendering(1400, 1050);
            // 震源情報を表す文字列を改行しながら描画 ※画面左下が(0,0)
            for (int i = 0; i < strings.length; ++i) {
                // (左余白10, 下余白10 + 表示行数*25) の位置から描画開始
                renderer.draw(strings[i], 10, (10 + strings.length * 25) - i * 25); // 行間20->25
                System.out.println("String["+i+"]:"+strings[i]);
            }
            renderer.endRendering();
    }

}
