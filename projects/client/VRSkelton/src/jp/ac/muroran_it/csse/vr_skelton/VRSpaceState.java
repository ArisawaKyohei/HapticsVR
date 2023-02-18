/*
 * VRSpaceState.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;

/**
 * VR空間における仮想デバイスの配置およびモデル空間の配置からなるVR空間状態。
 */
public class VRSpaceState {
     /** アプリケーション名 **/
    private String mainName;

    /** 仮想視点センサ座標値 */
    private Position viewpointPosition = new Position(0.0, 35.0, 60.0);

    /** 仮想視点センサ姿勢 */
    private Posture viewpointPosture = new Posture(0.0, 0.0, -20.0);

    /** 仮想スタイラス座標値 */
    private Position stylusPosition = new Position(0.0, 0.0, 0.0);

    /** 仮想スタイラス姿勢 */
    private Posture stylusPosture = new Posture(-150.0, 0.0, 0.0);

    /** スタイラスのボタンが押されているか */
    private boolean stylusPressed = false;

    /** モデル空間座標値 */
    private Position modelSpacePosition = new Position(0.0, 0.0, 0.0);

    /** モデル空間姿勢 */
    private Posture modelSpacePosture = new Posture(0.0, 0.0, 0.0);

     /**
     * コンストラクタ
     * @param name アプリケーション名
     */
    public VRSpaceState(String name){
        mainName = name;
    }
    public VRSpaceState(){
        this("unknown");
    }

    /**
     * 指定したVR空間の座標値をモデル空間の座標値へ変換。
     * @param pos VR空間の座標値。
     * @return モデル空間の座標値。
     */
    public synchronized Position toModelSpace(Position pos) {
        Position position = modelSpacePosition;
        Posture posture = modelSpacePosture;
//
        // 初期値は入力座標値
        Position p = pos;

        // 平行移動
        p = p.translate(-position.getX(), -position.getY(), -position.getZ());

        // X軸まわりの回転
        p = p.rotateX(Math.toRadians(posture.getX()));

        // Y軸まわりの回転
        p = p.rotateY(Math.toRadians(posture.getY()));

        // Z軸まわりの回転
        p = p.rotateZ(Math.toRadians(posture.getZ()));

        return p;
    }

    /**
     * 指定したモデル空間の座標値をVR空間の座標値へ変換。
     * @param pos モデル空間の座標値。
     * @return VR空間の座標値。
     */
    public synchronized Position toVRSpace(Position pos) {
        Position position = modelSpacePosition;
        Posture posture = modelSpacePosture;

        // 初期値は入力座標値
        Position p = pos;

        // Z軸まわりの回転
        p = p.rotateZ(Math.toRadians(-posture.getZ()));

        // Y軸まわりの回転
        p = p.rotateY(Math.toRadians(-posture.getY()));

        // X軸まわりの回転
        p = p.rotateX(Math.toRadians(-posture.getX()));

        // 平行移動
        p = p.translate(position.getX(), position.getY(), position.getZ());

        return p;
    }

    /**
     * VR空間での仮想視点センサ座標値の取得。
     * @return 仮想視点センサ座標値。
     */
    public synchronized Position getViewpointPosition() {
        return viewpointPosition;
    }

    /**
     * VR空間での仮想視点センサ座標値の設定。
     * @param x x座標値。
     * @param y y座標値。
     * @param z z座標値。
     */
    public synchronized void setViewpointPosition(double x, double y, double z) {
        viewpointPosition = new Position(x, y, z);
    }

    /**
     * VR空間での仮想視点センサ姿勢の取得。
     * @return 仮想視点センサ姿勢。
     */
    public synchronized Posture getViewpointPosture() {
        return viewpointPosture;
    }

    /**
     * VR空間での仮想視点センサ姿勢の設定。
     * @param x x軸回転量。
     * @param y y軸回転量。
     * @param z z軸回転量。
     */
    public synchronized void setViewpointPosture(double x, double y, double z) {
        viewpointPosture = new Posture(x, y, z);
    }

    /**
     * VR空間でのスタイラス座標値の取得。
     * @return スタイラス座標値。
     */
    public synchronized Position getStylusPosition() {
        return stylusPosition;
    }

    /**
     * VR空間でのスタイラス標値の設定。
     * @param x x座標値。
     * @param y y座標値。
     * @param z z座標値。
     */
    public synchronized void setStylusPosition(double x, double y, double z) {
        stylusPosition = new Position(x, y, z);
    }

    /**
     * VR空間でのスタイラス姿勢の取得。
     * @return スタイラス姿勢。
     */
    public synchronized Posture getStylusPosture() {
        return stylusPosture;
    }

    /**
     * VR空間でのスタイラス姿勢の設定。
     * @param x x軸回転量。
     * @param y y軸回転量。
     * @param z z軸回転量。
     */
    public synchronized void setStylusPosture(double x, double y, double z) {
        stylusPosture = new Posture(x, y, z);
    }

    /**
     * スタイラスのボタン状態の取得。
     * @return スタイラスのボタンが押されている場合true。
     */
    public synchronized boolean isStylusPressed() {
        return stylusPressed;
    }

    /**
     * スタイラスのボタン状態の設定。
     * @param pressed スタイラスのボタンを押す場合true。
     */
    public synchronized void setStylusPressed(boolean pressed) {
        stylusPressed = pressed;
    }

    /**
     * VR空間に対するモデル空間の座標値の取得。
     * @return モデル空間の原点のワールド空間上における座標値。
     */
    public synchronized Position getModelSpacePosition() {
        return modelSpacePosition;
    }

    /**
     * VR空間に対するモデル空間の座標値の設定。
     * @param x x座標。
     * @param y y座標。
     * @param z z座標。
     */
    public synchronized void setModelSpacePosition(double x, double y, double z) {
        modelSpacePosition = new Position(x, y, z);
    }

    /**
     * VR空間に対するモデル空間の姿勢の取得。
     * @return モデル空間姿勢。
     */
    public synchronized Posture getModelSpacePosture() {
        return modelSpacePosture;
    }

    /**
     * VR空間に対するモデル空間の姿勢の設定。
     * @param x x軸回転量。
     * @param y y軸回転量。
     * @param z z軸回転量。
     */
    public synchronized void setModelSpacePosture(double x, double y, double z) {
        modelSpacePosture = new Posture(x, y, z);
    }

    /**
     * アプリケーション名の取得
     * @return アプリケーション名
     */
    public synchronized String getMainName(){
        return mainName;
    }
}
