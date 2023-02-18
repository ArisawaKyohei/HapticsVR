/*
 * SimpleVRSpaceState.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.simple_viewer;

import jp.ac.muroran_it.csse.vr_skelton.Position;
import jp.ac.muroran_it.csse.vr_skelton.VRSpaceState;

/**
 * ティーポットの座標値を付加したVR空間状態。
 */
public class SimpleVRSpaceState extends VRSpaceState {
    /* ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ #3 ▼▼▼ */
    /**
     * ティーポット座標値
     */
    private Position teapotPosition = new Position(0.0, 30.0, -20.0);

    /**
     * ティーポットを選択している場合はtrue
     */
    private boolean teapotSelected = false;

    /**
     * ティーポットの色
     */
    private float[] teapotAmbientDiffuse = new float[] {0.0f, 0.0f, 1.0f, 1.0f};

    private Position spherePosition = new Position(0.0, 30.0, -20.0);

    /**
     * 球の半径
     */
    private float sphereRadius = 5.0f;

    /**
     * メニュー
     */
    private Menu menu = Menu.create(this);

    /**
     * アニメーションが実行している場合はtrue
     */
    private boolean animationRunning = false;

    /**
     * テクスチャ座標値
     */
    private Position texturePosition = new Position(-10.0, 15.0, -10.0);

    /**
     * 文字列座標値
     */
    private Position stringPosition = new Position(0.0, 30.0, -20.0);

    /* ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ #3 ▲▲▲ */

 /* ▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼▼ #4 ▼▼▼ */
    /**
     * スタイラスのボタン状態の設定。 ボタンを押したときにスタイラスがティーポットの近くにあったらティーポットを選択する。
     *
     * @param pressed スタイラスのボタンを押す場合true。
     */
    @Override
    public synchronized void setStylusPressed(boolean pressed) {
        super.setStylusPressed(pressed);
        if (pressed) {
            // モデル空間でのスタイラスの座標値を取得
            Position stylusPosition = toModelSpace(getStylusPosition());

            // スタイラスがティーポットの近くにあったらティーポットを選択
            if (stylusPosition.calculateDistance(teapotPosition) < 5.0) {
                teapotSelected = true;
            } else {
                teapotSelected = false;
            }
        }
    }

    /**
     * ティーポット座標値の取得。
     *
     * @return ティーポット座標値。
     */
    public synchronized Position getTeapotPosition() {
        return teapotPosition;
    }

    /**
     * ティーポットの選択状態の取得。
     *
     * @return ティーポットが選択されていた場合はtrue。
     */
    public synchronized boolean isTeapotSelected() {
        return teapotSelected;
    }

    
    /**
     * ティーポットの色を設定。
     *
     * @param color カラー情報。
     */
    public synchronized void setTeapotColor(float[] color) {
        teapotAmbientDiffuse = color;
    }

    /**
     * ティーポットの色の取得。
     *
     * @returnティーポットの色。
     */
    public synchronized float[] getTeapotColor() {
        return teapotAmbientDiffuse;
    }

    /**
     * 球の半径の設定。
     *
     * @param radius 球の半径。
     */
    public synchronized void setSphereRadius(float radius) {
        sphereRadius = radius;
    }

    /**
     * アニメーションの実行状態の取得。
     *
     * @return アニメーションが実行されていた場合は true 。
     */
    public synchronized boolean isAnimationRunning() {
        return animationRunning;
    }

    /**
     * アニメーションの実行状態の設定。
     *
     * @param animationFlag アニメーションを実行する場合 true 。
     */
    public synchronized void setAnimationFlag(boolean animationFlag) {
        animationRunning = animationFlag;
    }

    
    /**
     * ティーポットの選択状態の取得
     *
     * @return ティーポットが選択されていた場合は true。
     */
    public synchronized boolean isTeapotsSelected() {
        return teapotSelected;
    }

    /**
     * 球座標値の取得。
     *
     * @return 球座標値。
     */
    public synchronized Position getSpherePosition() {
        return spherePosition;
    }

    /**
     * 球の半径の取得。
     *
     * @return 球の半径。
     */
    public synchronized float getSphereRadius() {
        return sphereRadius;
    }

    /**
     * テクスチャ座標値の取得。
     *
     * @return テクスチャ座標値。
     */
    public synchronized Position getTexturePosition() {
        return texturePosition;
    }

    /**
     * 文字列座標値の取得。
     *
     * @return 文字列座標値。
     */
    public synchronized Position getStringPosition() {
        return stringPosition;
    }

    /**
     *
     * メニューの取得。
     *
     * @return メニュー。
     */
    public synchronized Menu getMenu() {
        return menu;
    }
    /* ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲ #4 ▲▲▲ */
}
