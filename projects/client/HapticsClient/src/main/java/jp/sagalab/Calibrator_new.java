package jp.sagalab;

import jp.sagalab.haptics.EventListener;
import jp.sagalab.haptics.HapticsClient;
import jp.sagalab.jftk.Matrix;
import jp.sagalab.jftk.Point;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Calibrator_new implements EventListener {
    public static void main(String[] args) {
        try {
            new Calibrator_new().run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /** World座標系での4点の座標(ユークリッド空間) */
    public final Matrix world_euc = Matrix.create(new double[][]{
            { 0.0, 0.07, 0.0, 0.0},
            { 0.0, 0.0 , 0.1, 0.0},
            { 0.0, 0.0 , 0.0, 0.2},
            { 1.0, 1.0 , 1.0, 1.0}
    });

    /** World座標系での4点の座標(ベクトル空間) */
    public final Matrix world_vec = Matrix.create(new double[][]{
            { 0.07, 0.0, 0.0},
            { 0.0 , 0.1, 0.0},
            { 0.0 , 0.0, 0.2}
    });

    /** Hapticsサーバ IPアドレス */
    private static final String hapticsServer = "localhost";
    /** Hapticsサーバ ポート番号 */
    private static final int hapticsPort = 9993;
    /** Hapticsサーバ デバイス番号 */
    private static final int hapticsDeviceNum = 0;

    /** Libertyサーバ IPアドレス */
    private static final String libertyServer = "wakana";
    /** Libertyサーバ ポート番号 */
    private static final int libertyPort = 11113;
    /** Libertyサーバ デバイス番号 */
    private static final int libertyDeviceNum = 0; // 偶数: スタイラス, 奇数: カメラ

//    /** Haptics座標系での４点の座標 */
//    private final ArrayList<Point> hapticsPoints = new ArrayList<>();
//
//    /** Liberty座標系での４点の座標 */
//    private final ArrayList<Point> libertyPoints = new ArrayList<>();

    /** 座標 */
    private Point point;

    private final ArrayList<Point> points = new ArrayList<>();

    /** 待機フラグ */
    private volatile boolean wait = true;

    /** サーバ識別番号 */
    private int serverNum = 0; // 0: Hapticsサーバ, 1: libertyサーバ

    void run() throws FileNotFoundException {
        // Hapticsサーバに接続
        HapticsClient hapticsClient = new HapticsClient();
        hapticsClient.addListener(this);
        if (!hapticsClient.connect(hapticsServer, hapticsPort, hapticsDeviceNum)) {
            System.err.println("Failed to connect to hapticsServer " + hapticsServer + ":" + hapticsPort);
            return;
        }
        serverNum = 0;

        // Geomagicの座標取得待機
        System.out.println("Please push Geomagic's pen on 4points");
        while (wait) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hapticsClient.disconnect();

        // Geomagicの変換行列を生成
        // 位置座標系
        Matrix geo_euc = Matrix.create(new double[][]{
                { points.get(0).x, points.get(1).x, points.get(2).x, points.get(3).x },
                { points.get(0).y, points.get(1).y, points.get(2).y, points.get(3).y },
                { points.get(0).z, points.get(1).z, points.get(2).z, points.get(3).z },
                {             1.0,             1.0,             1.0,             1.0 }
        });
        // 上記でずれる場合
//        Matrix geo_euc = Matrix.create(new double[][]{
//                { points.get(0).x, points.get(0).x, points.get(0).x+100, points.get(0).x },
//                { points.get(0).y, points.get(0).y, points.get(0).y, points.get(0).y+200 },
//                { points.get(0).z, points.get(0).z+70, points.get(0).z, points.get(0).z },
//                {             1.0,             1.0,             1.0,             1.0 }
//        });

        // ベクトル座標系
        Matrix geo_vec = Matrix.create(new double[][]{
                { points.get(1).x - points.get(0).x, points.get(2).x - points.get(0).x, points.get(3).x - points.get(0).x },
                { points.get(1).y - points.get(0).y, points.get(2).y - points.get(0).y, points.get(3).y - points.get(0).y },
                { points.get(1).z - points.get(0).z, points.get(2).z - points.get(0).z, points.get(3).z - points.get(0).z },
        });
        // 上記でずれる場合
//        Matrix geo_vec = Matrix.create(new double[][]{
//                { points.get(0).x - points.get(0).x, points.get(0).x+100 - points.get(0).x, points.get(0).x - points.get(0).x },
//                { points.get(0).y - points.get(0).y, points.get(0).y - points.get(0).y, points.get(0).y+200 - points.get(0).y },
//                { points.get(0).z+70 - points.get(0).z, points.get(0).z - points.get(0).z, points.get(0).z - points.get(0).z },
//        });

        // Geomagic -> World 位置座標変換行列生成
        Matrix M_geo2world_euc = world_euc.product(geo_euc.solve(Matrix.identity(4)));

        // World -> Geomagic ベクトル座標変換行列生成
        Matrix M_world2geo_vec = Matrix.create(new double[][]{
                {0.001,0.0,0.0},
                {0.0,0.001,0.0},
                {0.0,0.0,0.001}
        }).product(geo_vec.product(world_vec.solve(Matrix.identity(3))));


        // ファイル出力
        PrintWriter printWriter = new PrintWriter("geo2world_euc");
        for (double[] line : M_geo2world_euc.elements()) {
            printWriter.print(line[0]);
            printWriter.print(",");
            printWriter.print(line[1]);
            printWriter.print(",");
            printWriter.print(line[2]);
            printWriter.print(",");
            printWriter.println(line[3]);
        }
        printWriter.close();

        // ファイル出力
        printWriter = new PrintWriter("world2geo_vec");
        for (double[] line : M_world2geo_vec.elements()) {
            printWriter.print(line[0]);
            printWriter.print(",");
            printWriter.print(line[1]);
            printWriter.print(",");
            printWriter.println(line[2]);
        }
        printWriter.close();

        wait = true;
        points.clear();

        // Libertyサーバに接続
        HapticsClient libertyClient = new HapticsClient();
        libertyClient.addListener(this);
        if (!libertyClient.connect(libertyServer, libertyPort, libertyDeviceNum)) {
            System.err.println("Failed to connect to libertyServer " + libertyServer + ":" + libertyPort);
            return;
        }
        serverNum = 1;

        // libertyの座標取得待機
        System.out.println("Please push styluspen on 4points");
        while (wait) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        libertyClient.disconnect();

        // Libertyの変換行列を生成
        Matrix lib_euc = Matrix.create(new double[][]{
                { points.get(0).x, points.get(1).x, points.get(2).x, points.get(3).x },
                { points.get(0).y, points.get(1).y, points.get(2).y, points.get(3).y },
                { points.get(0).z, points.get(1).z, points.get(2).z, points.get(3).z },
                {             1.0,             1.0,             1.0,             1.0 }
        });

        // 上記でずれる場合
//        Matrix lib_euc = Matrix.create(new double[][]{
//                { points.get(0).x, points.get(0).x, points.get(0).x-10, points.get(0).x },
//                { points.get(0).y, points.get(0).y, points.get(0).y, points.get(0).y+20 },
//                { points.get(0).z, points.get(0).z-7, points.get(0).z, points.get(0).z },
//                {             1.0,             1.0,             1.0,             1.0 }
//        });

        // Liberty -> World の位置座標系変換行列生成
        Matrix M_lib2world_euc = world_euc.product(lib_euc.solve(Matrix.identity(4)));

        // ファイル出力
        printWriter = new PrintWriter("lib2world_euc");
        for (double[] line : M_lib2world_euc.elements()) {
            printWriter.print(line[0]);
            printWriter.print(",");
            printWriter.print(line[1]);
            printWriter.print(",");
            printWriter.print(line[2]);
            printWriter.print(",");
            printWriter.println(line[3]);
        }
        printWriter.close();
    }

    @Override
    public void onReleased(int buttonId, long time) {
        if (wait) {
            if (point == null) {
                System.err.println("point is null");
                return;
            }
            points.add(point);
            System.out.println(point);
            System.out.println("input:" + points.size());
            if (points.size() >= 4) {
                wait = false;
            }
        }
    }

    @Override
    public void position(double x, double y, double z, long time) {
        point = new Point(x, y, z);
    }

    static class Point {
        double x, y, z;

        Point(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public String toString(){
            return Double.toString(x) + " " + Double.toString(y) + " " + Double.toString(z);
        }

    }
}
