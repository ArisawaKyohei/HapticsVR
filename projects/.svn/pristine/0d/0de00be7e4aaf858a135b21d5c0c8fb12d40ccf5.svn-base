/*
 * Coordinate.java
 * 地球上の位置に関する情報(緯度・経度・高度)を扱うデータクラス
 * Oct. 2017 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.pbl2018;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import jp.ac.muroran_it.csse.vr_skelton.Position;

public class Coordinate {
    /** 緯度 */
    private final double latitude;

    /** 経度 */
    private final double longitude;

    /** 高度 */
    private final double altitude;

    /**
     * 緯度と経度と高度を指定するコンストラクタ。
     * @param latitude 緯度(度)。
     * @param longitude 経度(度)。
     * @param altitude 高度(km)。
     */
    public Coordinate(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    /**
     * 緯度と経度を指定し、高度を省略するコンストラクタ。
     * @param latitude 緯度(度)。
     * @param longitude 経度(度)。
     */
    public Coordinate(double latitude, double longitude) {
        this(latitude,longitude,0.0);
    }

    /**
     * 緯度の取得。
     * @return 緯度(度)。
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * 経度の取得。
     * @return 経度(度)。
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * 高度の取得。
     * @return 高度(km)。
     */
    public double getAltitude() {
        return altitude;
    }

    /**
     * メルカトル図表上の座標値へ変換。
     * @return メルカトル図表上の座標値。
     */
    public Position toPosition() {
        // 仮想の地球の赤道長(cm単位で縮尺1:16,000,000）
        double equatorLength = 250.0;
        double equatorRadius = equatorLength / (2 * Math.PI);
        // 北緯0度、東経0度、高度0に対応する点
        Position basingPoint = new Position(-96.0, 30.0, 10.0);

        /*
         * x:東方向, y:上空方向, z:北方向
         * 上空方向のみ1,600,000倍に拡大
         */
        double x = basingPoint.getX() + Math.toRadians(longitude) * equatorRadius;
        double y = basingPoint.getY() + altitude * 0.1;
        double angle = Math.tan(Math.toRadians(latitude) / 2 + Math.PI / 4);
        double z = basingPoint.getZ() - Math.log(angle) * equatorRadius;

        // メルカトル図表上の座標値
        return new Position(x, y, z);
    }

    /**
     * 指定したデータファイルからの位置情報群の読み込み。
     * @param filename データファイル名。
     * @return 読み込んだ位置情報群。
     */
    public static Coordinate[] loadFile(String filename) {
        ArrayList<Coordinate> tmpCoordinates = new ArrayList<Coordinate>();
        Coordinate[] returnCoordinates = null;
        Scanner scanner = null;
        try {
            // ファイルを開いてスキャナーにセット
            File file = new File(filename);
            scanner = new Scanner(file);

            // ファイルの終わりまで読み込み
            while (scanner.hasNext()) {
                // 1行に3つの実数値(緯度,経度,高度(深度))が並んでいるので順番に読み取る
                double latitude = scanner.nextDouble();
                double longitude = scanner.nextDouble();
                double altitude = scanner.nextDouble();
                tmpCoordinates.add(new Coordinate(latitude, longitude, altitude));
            }

            // リストを配列に変換
            returnCoordinates = tmpCoordinates.toArray(new Coordinate[tmpCoordinates.size()]);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return new Coordinate[0];
        } finally {
            // スキャナを閉じる
            if (scanner != null) {
                scanner.close();
            }
        }
        return returnCoordinates;
    }
}
