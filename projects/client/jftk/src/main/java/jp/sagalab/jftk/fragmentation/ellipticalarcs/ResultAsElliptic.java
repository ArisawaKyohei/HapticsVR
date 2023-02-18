/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jp.sagalab.jftk.fragmentation.ellipticalarcs;

/**
 * 楕円弧幾何曲線同定の結果を保持する.
 * 楕円弧幾何曲線として認識されたかどうかと楕円弧幾何曲線であるグレードを保持する.
 */
public class ResultAsElliptic{
	public Boolean isElliptic(){
		return isElliptic;
	}
	public Double getGradeElliptic(){
		return grade;
	}

	private final Boolean isElliptic;
	private final Double grade;

	public ResultAsElliptic(Boolean isEllipseArc, Double grade) {
		this.isElliptic = isEllipseArc;
		this.grade = grade;
	}
} 
