package jp.sagalab.model;

import jp.sagalab.jftk.Vector;

/**
 * 力覚を更新する
 */
public interface ForceUpdater {
    /** 力覚更新リクエスト */
    void requestUpdate(Vector force);
}
