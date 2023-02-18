package jp.sagalab;

import jp.sagalab.haptics.EventListener;
import jp.sagalab.haptics.HapticsClient;
import jp.sagalab.jftk.Matrix;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Calibrator implements EventListener {
    public static void main(String[] args) {
        try {
            new Calibrator().run();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /** geomagic既定座標値 */
    private final Matrix geo = Matrix.create(new double[][]{
            {-96.71, -96.44, -44.04, 1.0},
            {-0.45, -94.22, -10.12, 1.0},
            {97.30, -95.24, -39.43, 1.0},
            {-48.64, -38.45, -22.30, 1.0}
    }).transpose();

    /** 接続先IPアドレス */
    //private static final String liberty = "127.0.0.1";
    private static final String liberty = "shiigamoto";
    /** 接続先ポート番号 */
    private static final int port = 11113;
    /** 接続先デバイス番号 */
    private static final int deviceNum = 0;

    /** 座標 */
    private Point point;
    /** キャリブレーションに使用する点列 */
    private final ArrayList<double[]> elements = new ArrayList<>();
    /** 待機フラグ */
    private volatile boolean wait = true;

    void run() throws FileNotFoundException {
        // デバイスサーバに接続
        HapticsClient client = new HapticsClient();
        client.addListener(this);
        if (!client.connect(liberty, port, deviceNum)) {
            System.err.println("Failed to connect to server " + liberty + ":" + port);
            return;
        }

        // 座標取得待機
        System.out.println("wait input points...");
        while (wait) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // デバイスサーバとの接続解除
        client.disconnect();

        // 変換行列を生成
        Matrix lib = Matrix.create(new double[][]{
                elements.get(0),
                elements.get(1),
                elements.get(2),
                elements.get(3)
        }).transpose();

        // L x G-1
        Matrix geo2Lib = lib.product(geo.solve(Matrix.identity(4)));
        if (geo2Lib == null) {
            System.err.println("Cannot solve matrix.");
            return;
        }
        // ファイル出力
        PrintWriter printWriter = new PrintWriter("geo2Lib");
        for (double[] line : geo2Lib.elements()) {
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
            elements.add(point.copy());
            System.out.println("input:" + elements.size());
            if (elements.size() >= 4) {
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

        double[] copy() {
            return new double[]{x, y, z, 1.0};
        }
    }
}
