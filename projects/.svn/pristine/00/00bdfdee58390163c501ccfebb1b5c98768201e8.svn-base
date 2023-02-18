/*
 * DeviceClient.java
 *
 * Oct. 2010 by Muroran Institute of Technology
 */
package jp.ac.muroran_it.csse.deviceclient;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * デバイスサーバに接続し、サーバから受信したデバイスイベントをリスナー群に配信するリモートデバイス。
 */
public class DeviceClient {
    /** ソケット */
    private Socket socket;

    /** リスナー群 */
    private CopyOnWriteArrayList<DeviceListener> listeners = new CopyOnWriteArrayList<DeviceListener>();

    /**
     * デバイスサーバへ接続。
     * @param serverName デバイスサーバのホスト名。
     * @param portNumber デバイスサーバのポート番号。
     * @param id デバイス番号。
     * @return デバイスサーバへ接続できた場合はtrue。
     */
    public boolean connect(String serverName, int portNumber, final int id) {
        // 既に接続済みであれば、先に切断しておく
        disconnect();

        try {
            // デバイスサーバに接続
            socket = new Socket(serverName, portNumber);

            // 受信用スレッドの開始
            Thread receiveTherad = new Thread() {
                @Override
                public void run() {
                    Socket socket = DeviceClient.this.socket;
                    try {
                        // デバイスIDの送信
                        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
                        output.writeByte(id);

                        // デバイス情報の受信
                        DataInputStream input = new DataInputStream(socket.getInputStream());
                        // ソケットが閉じられるまで受信し続ける
                        while (true) {
                            // イベントタイプの受信
                            int eventType = input.readUnsignedByte();
                            // イベントタイプで本体の受信を振り分け
                            DeviceEvent event = null;
                            switch (eventType) {
                                case 0:
                                    event = recievePressedEvent(input);  // デバイスプレスイベント
                                    break;
                                case 1:
                                    event = recieveReleasedEvent(input); // デバイスリリースイベント
                                    break;
                                case 2:
                                    event = recieveMovedEvent(input);    // デバイスムーブイベント
                                    break;
                                case 3:
                                    event = recieveSwayedEvent(input);   // デバイススウェイイベント
                                    break;
                            }
                            // イベントをリスナー群に配信
                            processDeviceEvent(event);
                        }
                    } catch (IOException e) {
                        Logger.getLogger(DeviceClient.class.getName()).log(Level.SEVERE, null, e);
                        // 入出力エラー
                        if (socket != null) {
                            try {
                                socket.close();
                            } catch (IOException ex) {
                                Logger.getLogger(DeviceClient.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    }
                }
            };
            receiveTherad.start();
        } catch (ConnectException e) {
            // 接続失敗
            Logger.getLogger(DeviceClient.class.getName()).log(Level.SEVERE, null, e);
            return false;
        } catch (UnknownHostException e) {
            // ホストが見付からない
            Logger.getLogger(DeviceClient.class.getName()).log(Level.SEVERE, null, e);
            return false;
        } catch (IOException e) {
            // 入出力エラー
            Logger.getLogger(DeviceClient.class.getName()).log(Level.SEVERE, null, e);
            return false;
        }

        return true;
    }

    /**
     * 指定した入力ストリームからのデバイスプレスイベントの受信。
     * @param input 入力ストリーム。
     * @return 受信したデバイスプレスイベント。
     * @throws IOException 入出力エラーが発生したとき。
     */
    private static DeviceEvent recievePressedEvent(DataInputStream input) throws IOException {
        int buttonID = input.readUnsignedByte();
        long time = input.readLong();
        return DeviceEvent.createPressedEvent(buttonID, time);
    }

    /**
     * 指定した入力ストリームからのデバイスリリースイベントの受信。
     * @param input 入力ストリーム。
     * @return 受信したデバイスリリースイベント。
     * @throws IOException 入出力エラーが発生したとき。
     */
    private static DeviceEvent recieveReleasedEvent(DataInputStream input) throws IOException {
        int buttonID = input.readUnsignedByte();
        long time = input.readLong();
        return DeviceEvent.createReleasedEvent(buttonID, time);
    }

    /**
     * 指定した入力ストリームからのデバイスムーブイベントの受信。
     * @param input 入力ストリーム。
     * @return 受信したデバイスムーブイベント。
     * @throws IOException 入出力エラーが発生したとき。
     */
    private static DeviceEvent recieveMovedEvent(DataInputStream input) throws IOException {
        double[] position = new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
        long time = input.readLong();
        return DeviceEvent.createMovedEvent(position, time);
    }

    /**
     * 指定した入力ストリームからのデバイススウェイイベントの受信。
     * @param input 入力ストリーム。
     * @return 受信したデバイススウェイイベント。
     * @throws IOException 入出力エラーが発生したとき。
     */
    private static DeviceEvent recieveSwayedEvent(DataInputStream input) throws IOException {
        double[] posture = new double[] {input.readDouble(), input.readDouble(), input.readDouble()};
        long time = input.readLong();
        return DeviceEvent.createSwayedEvent(posture, time);
    }

    /**
     * デバイスサーバとの接続の切断。
     */
    public void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(DeviceClient.class.getName()).log(Level.SEVERE, null, ex);
            }
            socket = null;
        }
    }

    /**
     * デバイスイベントリスナーの登録。
     * @param listener デバイスイベントリスナー。
     */
    public void addDeviceListener(DeviceListener listener) {
        // 登録済みかをチェック
        int index = listeners.indexOf(listener);
        if (index < 0) {
            // 既存のリスナーでなければ登録
            listeners.add(listener);
        }
    }

    /**
     * デバイスイベントリスナーの削除。
     * @param listener デバイスイベントリスナー。
     */
    public void removeDeviceListener(DeviceListener listener) {
        // リスナー群から削除
        listeners.remove(listener);
    }

    /**
     * デバイスサーバとの接続状態の取得。
     * @return 接続済みの場合はtrue。
     */
    public boolean isConnected() {
        return socket != null;
    }

    /**
     * デバイスイベントリスナー群へのデバイスイベントの配信。
     * @param event デバイスイベント。
     */
    protected void processDeviceEvent(DeviceEvent event) {
        if (event != null) {
            List<DeviceListener> listeners = new ArrayList<DeviceListener>(this.listeners);
            for (DeviceListener listener : listeners) {
                switch (event.getType()) {
                    case BUTTON_PRESSED:
                        listener.devicePressed(event);
                        break;
                    case BUTTON_RELEASED:
                        listener.deviceReleased(event);
                        break;
                    case SENSOR_MOVED:
                        listener.deviceMoved(event);
                        break;
                    case SENSOR_SWAYED:
                        listener.deviceSwayed(event);
                        break;
                    default:
                        System.out.println("Unknown event");
                        break;
                }
            }
        }
    }
}
