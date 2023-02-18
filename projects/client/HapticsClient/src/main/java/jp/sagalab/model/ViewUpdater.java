package jp.sagalab.model;

import java.awt.*;

/**
 * モデルから再描画をリクエストするインタフェース.
 */
public interface ViewUpdater {

    /**
     * Viewを取得する
     */
    Component getView();

    /**
     * 再描画リクエスト
     */
    void requestRepaint();
}