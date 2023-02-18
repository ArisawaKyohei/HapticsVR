/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.ac.muroran_it.csse.simple_viewer;
import jp.ac.muroran_it.csse.vr_skelton.TimerController;
import jp.ac.muroran_it.csse.vr_skelton.VRSpaceState;

/**
 *
 * @author j17024007
 */
public class AnimationController extends TimerController{
    
    /** モデル空間 */
    SimpleVRSpaceState state;
    
    /** アニメーションによる球の半径の変化量 */
    private float scale = 0.125f;
    
    /**
    * 操作する VR 空間状態を指定するコンストラクタ
    * @param state VR 空間状態。
    */
    
    public AnimationController(VRSpaceState state) {
      super(state); // 継承元の TimerController のコンストラクタ
      this.state = (SimpleVRSpaceState)state;
    }
    
    /**
    * アニメーションのメイン処理
    */
    @Override
    public void action(){
        // アニメーションが ON(true) の状態の時に更新処理を実行
        if(state.isAnimationRunning()){
            // 球の半径を取得
            float sphereRadius = state.getSphereRadius();
            // 球の半径がアニメーション変化の範囲外だったら符号を反転
            if(sphereRadius > 10.0 || sphereRadius < 0.1)
                scale = -scale;
                // 球の半径を更新
                sphereRadius += scale;
                state.setSphereRadius(sphereRadius);
        }
    }
}
