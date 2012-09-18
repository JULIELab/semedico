package de.julielab.semedico.core;

import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

public class QueryToken implements Comparable<QueryToken>{

	private int beginOffset;
	private int endOffset;
	private String value;
	private IFacetTerm term;
	private String originalValue;
	private double score;
	
	public QueryToken(int beginOffset, int endOffset, String value) {
		this.beginOffset = beginOffset;
		this.endOffset = endOffset;
		this.value = value;
	}
	
	public int getBeginOffset() {
		return beginOffset;
	}
	public void setBeginOffset(int beginOffset) {
		this.beginOffset = beginOffset;
	}
	public int getEndOffset() {
		return endOffset;
	}
	public void setEndOffset(int endOffset) {
		this.endOffset = endOffset;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public IFacetTerm getTerm() {
		return term;
	}
	public void setTerm(IFacetTerm term) {
		this.term = term;
	}

	public int compareTo(QueryToken token) {

		return beginOffset - token.beginOffset;
	}

	public String getOriginalValue() {
		return this.originalValue;
	}

	public void setOriginalValue(String originalValue) {
		this.originalValue = originalValue;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}
}
