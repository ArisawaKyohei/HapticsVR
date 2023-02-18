package jp.sagalab.model;

import jp.sagalab.jftk.curve.BezierCurve;
import jp.sagalab.jftk.force.calculator.SurfaceFrictionCalculator;
import jp.sagalab.jftk.force.surface.FrictionSurface;
import jp.sagalab.jftk.force.surface.Plane;
import jp.sagalab.jftk.force.surface.Sphere;
import jp.sagalab.jftk.Point;
import jp.sagalab.jftk.Vector;
import jp.sagalab.jftk.force.calculator.ForceCalculator;
import jp.sagalab.jftk.force.calculator.AxisFrictionCalculator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import jp.ac.muroran_it.csse.vr_skelton.VRSpaceState;

public class AppModel extends VRSpaceState{
    // force calculator
    private final ForceCalculator calculator;
    private boolean enableForceCalculation = false;

    // updaters
    private final ArrayList<ViewUpdater> viewUpdaters = new ArrayList<>();
    private final ArrayList<ForceUpdater> forceUpdaters = new ArrayList<>();

    public AppModel(ForceCalculator calculator) {
        this.calculator = calculator;
        if (calculator instanceof SurfaceFrictionCalculator) {
            surfaces = new FrictionSurface[]{
///*plane*/                    Plane.create(Point.createXYZ(0.0, 0, 0.15), Vector.createXYZ(1.00, 0.00, 1.00))
/*sphere*/                   Sphere.create(Point.createXYZ(0.05, 0.0, 0.1), 0.05)
///*pl-pl*/             Plane.create(Point.createXYZ(0.05, 0, 0.15), Vector.createXYZ(1, 0, 1)),
//                    Plane.create(Point.createXYZ(0.05, 0, 0.15), Vector.createXYZ(-1, 0, 1))
///*pl-sp*/           Plane.create(Point.createXYZ(0.05, 0, 0.15), Vector.createXYZ(1, 0, 1)),
//                    Sphere.create(Point.createXYZ(0.05, 0.0, 0.1), 0.1)
///*pl-pl-pl*/                    Plane.create(Point.createXYZ(0.05, 0, 0.05), Vector.createXYZ(0, 0, 1)),
//                    Plane.create(Point.createXYZ(0.05, 0, 0.05), Vector.createXYZ(0, 1, 0)),
//                    Plane.create(Point.createXYZ(0.05, 0, 0.05), Vector.createXYZ(1, 0, 0)),
//                    Plane.create(Point.createXYZ(0.05, 0, 0.1), Vector.createXYZ(1, 1, 0))
///*sp-sp*/           Sphere.create(Point.createXYZ(0.05, 0.03, 0.18), 0.05),
//                    Sphere.create(Point.createXYZ(0.05, -0.03, 0.18), 0.05),
///*out*/             Plane.create(Point.createXYZ(0.05, 0, 0.1), Vector.createXYZ(0, 0, 1)),
//                    Plane.create(Point.createXYZ(0.05, 0, 0.1), Vector.createXYZ(1, 0, 0)),
//                    Sphere.create(Point.createXYZ(0.05, -0.05, 0.1), 0.05)

            };
            ((SurfaceFrictionCalculator) calculator).addAll(surfaces);
        }
    }

    /** ViewUpdaterを追加する */
    public void addUpdater(ViewUpdater updater) {
        this.viewUpdaters.add(updater);
    }

    /** ViewUpdaterを追加する */
    public void addUpdaters(ViewUpdater... updaters) {
        this.viewUpdaters.addAll(Arrays.asList(updaters));
    }

    /** ForceUpdaterを追加する */
    public void addUpdater(ForceUpdater updater) {
        this.forceUpdaters.add(updater);
    }

    /** 各ViewUpdaterに再描画リクエストを送る */
    public void requestPaint() {
        viewUpdaters.forEach(ViewUpdater::requestRepaint);
    }

    // Haptics values
    /** Hapticsのスタイラス座標 */
    private Point hapticsPosition = Point.createXYZ(0, 0, 0);
    /** 力覚 */
    private Vector hapticsForce = Vector.createXYZ(0, 0, 0);
    /** 静止摩擦状態フラグ [x, y, z] */
    private final boolean[] forceFlags = new boolean[]{false, false, false};
    /** ボタン1の状態 */
    private boolean hapticsButton1Pressed = false;
    /** ボタン2の状態 */
    private boolean hapticsButton2Pressed = false;
    /** Hapticsスタイラスの軌跡 */
    private final LinkedList<Point> hapticsPoints = new LinkedList<>();
    /** 力覚フィードバックありの時の点列 */
    private final LinkedList<LinkedList<Point>> onForcePointsList = new LinkedList<>();
    /** 力覚フィードバックなしの時の点列 */
    private final LinkedList<LinkedList<Point>> offForcePointsList = new LinkedList<>();
    /** FBCの最後の点 */
    private Point fbcLastPoint = null;
    /** 立体 */
    private FrictionSurface[] surfaces;

    // 3D rotation
    private final double angleStep = 5;
    private final double angleLimit = 90;
    private double rx = 0;
    private double ry = 0;
    private double rz = 0;

    public Point getHapticsPosition() {
        return hapticsPosition;
    }

    public Vector getHapticsForce() {
        return hapticsForce;
    }

    public boolean isHapticsButton1Pressed() {
        return hapticsButton1Pressed;
    }

    public boolean isHapticsButton2Pressed() {
        return hapticsButton2Pressed;
    }

    public LinkedList<Point> getHapticsPoints() {
        return hapticsPoints;
    }

    public LinkedList<LinkedList<Point>> getOffForcePointsList() {
        return offForcePointsList;
    }

    public LinkedList<LinkedList<Point>> getOnForcePointsList() {
        return onForcePointsList;
    }

    public boolean isEnableForceCalculation() {
        return enableForceCalculation;
    }

    public boolean[] getForceFlags() {
        return forceFlags;
    }

    public FrictionSurface[] getSurfaces() {
        return surfaces;
    }

    public Map<FrictionSurface, Boolean> getSurfaceFlags() {
        if (enableForceCalculation) {
            return ((SurfaceFrictionCalculator) calculator). getFlags();
        } else {
            return null;
        }
    }

    public void setEnableForceCalculation(boolean enableForceCalculation) {
        this.enableForceCalculation = enableForceCalculation;
        if (!enableForceCalculation) {
            Arrays.fill(forceFlags, false);
            onForcePointsList.add(new LinkedList<>(hapticsPoints));
            fbcLastPoint = null;
        } else {
            offForcePointsList.add(new LinkedList<>(hapticsPoints));
        }
        hapticsPoints.clear();
    }

    public Point getFbcLastPoint() {
        return fbcLastPoint;
    }

    public void setHapticsPosition(double x, double y, double z, double time) {
        this.hapticsPosition = Point.createXYZT(x, y, z, time);
        // 力を計算する
        if (enableForceCalculation) {
            Vector force = calculator.calculate(hapticsPosition);
            System.out.print(force +" -> ");
            forceUpdaters.forEach((u) -> u.requestUpdate(force));
        } else {
            if (hapticsForce.length() != 0) {
                forceUpdaters.forEach((u) -> u.requestUpdate(Vector.createXYZ(0, 0, 0)));
            }
        }

        if (hapticsButton1Pressed) {
            if (hapticsPoints.isEmpty()) {
                hapticsPoints.add(hapticsPosition);
            } else {
                // 1秒間に30点程度サンプリング
                if (hapticsPoints.getLast().time() + 0.03 < time) {
                    hapticsPoints.add(hapticsPosition);
                }
            }
        }
        if (enableForceCalculation && calculator instanceof AxisFrictionCalculator) {
            // 静止摩擦状態を更新
            forceFlags[0] = ((AxisFrictionCalculator) calculator).getXFlag();
            forceFlags[1] = ((AxisFrictionCalculator) calculator).getYFlag();
            forceFlags[2] = ((AxisFrictionCalculator) calculator).getZFlag();
            // fbcを更新
            BezierCurve fbc = ((AxisFrictionCalculator) calculator).getFbc();
            if (fbc != null) {
                fbcLastPoint = fbc.evaluateAtEnd();
            }
        }
        if (enableForceCalculation && calculator instanceof SurfaceFrictionCalculator) {
            BezierCurve fbc = ((SurfaceFrictionCalculator) calculator).getFbc();
            if (fbc != null) {
                fbcLastPoint = fbc.evaluateAtEnd();
            }
        }
    }

    public void setHapticsForce(double x, double y, double z) {
        this.hapticsForce = Vector.createXYZ(x, y, z);
    }

    public void setHapticsButton1Pressed(boolean hapticsButton1Pressed) {
        this.hapticsButton1Pressed = hapticsButton1Pressed;
        if (hapticsButton1Pressed) {
            hapticsPoints.clear();
        } else {
            if (enableForceCalculation) {
                onForcePointsList.add(new LinkedList<>(hapticsPoints));
            } else {
                offForcePointsList.add(new LinkedList<>(hapticsPoints));
            }
        }
    }

    public void setHapticsButton2Pressed(boolean hapticsButton2Pressed) {
        this.hapticsButton2Pressed = hapticsButton2Pressed;
    }

    public void clearPoints() {
        onForcePointsList.clear();
        offForcePointsList.clear();
        hapticsPoints.clear();
    }

    public void up() {
        rx = max(rx - angleStep, -angleLimit);
    }

    public void down() {
        rx = min(rx + angleStep, angleLimit);
    }

    public void left() {
        ry = max(ry - angleStep, -angleLimit);
    }

    public void right() {
        ry = min(ry + angleStep, angleLimit);
    }

    public void setAngle(double rx, double ry, double rz) {
        this.rx = rx;
        this.ry = ry;
        this.rz = rz;
    }

    public double getRx() {
        return rx;
    }

    public double getRy() {
        return ry;
    }

    public double getRz() {
        return rz;
    }

    public void release() {
//        displayThread.release();
    }

    public void savePoints(){
        String foldername = "data0202/";
        try {
            Calendar cal = Calendar.getInstance();
            DateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmss");
            String name = format.format(cal.getTime());
            String surName = "_";
            for(FrictionSurface sur : surfaces){
                if(sur instanceof Plane){
                    surName = surName + "pl-";
                }else if(sur instanceof Sphere){
                    surName = surName + "sp-";
                }
            }

            name = name + surName;
            name = name.substring(0, name.length()-1);

            if(!onForcePointsList.isEmpty()) {
                FileWriter fw_on = new FileWriter(foldername + name + "_OnForcePoints");
                LinkedList<Point> onForcePoints = new LinkedList<>(onForcePointsList.getLast());
                for (Point p : onForcePoints) {
                    fw_on.write(p.x() + " " + p.y() + " " + p.z() + " " + p.time() + "\n");
                }
                fw_on.close();
            }

            if(!offForcePointsList.isEmpty()) {
                FileWriter fw_off = new FileWriter(foldername + name + "_OffForcePoints");
                LinkedList<Point> offForcePoints = new LinkedList<>(offForcePointsList.getLast());
                for (Point p : offForcePoints) {
                    fw_off.write(p.x() + " " + p.y() + " " + p.z() + " " + p.time() + "\n");
                }
                fw_off.close();
                System.out.println("#####");
            }

            if(surfaces.length != 0) {
                FileWriter sur = new FileWriter(foldername + name + "_Surfaces");
                for (FrictionSurface sf : surfaces) {
                    if(sf instanceof Plane){ // 0 → Plane
                        Point p = ((Plane) sf).base();
                        Vector v = ((Plane) sf).normal(p);
                        sur.write("0 "+p.x()+" "+p.y()+" "+p.z()+" "+v.x()+" "+v.y()+" "+v.z()+"\n");
                    }
                    else if(sf instanceof Sphere){ // 1 → Sphere
                        Point p = ((Sphere) sf).base();
                        double r = ((Sphere) sf).radius();
                        sur.write("1 "+p.x()+" "+p.y()+" "+p.z()+" "+r+"\n");
                    }
                }
                sur.close();
            }
        } catch (IOException ex){
            ex.printStackTrace();
        }
    }

    /* sphere x 0.05 ~ 0.09, y -0.08 ~ 0.08, z 0.03 ~ 0.15, d < r < 0.1
    * plane x 0 ~ 0.03, y -0.07 ~ 0.07, z 0.04 ~ 0.12,  */
    public void randomCircle(){
        ((SurfaceFrictionCalculator)calculator).removeAll();
        Random randomS = new Random();
        double ranSX = (double)(randomS.nextInt(40) + 50) /1000;
        double ranSY = (double)(randomS.nextInt(160) - 80) /1000;
        double ranSZ = (double)(randomS.nextInt(120) + 30) /1000;
        Point SPoint = Point.createXYZ(ranSX, ranSY, ranSZ);

        Random randomP = new Random();
        double ranPPX = (double)(randomP.nextInt(30) + 50) /1000;
        double ranPPY = (double)(randomP.nextInt(140) - 70) /1000;
        double ranPPZ = (double)(randomP.nextInt(10) + 40) /1000;
        Point PPoint = Point.createXYZ(ranPPX, ranPPY, ranPPZ);

        int ranPVZ_int = randomP.nextInt(1000);

//        double ranPVX = (double)(new Random().nextInt(ranPVZ_int) ) /1000;
//        double ranPVY = (double)(new Random().nextInt(ranPVZ_int) ) /1000;
//        double ranPVZ = (double)(ranPVZ_int) /1000;
        double ranPVX = new Random().nextDouble();
        double ranPVY = new Random().nextDouble();
        double ranPVZ = new Random().nextDouble();

        Vector PVector = Vector.createXYZ(ranPVX-0.5, ranPVY-0.5, ranPVZ-0.5);
        Plane plane = Plane.create(PPoint, PVector);

        int distanceInt = (int)(plane.projection(SPoint).distance(SPoint) * 1000);
        double SR = (double)(randomP.nextInt(100-distanceInt) + distanceInt) /1000;
        if(SR < 0){
            randomCircle();
        }else {
            Sphere sphere = Sphere.create(SPoint, SR);
            System.out.println(ranPVX + " " + ranPVY + " " +ranPVZ);
            surfaces = new FrictionSurface[]{
                    sphere,
                    plane
            };
            ((SurfaceFrictionCalculator) calculator).addAll(surfaces);
        }
    }

    public  void randomline(){
        ((SurfaceFrictionCalculator)calculator).removeAll();
        Random randomA = new Random();
        double ranAX = (double)(randomA.nextInt(30) + 50) /1000;
        double ranAY = (double)(randomA.nextInt(140) - 70) /1000;
        double ranAZ = (double)(randomA.nextInt(10) + 40) /1000;
        Point APoint = Point.createXYZ(ranAX, ranAY, ranAZ);

        double ranAVX = new Random().nextDouble()-0.5;
        double ranAVY = new Random().nextDouble()-0.5;
        double ranAVZ = new Random().nextDouble()-0.5;
        Vector AVector = Vector.createXYZ(ranAVX, ranAVY, ranAVZ);

        Random randomB = new Random();
        double ranBX = (double)(randomB.nextInt(30) + 50) /1000;
        double ranBY = (double)(randomB.nextInt(140) - 70) /1000;
        double ranBZ = (double)(randomB.nextInt(10) + 40) /1000;
        Point BPoint = Point.createXYZ(ranBX, ranBY, ranBZ);

        double ranBVX = new Random().nextDouble()-0.5;
        double ranBVY = new Random().nextDouble()-0.5;
        double ranBVZ = new Random().nextDouble()-0.5;
        Vector BVector = Vector.createXYZ(ranBVX, ranBVY, ranBVZ);

        surfaces = new FrictionSurface[]{
                Plane.create(APoint, AVector),
                Plane.create(BPoint, BVector)
        };
        ((SurfaceFrictionCalculator) calculator).addAll(surfaces);

    }

}
