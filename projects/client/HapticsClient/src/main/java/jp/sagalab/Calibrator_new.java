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

    /** World���W�n�ł�4�_�̍��W(���[�N���b�h���) */
    public final Matrix world_euc = Matrix.create(new double[][]{
            { 0.0, 0.07, 0.0, 0.0},
            { 0.0, 0.0 , 0.1, 0.0},
            { 0.0, 0.0 , 0.0, 0.2},
            { 1.0, 1.0 , 1.0, 1.0}
    });

    /** World���W�n�ł�4�_�̍��W(�x�N�g�����) */
    public final Matrix world_vec = Matrix.create(new double[][]{
            { 0.07, 0.0, 0.0},
            { 0.0 , 0.1, 0.0},
            { 0.0 , 0.0, 0.2}
    });

    /** Haptics�T�[�o IP�A�h���X */
    private static final String hapticsServer = "localhost";
    /** Haptics�T�[�o �|�[�g�ԍ� */
    private static final int hapticsPort = 9993;
    /** Haptics�T�[�o �f�o�C�X�ԍ� */
    private static final int hapticsDeviceNum = 0;

    /** Liberty�T�[�o IP�A�h���X */
    private static final String libertyServer = "wakana";
    /** Liberty�T�[�o �|�[�g�ԍ� */
    private static final int libertyPort = 11113;
    /** Liberty�T�[�o �f�o�C�X�ԍ� */
    private static final int libertyDeviceNum = 0; // ����: �X�^�C���X, �: �J����

//    /** Haptics���W�n�ł̂S�_�̍��W */
//    private final ArrayList<Point> hapticsPoints = new ArrayList<>();
//
//    /** Liberty���W�n�ł̂S�_�̍��W */
//    private final ArrayList<Point> libertyPoints = new ArrayList<>();

    /** ���W */
    private Point point;

    private final ArrayList<Point> points = new ArrayList<>();

    /** �ҋ@�t���O */
    private volatile boolean wait = true;

    /** �T�[�o���ʔԍ� */
    private int serverNum = 0; // 0: Haptics�T�[�o, 1: liberty�T�[�o

    void run() throws FileNotFoundException {
        // Haptics�T�[�o�ɐڑ�
        HapticsClient hapticsClient = new HapticsClient();
        hapticsClient.addListener(this);
        if (!hapticsClient.connect(hapticsServer, hapticsPort, hapticsDeviceNum)) {
            System.err.println("Failed to connect to hapticsServer " + hapticsServer + ":" + hapticsPort);
            return;
        }
        serverNum = 0;

        // Geomagic�̍��W�擾�ҋ@
        System.out.println("Please push Geomagic's pen on 4points");
        while (wait) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        hapticsClient.disconnect();

        // Geomagic�̕ϊ��s��𐶐�
        // �ʒu���W�n
        Matrix geo_euc = Matrix.create(new double[][]{
                { points.get(0).x, points.get(1).x, points.get(2).x, points.get(3).x },
                { points.get(0).y, points.get(1).y, points.get(2).y, points.get(3).y },
                { points.get(0).z, points.get(1).z, points.get(2).z, points.get(3).z },
                {             1.0,             1.0,             1.0,             1.0 }
        });
        // ��L�ł����ꍇ
//        Matrix geo_euc = Matrix.create(new double[][]{
//                { points.get(0).x, points.get(0).x, points.get(0).x+100, points.get(0).x },
//                { points.get(0).y, points.get(0).y, points.get(0).y, points.get(0).y+200 },
//                { points.get(0).z, points.get(0).z+70, points.get(0).z, points.get(0).z },
//                {             1.0,             1.0,             1.0,             1.0 }
//        });

        // �x�N�g�����W�n
        Matrix geo_vec = Matrix.create(new double[][]{
                { points.get(1).x - points.get(0).x, points.get(2).x - points.get(0).x, points.get(3).x - points.get(0).x },
                { points.get(1).y - points.get(0).y, points.get(2).y - points.get(0).y, points.get(3).y - points.get(0).y },
                { points.get(1).z - points.get(0).z, points.get(2).z - points.get(0).z, points.get(3).z - points.get(0).z },
        });
        // ��L�ł����ꍇ
//        Matrix geo_vec = Matrix.create(new double[][]{
//                { points.get(0).x - points.get(0).x, points.get(0).x+100 - points.get(0).x, points.get(0).x - points.get(0).x },
//                { points.get(0).y - points.get(0).y, points.get(0).y - points.get(0).y, points.get(0).y+200 - points.get(0).y },
//                { points.get(0).z+70 - points.get(0).z, points.get(0).z - points.get(0).z, points.get(0).z - points.get(0).z },
//        });

        // Geomagic -> World �ʒu���W�ϊ��s�񐶐�
        Matrix M_geo2world_euc = world_euc.product(geo_euc.solve(Matrix.identity(4)));

        // World -> Geomagic �x�N�g�����W�ϊ��s�񐶐�
        Matrix M_world2geo_vec = Matrix.create(new double[][]{
                {0.001,0.0,0.0},
                {0.0,0.001,0.0},
                {0.0,0.0,0.001}
        }).product(geo_vec.product(world_vec.solve(Matrix.identity(3))));


        // �t�@�C���o��
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

        // �t�@�C���o��
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

        // Liberty�T�[�o�ɐڑ�
        HapticsClient libertyClient = new HapticsClient();
        libertyClient.addListener(this);
        if (!libertyClient.connect(libertyServer, libertyPort, libertyDeviceNum)) {
            System.err.println("Failed to connect to libertyServer " + libertyServer + ":" + libertyPort);
            return;
        }
        serverNum = 1;

        // liberty�̍��W�擾�ҋ@
        System.out.println("Please push styluspen on 4points");
        while (wait) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        libertyClient.disconnect();

        // Liberty�̕ϊ��s��𐶐�
        Matrix lib_euc = Matrix.create(new double[][]{
                { points.get(0).x, points.get(1).x, points.get(2).x, points.get(3).x },
                { points.get(0).y, points.get(1).y, points.get(2).y, points.get(3).y },
                { points.get(0).z, points.get(1).z, points.get(2).z, points.get(3).z },
                {             1.0,             1.0,             1.0,             1.0 }
        });

        // ��L�ł����ꍇ
//        Matrix lib_euc = Matrix.create(new double[][]{
//                { points.get(0).x, points.get(0).x, points.get(0).x-10, points.get(0).x },
//                { points.get(0).y, points.get(0).y, points.get(0).y, points.get(0).y+20 },
//                { points.get(0).z, points.get(0).z-7, points.get(0).z, points.get(0).z },
//                {             1.0,             1.0,             1.0,             1.0 }
//        });

        // Liberty -> World �̈ʒu���W�n�ϊ��s�񐶐�
        Matrix M_lib2world_euc = world_euc.product(lib_euc.solve(Matrix.identity(4)));

        // �t�@�C���o��
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
