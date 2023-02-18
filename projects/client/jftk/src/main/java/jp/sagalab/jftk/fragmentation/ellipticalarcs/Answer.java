/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.sagalab.jftk.fragmentation.ellipticalarcs;

/**
 * 分割点時刻の添字の列とその添字列に対する楕円弧幾何曲線列性のペア.
 * 高速な楕円弧幾何曲線列化の探索処理における部分解を保持するたのクラス．
 * @author ito tomohiko
 */
public class Answer{
	public Double getEllipseArcsGrade() {
		return m_ellipseArcGrade;
	}
	public PartitionParameters getPartitionParameters() {
		return m_partitionParameters;
	}

	private final Double m_ellipseArcGrade;

	private final PartitionParameters m_partitionParameters;

	public Answer( Double grade, PartitionParameters parameters ) {
		this.m_ellipseArcGrade = grade;
		this.m_partitionParameters = parameters;
	}
}
