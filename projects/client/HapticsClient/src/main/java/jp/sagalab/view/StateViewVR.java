package jp.sagalab.view;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.util.gl2.GLUT;
import jp.ac.muroran_it.csse.vr_skelton.*;
import jp.sagalab.haptics.HapticsClient;
import jp.sagalab.jftk.Matrix;
import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.force.surface.FrictionSurface;
import jp.sagalab.jftk.force.surface.Plane;
import jp.sagalab.jftk.force.surface.Sphere;
import jp.sagalab.model.AppModel;

import java.util.Arrays;
import java.util.LinkedList;

import static com.jogamp.opengl.GL.GL_BLEND;
import static java.lang.Math.PI;

public class StateViewVR extends VRSpaceView {
    /**
     * VR空間状態
     */
    private final AppModel model;

    private double fovy = 30.0; // (度)
    private double distance = 100.0; // (mm)

    private final Matrix M; // World座標系 -> Liberty座標系 の変換行列
    private final double expand; // World座標系(m) -> Liberty座標系(cm) への拡大率
    private Matrix Axis = Matrix.create(new double[][]{
            {0.0,1.0,0.0,0.0},
            {0.0,0.0,1.0,0.0},
            {0.0,0.0,0.0,1.0},
            {1.0,1.0,1.0,1.0}
    });

    public StateViewVR(AppModel model, Config.Mode displayMode) {
        super(model, displayMode);
        this.model = model;
        this.M = HapticsClient.getLib2WorldMatrix().solve(Matrix.identity(4));
        this.expand =( Math.sqrt(Math.pow( M.get(0,0), 2 ) + Math.pow( M.get(1,0), 2 ) + Math.pow( M.get(2,0), 2 ) )
                     + Math.sqrt(Math.pow( M.get(0,1), 2 ) + Math.pow( M.get(1,1), 2 ) + Math.pow( M.get(2,1), 2 ) )
                     + Math.sqrt(Math.pow( M.get(0,2), 2 ) + Math.pow( M.get(1,2), 2 ) + Math.pow( M.get(2,2), 2 ) )
                     )/3;

    }

    @Override
    public void drawVRSpaceObjects(GL gl1) {

        setLighting(gl1);

        drawStylus(gl1);

        //drawAxis(gl1);

        LinkedList<Point> hapticsPoints = model.getHapticsPoints();
        for(int i=0; i<hapticsPoints.size()-1; i++){
            Point from = transformPoint(hapticsPoints.get(i));
            Point to = transformPoint(hapticsPoints.get(i+1));
            if(model.isEnableForceCalculation()){
                drawLine(gl1,
                        from.x(), from.y(), from.z(),
                        to.x(), to.y(), to.z(),
                        5f,
                        1f, 0f, 1f);
            }else{
                drawLine(gl1,
                        from.x(), from.y(), from.z(),
                        to.x(), to.y(), to.z(),
                        5f,
                        1f, 1f, 0f);
            }
        }
        LinkedList<LinkedList<Point>> onforcepoints = model.getOnForcePointsList();
        for(LinkedList<Point> list : onforcepoints){
            for(int i=0; i<list.size()-1; i++){
                Point from = transformPoint(list.get(i));
                Point to = transformPoint(list.get(i+1));
                drawLine(gl1,
                        from.x(), from.y(), from.z(),
                        to.x(), to.y(), to.z(),
                        5f,
                        1f, 0f, 1f);
            }
        }

        LinkedList<LinkedList<Point>> offforcepoints = model.getOffForcePointsList();
        for(LinkedList<Point> list : offforcepoints){
            for(int i=0; i<list.size()-1; i++){
                Point from = transformPoint(list.get(i));
                Point to = transformPoint(list.get(i+1));
                drawLine(gl1,
                        from.x(), from.y(), from.z(),
                        to.x(), to.y(), to.z(),
                        5f,
                        1f, 1f, 0f);
            }
        }
    }

    private Point transformPoint(Point p){
        Matrix point = M.product(Matrix.create(new double[][]{
                {p.x()},
                {p.y()},
                {p.z()},
                {1}
        }));
        return Point.createXYZ(point.get(0,0),point.get(1,0), point.get(2,0));
    }
    private Vector transformVector(Vector v){
        Matrix vector = M.product(Matrix.create(new double[][]{
                {v.x()},
                {v.y()},
                {v.z()},
                {0}
        }));
        return Vector.createXYZ(vector.get(0,0),vector.get(1,0), vector.get(2,0));
    }
    private void drawLine(GL gl1,
                          double fromX, double fromY, double fromZ,
                          double toX, double toY, double toZ,
                          float width,
                          float r, float g, float b){
        GL2 gl = gl1.getGL2();
        // 線の太さを設定
        gl.glLineWidth(width);
        // 色を設定
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, new float[]{r,g,b,1f}, 0);
        // 線の描画
        gl.glBegin(GL.GL_LINES);
        gl.glVertex3d(fromX, fromY, fromZ);
        gl.glVertex3d(toX, toY, toZ);
        gl.glEnd();
    }

    /**
     * 軸の描画
     * @param gl1
     */
    private void drawAxis(GL gl1) {
        GL2 gl = gl1.getGL2();

        Matrix GLAxis = M.product(Axis);

        float[] ambientDiffuseX = {1.0f, 0.0f, 0.0f, 1.0f}; // 赤
        float[] ambientDiffuseY = {0.0f, 1.0f, 0.0f, 1.0f}; // 緑
        float[] ambientDiffuseZ = {0.0f, 0.0f, 1.0f, 1.0f}; // 青
   //     System.out.println("x " + model.getViewpointPosition().getX() + "y " + model.getViewpointPosition().getY() + "z " + model.getViewpointPosition().getZ());

  //      gl.glTranslated(0,0,0);
        gl.glBegin(GL.GL_LINES);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuseX, 0); // x軸
        gl.glVertex3f((float)GLAxis.get(0,0), (float)GLAxis.get(1,0), (float)GLAxis.get(2,0));
        gl.glVertex3f((float)GLAxis.get(0,1), (float)GLAxis.get(1,1), (float)GLAxis.get(2,1));

        gl.glEnd();

        gl.glBegin(GL.GL_LINES);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuseY, 0); // y軸
        gl.glVertex3f((float)GLAxis.get(0,0), (float)GLAxis.get(1,0), (float)GLAxis.get(2,0));
        gl.glVertex3f((float)GLAxis.get(0,2), (float)GLAxis.get(1,2), (float)GLAxis.get(2,2));
        gl.glEnd();

        gl.glBegin(GL.GL_LINES);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuseZ, 0); // z軸
        gl.glVertex3f((float)GLAxis.get(0,0), (float)GLAxis.get(1,0), (float)GLAxis.get(2,0));
        gl.glVertex3f((float)GLAxis.get(0,3), (float)GLAxis.get(1,3), (float)GLAxis.get(2,3));
        gl.glEnd();
    }

    /**
     * 光源の設定。
     *
     * @param gl1 GLオブジェクト。
     */
    public void setLighting(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 点光源の座標値を設定
        float lightPosition[] = {0f, 70f, 0f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);

        // 環境光を設定
        float[] lightAmbient = new float[] {0.2f, 0.2f, 0.2f, 1.0f};
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
    }
    private void drawStylus(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 仮想スタイラスの座標値と姿勢を取得
        Point hapticsPosition = model.getHapticsPosition();
        Matrix GLposition = M.product(Matrix.create(new double[][]{
            {hapticsPosition.x()},
            {hapticsPosition.y()},
            {hapticsPosition.z()},
                {1}
        }));

        Point stylusPosition = Point.createXYZ(GLposition.get(0,0), GLposition.get(1,0), GLposition.get(2,0));
        Posture stylusPosture = model.getStylusPosture();

        // GLの座標系を仮想スタイラスの座標値と姿勢に一致するように設定
        gl.glPushMatrix();
        gl.glTranslated(stylusPosition.x(), stylusPosition.y(), stylusPosition.z()); // m -> cm
        gl.glRotated(stylusPosture.getX(), 0.0, 0.0, 1.0);
        gl.glRotated(stylusPosture.getY(), 0.0, 1.0, 0.0);
        gl.glRotated(stylusPosture.getZ(), 1.0, 0.0, 0.0);

        // スタイラスオブジェクトを描画
        drawStylusObjects(gl);
        gl.glPopMatrix();

    }
    /**
     * スタイラスオブジェクトの描画。
     *
     * @param gl1 GLオブジェクト。
     */
    private void drawStylusObjects(GL gl1) {
        GL2 gl = gl1.getGL2();
        // デフォルト色は緑色
        float[] ambientDiffuse = new float[] {0.0f, 1.0f, 0.0f, 1.0f};
        if (model.isStylusPressed()) {
            // スタイラスのボタンが押されているときは赤色
            ambientDiffuse = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
        }
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuse, 0);

        // スタイラスと同じ座標値、姿勢で角錐を描画
//        gl.glRotated(90.0, 0.0, 1.0, 0.0);
//        gl.glTranslated(0.0, 0.0, -16.0);
//        new GLUT().glutSolidCone(0.5, 16, 8, 1);
        Point lastpoint = model.getFbcLastPoint();
        if(lastpoint!=null){
//            new GLUT().glutWireSphere(lastpoint.fuzziness()*expand, 20, 20);
            float[] ambientDiffuse2 = {1.0f, 1.0f, 0.0f, 1.0f};
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuse2, 0);
            new GLUT().glutSolidSphere(0.5, 20, 20);
        }else {
            new GLUT().glutSolidSphere(1, 20, 20);
        }
    }
    /**
     * VRSpaceViewのdrawScene()の中で、モデル空間中のオブジェクト群を描画するときに呼ばれる。
     *
     * @param gl1 GLオブジェクト。
     */
    @Override
    public void drawModelSpaceObjects(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 表面を描画
        Arrays.asList(model.getSurfaces()).forEach(surface -> drawSurface(gl,surface));
    }

    /**
     * 球の描画。
     *
     * @param gl1 GL オブジェクト
     */
    private void drawSphere(GL gl1) {
        GL2 gl = gl1.getGL2();
        // 球座標を取得
        Position spherePosition = new Position(0.0, 30.0, -20.0);;
        // 球の半径の取得
        float sphereRadius = 5.0f;

        // 現在のモデルビュー行列を保存
        gl.glPushMatrix();

        // GLの座標系の原点を球の座標と一致するように設定
        gl.glTranslated(spherePosition.getX(), spherePosition.getY(), spherePosition.getZ());

        // 球の色を赤色に指定
        float[] ambientDiffuse = new float[] {1.0f, 0.0f, 0.0f, 1.0f};
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuse, 0);

        // 球を描画
//        new GLUT().glutWireSphere(sphereRadius, 20, 20);
        new GLUT().glutSolidSphere(sphereRadius, 20, 20);

        // 保存したモデルビュー行列を復帰
        gl.glPopMatrix();
    }

    private void drawSurface(GL gl1, FrictionSurface surface){
        GL2 gl = gl1.getGL2();
        gl.glEnable(GL_BLEND);  //ブレンド有効化

        gl.glPushMatrix();

        float[] ambientDiffuse = new float[]{0f, 1f, 1f, 0.8f};
        //gl.glColor4d(0, 1, 1, 0.5);//色とアルファ
        gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuse, 0);
        gl.glColor4d(0, 1, 1, 0.8);//色とアルファ

        if(model.getSurfaceFlags() != null){
            if (model.getSurfaceFlags().get(surface)) {
                float[] ambientDiffuse2 = {1f, 1f, 0f, 0.8f};
                //gl.glColor4d(1, 1, 0, 0.5);//色とアルファ
                gl.glMaterialfv(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE, ambientDiffuse2, 0);
                gl.glColor4d(1, 1, 0, 0.8);//色とアルファ
            }
        }



        if(surface instanceof Plane){
            Vector norm = transformVector(surface.normal(null));
            Vector zAxis = Vector.createXYZ(0.0, 0.0, 1.0);
//            System.out.println(transformVector(zAxis));
//            System.out.println(transformVector(Vector.createXYZ(-1.0, 0.0, 0.0)));
            Vector rotateAxis = zAxis.cross(norm).normalize();
            //System.out.println(rotateAxis);
            double degree = 180 * zAxis.angle(norm) / PI;

            Matrix GLSurfaceBase = M.product(Matrix.create(new double[][]{
                    {((Plane)surface).base().x()},
                    {((Plane)surface).base().y()+0.0},
                    {((Plane)surface).base().z()+0.0},
                    {1}
            }));

            //System.out.println(GLSurfaceBase);
//            gl.glTranslated(((Plane)surface).base().x()/10, ((Plane)surface).base().y()/10, ((Plane)surface).base().z()/10); // base() キャリブレーションする
            gl.glTranslated(GLSurfaceBase.get(0,0), GLSurfaceBase.get(1,0), GLSurfaceBase.get(2,0));
            //System.out.println(rotateAxis.x() + " " + rotateAxis.y() + " " + rotateAxis.z());
            gl.glRotated(degree, rotateAxis.x(), rotateAxis.y(), rotateAxis.z());
            gl.glBegin(GL2.GL_POLYGON);
            gl.glVertex3d(-distance, distance, 0.0);
            gl.glVertex3d(distance, distance, 0.0);
            gl.glVertex3d(distance, -distance, 0.0);
            gl.glVertex3d(-distance, -distance, 0.0);
            gl.glEnd();
        }else if(surface instanceof Sphere){
            Matrix GLSurfaceBase = M.product(Matrix.create(new double[][]{
                    {((Sphere)surface).base().x()},
                    {((Sphere)surface).base().y()},
                    {((Sphere)surface).base().z()},
                    {1}
            }));

  //          gl.glTranslated(((Sphere)surface).base().x(), ((Sphere)surface).base().y(), ((Sphere)surface).base().z());
            gl.glTranslated(GLSurfaceBase.get(0,0), GLSurfaceBase.get(1,0), GLSurfaceBase.get(2,0));
            new GLUT().glutSolidSphere(((Sphere)surface).radius()*expand, 64, 64);

        }

        gl.glPopMatrix();
        gl.glDisable(GL_BLEND);  //ブレンド無効化
    }
}
