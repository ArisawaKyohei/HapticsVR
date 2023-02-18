# Haptics Client

Haptics Serverに対応したクライアントです。

## 開発環境

このプログラムは一部kotlinで書かれています。推奨IDEは IntelliJ です。
このプログラムはWindows, Mac, Linuxで動作します。 

## 依存関係

- JOGL

  ３次元描画を行うためにJOGLを使用しています。JOGLの2020年9月現在の[最終リリース](https://jogamp.org/deployment/jogamp-current/jar/)は2015年10月で最新の環境に対応していません。有志による開発は[次期バージョン](https://jogamp.org/deployment/jogamp-next/jar/)が存在します。こちらを使用することで、最新の環境でもJOGLを動かすことができます。`./lib/JOGL`以下には2020年3月ビルドのjarファイルが入っています。

- JFTK

  力を計算するメソッドをJFTKに実装しました。
 
## クイックスタート

1. [IntelliJ](https://www.jetbrains.com/ja-jp/idea/download/)をダウンロード。
1. Gradleプロジェクトとして開く。
1. `jp.sagalab.Main`内のserverNameおよびportを適宜変更。
1. `jp.sagalab.Main`を実行。

## その他

- 通信部に関して室蘭工業大学VRシステムのデバイスサーバプロトコルにしたがっているので、その他のデバイスサーバへの接続も可能。（デバイス番号:0）
