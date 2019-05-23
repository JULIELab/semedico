package de.julielab.semedico.core.services;

import java.util.List;

import de.julielab.semedico.core.services.SortCriterium;

/**
* 
* @author lohr
*
*/

public class SearchQueryResultList {
	private String inputstring;
	private List <String> tokens;
	private SortCriterium sortcriterium;
	private String subsetsize;
	private String subset;
	private String countallresults;	
	
	List <BibliographyEntry> resultlistbibliography;
	
	public SearchQueryResultList(
			String inputstring,
			List<String> tokens,
			SortCriterium sortcriterium,
			String subsetsize,
			String subset,
			String countallresults,
			List<BibliographyEntry> bibliography) {
		this.setInputstring(inputstring);
		this.setTokens(tokens);
		this.setSortcriterium(sortcriterium);
		this.setSubsetsize(subsetsize);
		this.setSubset(subset);
		this.setCountallresults(countallresults);
		this.setResultlistbibliography(bibliography);
	}
	
	public SearchQueryResultList(){}
	
	public String getInputstring() {
		return inputstring;
	}
	
	public void setInputstring(String inputstring) {
		this.inputstring = inputstring;
	}

	public List <String> getTokens() {
		return tokens;
	}
	
	public void setTokens(List<String> tokens) {
		this.tokens = tokens;
	}

	public SortCriterium getSortcriterium() {
		return sortcriterium;
	}
	
	public void setSortcriterium(SortCriterium sortcriterium) {
		this.sortcriterium = sortcriterium;
	}

	public String getSubsetsize() {
		return subsetsize;
	}
	
	public void setSubsetsize(String subsetsize) {
		this.subsetsize = subsetsize;
	}

	public String getSubset() {
		return subset;
	}
	
	public void setSubset(String subset) {
		this.subset = subset;
	}

	
	public String getCountallresults() {
		return countallresults;
	}
	
	public void setCountallresults(String countallresults) {
		this.countallresults = countallresults;
	}

	
	public List <BibliographyEntry> getResultlistbibliography() {
		return resultlistbibliography;
	}
	public void setResultlistbibliography(List <BibliographyEntry> reusltlistbibliography) {
		this.resultlistbibliography = reusltlistbibliography;
	}

}