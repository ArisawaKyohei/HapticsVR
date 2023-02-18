/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.sagalab.jftk.fragmentation.ellipticalarcs;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import jp.sagalab.jftk.curve.SplineCurve;
import jp.sagalab.jftk.fragmentation.BestDivideParameterSearchAlgorithm;
import jp.sagalab.jftk.fragmentation.IdentificationFragment;

/**
 * BestDivideParameterSearchAlgorithmの高速な実装.
 * @author ito tomohiko
 */
public class EllipticalArcsSegmentation implements BestDivideParameterSearchAlgorithm{

	public static class NarrowedInterval{
		public final Integer first;
		public final Integer last;

		public NarrowedInterval( Integer first, Integer last ) {
			this.first = first;
			this.last = last;
		}
	}

	/**
	 * 始終点を含まない
	 * @param _identificationFragment
	 * @param _fsc
	 * @param _searchParameters
	 * @return 
	 */
	@Override
	public Double[] getBestDevidedParameters( IdentificationFragment _identificationFragment, SplineCurve _fsc, double[] _searchParameters ) {
		List<Double> ts = DoubleStream.of(_searchParameters).boxed().collect(Collectors.toList());
		final int n = _searchParameters.length;
		MemoizingEllipticIdentifier identifier = new MemoizingEllipticIdentifier(ts, _fsc );
		List<NarrowedInterval> narrowedIntervals = narrow(n, identifier);
				
		List<Answer> answerTable = new ArrayList<>(n);
		answerTable.add(new Answer(1.0, PartitionParameters.empty()));
		
		int indexNarrowed = 0;
		for(int j = 1; j < n; ++j){
			if(narrowedIntervals.get(indexNarrowed + 1).first == j){
				++indexNarrowed;
			}

			if(j <= narrowedIntervals.get(indexNarrowed).last){
				Integer k = argMaxBisection(narrowedIntervals.get(indexNarrowed - 1).first, narrowedIntervals.get(indexNarrowed - 1).last, j, answerTable, identifier);
				if(k < narrowedIntervals.get(indexNarrowed - 1).first || narrowedIntervals.get(indexNarrowed - 1).last < k)
					throw new IllegalStateException(k + " is out of [" + narrowedIntervals.get(indexNarrowed - 1).first + ", " + narrowedIntervals.get(indexNarrowed - 1).last + "]");
				
				Double mu = Math.min(answerTable.get(k).getEllipseArcsGrade(), identifier.identify(k, j).getGradeElliptic());
				PartitionParameters p = PartitionParameters.add(answerTable.get(k).getPartitionParameters(), k);
				answerTable.add(new Answer(mu, p));
			}
			else {
				answerTable.add(null);
			}
		}			

		return answerTable.get(n-1).getPartitionParameters().getPartitionParameterList().stream().map(ts::get).skip(1).toArray(Double[]::new);
	}
	
	private static Integer argMaxBisection(Integer plFirst, Integer plLast, Integer j, List<Answer> answerTable, MemoizingEllipticIdentifier recognize){
		Integer a = plFirst, b = plLast;
		Function<Integer, Double> calcMuj = (Integer k)->{
			
			if(k < plFirst || plLast < k){
				return 0.0;//Double.NEGATIVE_INFINITY;
			}
			if(answerTable.get(k) == null)
				throw new NullPointerException("answers_"+k+" = null");
			return recognize.identify(k, j).isElliptic() ?
				Math.min(recognize.identify(k, j).getGradeElliptic(), answerTable.get(k).getEllipseArcsGrade()) : Double.NEGATIVE_INFINITY;
		};
		
		while(true){
			if(Objects.equals(a, b)){
				return a;
			}
			final int k = a + (int)Math.ceil((b - a)/2.0);
			if(!recognize.identify(k, j).isElliptic()){
				a = k + 1;
			}
			else if(calcMuj.apply(k) < calcMuj.apply(k + 1)){
				a = k + 1;
			}
			else if(calcMuj.apply(k) <= calcMuj.apply(k - 1)){
				b = k - 1;
			}
			else {
				a = b = k;
			}
		}
	}

	private static List<Integer> leftGreedyP(Integer n, MemoizingEllipticIdentifier recognizer){
		LinkedList<Integer> ls = new LinkedList<>();
		ls.addLast(0);
		for(int i = 0; i < n; ++i){
			if(i == n - 1){
				ls.addLast(n - 1);
			}
			else if(!recognizer.identify(ls.getLast(), i + 1).isElliptic()){
				ls.addLast(i);
			}
		}
		return Collections.unmodifiableList(new ArrayList<>(ls));
	}

	/**
	 * pLが求められた後はpL_{i-1} < pR_i <= pL_iが成り立つはずなのでこれを利用して，効率よく安定してpRを探索．
	 * @param recognizer
	 * @param ls
	 * @return
	 */
	private static List<Integer> rightGreedyP(MemoizingEllipticIdentifier recognizer, List<Integer> ls){
		final int m = ls.size() - 1;
		LinkedList<Integer> rs = new LinkedList<>();
		rs.addFirst(ls.get(m));
		for(int indexPl = m-1; indexPl > 0; --indexPl ){
			int rip1 = rs.getFirst();
			int ri = ls.get(indexPl-1) + 1;
			for(int r = ls.get(indexPl); r > ls.get(indexPl-1); --r){
				if(!recognizer.identify(r, rip1).isElliptic()){
					ri = r + 1;
					break;
				}
			}
			rs.addFirst(ri);
		}
		rs.addFirst(0);
		
		return Collections.unmodifiableList(new ArrayList<>(rs));
	}
	
	private static List<Integer> rightGreedyP(Integer n, MemoizingEllipticIdentifier recognizer){
		LinkedList<Integer> rs = new LinkedList<>();
		rs.add(n - 1);
		for(int i = n - 1; i >= 0; --i){
			if(i == 0){
				rs.addFirst(0);
			}
			else if(!recognizer.identify(i - 1, rs.getFirst()).isElliptic()){
				rs.addFirst(i);
			}
		}
		return Collections.unmodifiableList(new ArrayList<>(rs));
	}

	/**
	 * pLが求められた後はpR_{i} <= pL_i < pR_{i+1}が成り立つはずなのでこれを利用して，効率よく安定してpLを探索．
	 * @param recognizer
	 * @param rs
	 * @return
	 */
	private static List<Integer> leftGreedyP(MemoizingEllipticIdentifier recognizer, List<Integer> rs){
		final int m = rs.size() - 1;
		LinkedList<Integer> ls = new LinkedList<>();
		ls.addLast(0);
		for(int indexPr = 1; indexPr < m; ++indexPr ){
			int lim1 = ls.getLast();
			int li = rs.get(indexPr+1) - 1;
			for(int l = rs.get(indexPr); l < rs.get(indexPr+1); ++l){
				if(!recognizer.identify(lim1, l).isElliptic()){
					li = l - 1;
					break;
				}
			}
			ls.addLast(li);
		}
		ls.addLast(rs.get(m));
		return Collections.unmodifiableList(new ArrayList<>(ls));
	}

	/**
	 * rsとlsのzipWith処理．
	 * @param ls
	 * @param rs
	 * @return
	 */
	private static List<NarrowedInterval> narrow(List<Integer> ls, List<Integer> rs){
		List<NarrowedInterval> ss = new ArrayList<>(rs.size());
		
		for(Iterator<Integer> litr = ls.iterator(), ritr = rs.iterator(); litr.hasNext() && ritr.hasNext(); ){
			int r = ritr.next();
			int l = litr.next();

			ss.add(new NarrowedInterval(r, l));
		}

		return Collections.unmodifiableList(ss);
	}

	/**
	 * 片側だけからの絞り込みでは稀に安定しないので両側から絞り込んで，よかった方を選択する．
	 * @param n
	 * @param identifier
	 * @return
	 */
	private static List<NarrowedInterval> narrow(Integer n, MemoizingEllipticIdentifier identifier){
		List<Integer> ls = leftGreedyP(n, identifier);
		List<Integer> rs = rightGreedyP(identifier, ls);
		List<NarrowedInterval> ss = narrow(ls, rs);
		
		List<Integer> _rs = rightGreedyP(n, identifier);
		List<Integer> _ls = leftGreedyP(identifier, _rs);
		
		List<NarrowedInterval> _ss = narrow(_ls, _rs);

		return ss.size() < _ss.size() ? ss : _ss;
	}
}
