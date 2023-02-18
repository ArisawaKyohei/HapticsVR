package jp.sagalab.jftk.fragmentation;

import jp.sagalab.jftk.Sigmoid;
import jp.sagalab.jftk.curve.Range;
import jp.sagalab.jftk.curve.SplineCurve;
import jp.sagalab.jftk.fragmentation.ellipticalarcs.EllipticalArcsSegmentation;
import jp.sagalab.jftk.recognition.Recognizable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 探索区間数を設定した楕円弧幾何曲線列化による最良分割点を用いてファジィフラグメンテーション法を行います。<br>
 * このアルゴリズムでは探索区間数を設定し，その区間毎に楕円弧幾何曲線列化を探索することで，
 * 処理速度の改善を図った楕円弧幾何曲線列化手法です．
 *
 * @author aburaya
 * @author ito
 * @see "2016年 伊藤友彦 卒業論文「インタラクティブな手書き幾何作図における自由曲線整形のための高速な楕円弧幾何曲線列化アルゴリズム」"
 * @see "修士論文「手書き自由曲線の楕円弧幾何曲線列に関する研究」4章 油谷 凜"
 */
public class NonPartitionFragmentation implements BestDivideParameterSearchAlgorithm, FuzzyFragmentation {

  @Override
  public Fragment[] createFragment(SplineCurve _splineCurve) {
    //同定フラグメントの生成
    IdentificationFragment identificationFragment = IdentificationFragment.create(_splineCurve);
    // FSCの探索点の導出
    double[] searchParameters = calcSearchParameters(_splineCurve, MOVE_PARAM);

    // 最良分割パラメータ列を探索（探索アルゴリズムは各自選択してください）
    //Double[] separateParameters = getBestDevidedParameters(identificationFragment, _splineCurve, searchParameters);

    List<Double> separateParameters = new ArrayList<>(Arrays.asList(getBestDevidedParameters(identificationFragment, _splineCurve, searchParameters)));
    separateParameters.add(0, searchParameters[0]);
    separateParameters.add(searchParameters[searchParameters.length-1]);
    Fragment[] fragments = new Fragment[separateParameters.size() - 1];
    for (int i = 0; i < separateParameters.size()-1; i++) {
      fragments[i] = IdentificationFragment.create(_splineCurve.part(Range.create(separateParameters.get(i), separateParameters.get(i+1))));
    }


/*
    // 分割対象のfsc
    SplineCurve targetFSC = _splineCurve;

    //フラグメント列
    Fragment[] fragments = new Fragment[separateParameters.length + 1];

    // 複数分割の場合
    for (int i = 0; i < separateParameters.length; ++i) {
      // 分割対象のFSCを最良分割位置で二分
      double parameter = separateParameters[i];

      // フラグメント列に入れる
      fragments[i] = createFragments(parameter, targetFSC)[0];

      // targetFSCの更新
      targetFSC = createFragments(parameter, targetFSC)[1].curve();
      if (i == separateParameters.length - 1) {
        fragments[separateParameters.length] = createFragments(parameter, targetFSC)[1];
      }
    }
    */


    if (fragments[0] == null) {
      System.out.println("");
    }
    return fragments;
  }

  @Override
  public SplineCurve[] divide(SplineCurve _splineCurve) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  /**
   * このクラスのインスタンスを生成します。
   *
   * @param _searchInterval 探索区間数
   * @param _rule           推論ルール
   * @param _recognizer     幾何曲線認識法のストラテジー
   */
  public NonPartitionFragmentation(int _searchInterval, Map<String, Sigmoid> _rule, Recognizable _recognizer) {
  }

  /**
   * 最良分割点を返します．
   *
   * @param _identificationFragment     同定フラグメント
   * @param _fsc                        FSC
   * @param _searchEvaluationParameters 探索候補点
   * @return 最良分割点列
   */
  @Override
  public Double[] getBestDevidedParameters(IdentificationFragment _identificationFragment, SplineCurve _fsc, double[] _searchEvaluationParameters) {
    return new EllipticalArcsSegmentation().getBestDevidedParameters(
        _identificationFragment,
        _fsc,
        _searchEvaluationParameters
    );
  }

  /**
   * 指定されたパラメータでFSCをフラグメント列化する（指定パラメータでばっつり分割）。
   * XXX 楕円弧幾何曲線列化に用います。
   *
   * @param _splineCurve       ファジィスプライン曲線
   * @param _divisionParameter 分割位置のパラメータ
   * @return フラグメント列（同定フラグメント）
   */
  private Fragment[] createFragments(double _divisionParameter, SplineCurve _splineCurve) {
    Fragment[] fragments = new Fragment[2];
    // 同定フラグメント(Left)を作る-------------
    Range range = Range.create(_splineCurve.range().start(), _divisionParameter);
    fragments[0] = IdentificationFragment.create(_splineCurve.part(range));

    // 同定フラグメント(Right)を作る-------------
    range = Range.create(_divisionParameter, _splineCurve.range().end());
    fragments[1] = IdentificationFragment.create(_splineCurve.part(range));

    return fragments;
  }

  /**
   * FSCの等時間間隔の探索パラメータ列の生成。
   * （探索時の誤差をなくすため、内分により求める）
   *
   * @param _targetFSC      探索対象のFSC
   * @param _movementAmount 探索点の時間間隔(秒)
   * @return 探索パラメータ列
   */
  private static double[] calcSearchParameters(SplineCurve _targetFSC, double _movementAmount) {
    Range targetRange = _targetFSC.range();
    int num = Math.max((int) Math.round(targetRange.length() / _movementAmount), 1);
    double[] dividedParameters = new double[num + 1];
    double targetRangeStart = targetRange.start();
    double targetRangeEnd = targetRange.end();

    for (int i = 0; i < dividedParameters.length; ++i) {
      double t = i / (double) num;
      // 探索点格納
      dividedParameters[i] = (1 - t) * targetRangeStart + t * targetRangeEnd;
    }

    return dividedParameters;
  }

  /**
   * 探索移動量Δt(秒)
   */
  private static final double MOVE_PARAM = 0.1;

}

