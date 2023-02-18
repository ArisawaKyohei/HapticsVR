/*
 * ModelSpaceObject.java
 * 空間描画されるモデルのための抽象オブジェクト
 * Oct. 2017 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.vr_skelton;


import com.jogamp.opengl.GL;

public abstract class ModelSpaceObject {
    public abstract void drawObject(GL gl);
}
