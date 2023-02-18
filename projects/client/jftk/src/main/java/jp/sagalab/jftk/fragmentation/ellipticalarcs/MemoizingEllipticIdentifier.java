/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.sagalab.jftk.fragmentation.ellipticalarcs;

import java.util.List;
import java.util.Map;
import jp.sagalab.jftk.curve.Range;
import jp.sagalab.jftk.curve.SplineCurve;
import jp.sagalab.jftk.fragmentation.IdentificationFragment;
import jp.sagalab.jftk.recognition.OpenRecognizer;
import jp.sagalab.jftk.recognition.PrimitiveType;
import static jp.sagalab.jftk.recognition.PrimitiveType.CLOSED_FREE_CURVE;
import static jp.sagalab.jftk.recognition.PrimitiveType.OPEN_FREE_CURVE;
import jp.sagalab.jftk.recognition.RecognitionResult;
import jp.sagalab.jftk.recognition.Recognizable;

/**
 * 楕円弧幾何曲線同定を行う.
 * OpenRecognizerのラッパクラス．
 * 特定のFSCの部分FSCに対して，何度も同定処理を繰り返す場合に，メモ化することで効率を高めている
 * 部分FSCの始終点を探索点の添字を指定することによって，部分FSCの楕円弧幾何曲線同定を行うことができる．
 * @author ito tomohiko
 */
public class MemoizingEllipticIdentifier {
	private final ResultAsElliptic[][] table;
	private final List<Double> ts;
	private final SplineCurve fsc;
	private static final Recognizable recognizer = new OpenRecognizer();//new EllipticRecognizer(Experiment.rule, new OpenRecognizer());

	public MemoizingEllipticIdentifier(List<Double> ts, SplineCurve fsc) {
		this.table = new ResultAsElliptic[ts.size()][ts.size()];

		this.ts = ts;
		this.fsc = fsc;
	}

	public static ResultAsElliptic identify(SplineCurve fsc){
		RecognitionResult result = recognizer.recognize(IdentificationFragment.create(fsc), fsc, null);
		PrimitiveType type = result.getType();
		Map<PrimitiveType, Double> grades = result.getGradeList();
		return new ResultAsElliptic(
			!(type.equals(CLOSED_FREE_CURVE) || type.equals(OPEN_FREE_CURVE)),
			1.0-Math.max(grades.getOrDefault(CLOSED_FREE_CURVE, 0.0), grades.get(OPEN_FREE_CURVE)));
	}
	
	public ResultAsElliptic identify(Integer i, Integer j){
		SplineCurve sub = fsc.part(Range.create(ts.get(i), ts.get(j)));
		return (table[i][j] == null) ? (table[i][j] = identify(sub)) : table[i][j];
	}
}
