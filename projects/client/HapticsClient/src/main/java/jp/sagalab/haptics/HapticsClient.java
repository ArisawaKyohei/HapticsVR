package jp.sagalab.haptics;

import jp.sagalab.jftk.Matrix;
import jp.sagalab.jftk.transform.TransformMatrix;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class HapticsClient implements Runnable {

    private Socket socket;
    private DataOutputStream output;
    private DataInputStream input;
    private Thread receiveThread;
    private final List<EventListener> listener = new ArrayList<>();
    private final Object lock = new Object();

    /**
     * 接続状態を返す。
     * @return 接続状態
     */
    public boolean isConnected() {
        return socket != null;
    }

    /**
     * 力設定イベントを送信する。
     * @param fx x軸方向の力
     * @param fy y軸方向の力
     * @param fz z軸方向の力
     */
    public void sendForceEvent(double fx, double fy, double fz) {
        synchronized (lock) {
            try {
                output.write(toBytes(5, fx, fy, fz, System.nanoTime()));
            } catch (IOException e) {
                // 送信に失敗したらソケットを閉じる
                disconnect();
            }
        }
    }

    /**
     * イベントリスナを登録する。
     * @param l リスナ
     */
    public void addListener(EventListener l) {
        listener.add(l);
    }

    /**
     * イベントリスナを解除する。
     * @param l 　リスナ
     */
    public void removeListener(EventListener l) {
        listener.remove(l);
    }

    /**
     * サーバに接続する。
     * @param server    IPアドレス
     * @param port      ポート番号
     * @param deviceNum デバイス番号
     * @return 接続成否
     */
    public boolean connect(String server, int port, int deviceNum) {
        disconnect();
        try {
            this.socket = new Socket(server, port);
            this.output = new DataOutputStream((socket.getOutputStream()));
            this.input = new DataInputStream(socket.getInputStream());
            // Device Select Data
            output.writeByte(deviceNum);
            receiveThread = new Thread(this);
            receiveThread.start();
        } catch (IOException e) {
            e.printStackTrace();
            this.socket = null;
            return false;
        }
        return true;
    }

    public boolean connect(String server, int port) {
        return connect(server, port, 0);
    }

    /**
     * サーバとの接続を切断する。
     */
    public void disconnect() {
        // 受信スレッドを停止する
        if (receiveThread != null) {
            receiveThread.interrupt();
            try {
                receiveThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                receiveThread = null;
            }
        }
        // ソケットを切断する
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                socket = null;
                output = null;
                input = null;
            }
            listener.forEach(EventListener::disconnected);
        }
    }

    /**
     * 変換行列を設定ファイルから読み出す。
     * @return 変換行列
     */
//    public TransformMatrix getMatrix() {
//        ArrayList<double[]> elements = new ArrayList<>();
//        try {
//            Scanner scanner = new Scanner(new File("geo2Lib"));
//            while (scanner.hasNext()) {
//                String line = scanner.next();
//                if (line.isEmpty()) continue;
//                String[] split = line.split(",");
//                if (split.length != 4) {
//                    System.err.println("invalid column size. " + split.length);
//                    return TransformMatrix.identity();
//                }
//                elements.add(new double[]{
//                        Double.parseDouble(split[0]),
//                        Double.parseDouble(split[1]),
//                        Double.parseDouble(split[2]),
//                        Double.parseDouble(split[3])
//                });
//            }
//        } catch (FileNotFoundException e) {
//            System.err.println(e.getMessage());
//            return TransformMatrix.identity();
//        }
//        if (elements.size() != 4) {
//            System.err.println("invalid row size. " + elements.size());
//            return TransformMatrix.identity();
//        }
//        return TransformMatrix.create(new double[][]{
//                elements.get(0),
//                elements.get(1),
//                elements.get(2),
//                elements.get(3)
//        });
//    }

    public TransformMatrix getGeo2WorldMatrix() {
        ArrayList<double[]> elements = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("geo2world_euc"));
            while (scanner.hasNext()) {
                String line = scanner.next();
                if (line.isEmpty()) continue;
                String[] split = line.split(",");
                if (split.length != 4) {
                    System.err.println("invalid column size. " + split.length);
                    return TransformMatrix.identity();
                }
                elements.add(new double[]{
                        Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2]),
                        Double.parseDouble(split[3])
                });
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return TransformMatrix.identity();
        }
        if (elements.size() != 4) {
            System.err.println("invalid row size. " + elements.size());
            return TransformMatrix.identity();
        }
        return TransformMatrix.create(new double[][]{
                elements.get(0),
                elements.get(1),
                elements.get(2),
                elements.get(3)
        });
    }

    public static Matrix getLib2WorldMatrix() {
        ArrayList<double[]> elements = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("lib2world_euc"));
            while (scanner.hasNext()) {
                String line = scanner.next();
                if (line.isEmpty()) continue;
                String[] split = line.split(",");
                if (split.length != 4) {
                    System.err.println("invalid column size. " + split.length);
                    return Matrix.identity(4);
                }
                elements.add(new double[]{
                        Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2]),
                        Double.parseDouble(split[3])
                });
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return Matrix.identity(4);
        }
        if (elements.size() != 4) {
            System.err.println("invalid row size. " + elements.size());
            return Matrix.identity(4);
        }
        return Matrix.create(new double[][]{
                elements.get(0),
                elements.get(1),
                elements.get(2),
                elements.get(3)
        });
    }

    public static Matrix getWorld2GeoMatrix() {
        ArrayList<double[]> elements = new ArrayList<>();
        try {
            Scanner scanner = new Scanner(new File("world2geo_vec"));
            while (scanner.hasNext()) {
                String line = scanner.next();
                if (line.isEmpty()) continue;
                String[] split = line.split(",");
                if (split.length != 3) {
                    System.err.println("invalid column size. " + split.length);
                    return Matrix.identity(3);
                }
                elements.add(new double[]{
                        Double.parseDouble(split[0]),
                        Double.parseDouble(split[1]),
                        Double.parseDouble(split[2]),
                });
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
            return Matrix.identity(3);
        }
        if (elements.size() != 3) {
            System.err.println("invalid row size. " + elements.size());
            return Matrix.identity(3);
        }
        return Matrix.create(new double[][]{
                elements.get(0),
                elements.get(1),
                elements.get(2)
        });
    }

    @Override
    public void run() {
        while (!receiveThread.isInterrupted() && socket != null && !socket.isClosed()) {
            try {
                int type = input.readUnsignedByte();
                switch (type) {
                    case 0: // PressEvent
                        handlePressedEvent(input);
                        break;
                    case 1: // ReleaseEvent
                        handleReleasedEvent(input);
                        break;
                    case 2: // MoveEvent
                        handleMovedEvent(input);
                        break;
                    case 3: // SwayEvent
                        handleSwayedEvent(input);
                        break;
                    case 5: // ForceEvent
                        handleForceEvent(input);
                        break;
                    default:
                        System.out.println("Unknown type: " + type);
                }
            } catch (IOException e) {
                // 受信に失敗したらソケットを閉じる
                disconnect();
            }
        }
    }

    private void handlePressedEvent(DataInputStream stream) throws IOException {
        final int button = stream.readUnsignedByte();
        final long time = stream.readLong();
        listener.forEach((l) -> l.onPressed(button, time));
    }

    private void handleReleasedEvent(DataInputStream stream) throws IOException {
        final int button = stream.readUnsignedByte();
        final long time = stream.readLong();
        listener.forEach((l) -> l.onReleased(button, time));
    }

    private void handleMovedEvent(DataInputStream stream) throws IOException {
        final double x = stream.readDouble();
        final double y = stream.readDouble();
        final double z = stream.readDouble();
        final long time = stream.readLong();
        //System.out.println("x: "+x+" y: "+y+" z: "+z);
        listener.forEach((l) -> l.position(x, y, z, time));
    }

    private void handleSwayedEvent(DataInputStream stream) throws IOException {
        final double x = stream.readDouble();
        final double y = stream.readDouble();
        final double z = stream.readDouble();
        final long time = stream.readLong();
        listener.forEach((l) -> l.posture(x, y, z, time));
    }

    private void handleForceEvent(DataInputStream stream) throws IOException {
        final double x = stream.readDouble();
        final double y = stream.readDouble();
        final double z = stream.readDouble();
        final long time = stream.readLong();
        listener.forEach((l) -> l.force(x, y, z, time));
    }

    private byte[] toBytes(int id, double x, double y, double z, long t) {
        return (byte[]) ((ByteBuffer)ByteBuffer.allocate(33).clear())
                .put((byte) id).putDouble(x).putDouble(y).putDouble(z).putLong(t)
                .flip().array();
    }
}
