package jp.sagalab.jftk.curve;

/**
 * 向きを反転させるために必要なインタフェースです。
 *
 * @param <T> 反転させるオブジェクト
 * @author miwa
 */
public interface Invertible<T> {

  /**
   * このオブジェクトの向きを反転させます。
   *
   * @return 反転したオブジェクト
   */
  public T invert();
}
