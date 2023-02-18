/*
 * TimerController.java
 *
 * Oct. 2012 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 定時処理を行うコントローラー
 */
public class TimerController extends Thread {
    // VR空間状態
    VRSpaceState state;
    
    // アニメーションのインターバル
    private int interval = 10;
    
    /**
     * 操作するVR空間状態を指定するコンストラクタ。
     * @param state VR空間状態。
     */
    public TimerController(VRSpaceState state) {
        this.state = state;
    }

    /**
     * アニメーションの間隔をセット
     * @param interval インターバル(ミリ秒)
     */
    public void setAnimationInterval(int interval){
        this.interval = interval;
    }
    
    /**
     * アニメーション動作のメイン処理
     */
    protected void action(){
        
    }
    
    @Override
    public void run(){
        while(true){
            try{
                Thread.sleep(interval);
            }catch(InterruptedException ex){
                Logger.getLogger(TimerController.class.getName()).log(Level.SEVERE,null,ex);
            }
            action();
        }
    }
}
