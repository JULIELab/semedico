package de.julielab.semedico.core.services;

import de.julielab.elastic.query.SortCriterium;

import java.util.ArrayList;
import java.util.List;

//TODO IN ENTWICKLUNG

/**
* 
* @author lohr
*
*/

public class SearchQueryResultList
{
	private String inputstring;
	private List <String> tokens;
	private SortCriterium sortcriterium;
	private String subsetsize;
	private String subset;
	private String countallresults;	
	
	private List <BibliographyEntry> reusltlistbibliography = new ArrayList <BibliographyEntry>();
	
	public SearchQueryResultList(
			String inputstring,
			List<String> tokens,
			SortCriterium sortcriterium,
			String subsetsize,
			String subset,
			String countallresults,
			List<BibliographyEntry> bibliography
				)
	{
		this.setInputstring(inputstring);
		this.setTokens(tokens);
		this.setSortcriterium(sortcriterium);
		this.setSubsetsize(subsetsize);
		this.setSubset(subset);
		this.setCountallresults(countallresults);
		this.setReusltlistbibliography((List<BibliographyEntry>) bibliography);
	}
	
	public SearchQueryResultList(){}
	
	public String getInputstring()
	{
		return inputstring;
	}
	public void setInputstring(String inputstring)
	{
		this.inputstring = inputstring;
	}

	
	public List <String> getTokens() 
	{
		return tokens;
	}
	public void setTokens(List<String> tokens)
	{
		this.tokens = tokens;
	}

	
	public SortCriterium getSortcriterium()
	{
		return sortcriterium;
	}
	public void setSortcriterium(SortCriterium sortcriterium)
	{
		this.sortcriterium = sortcriterium;
	}

	
	public String getSubsetsize()
	{
		return subsetsize;
	}
	public void setSubsetsize(String subsetsize)
	{
		this.subsetsize = subsetsize;
	}

	
	public String getSubset()
	{
		return subset;
	}
	public void setSubset(String subset)
	{
		this.subset = subset;
	}

	
	public String getCountallresults()
	{
		return countallresults;
	}
	public void setCountallresults(String countallresults)
	{
		this.countallresults = countallresults;
	}

	
	public List <BibliographyEntry> getReusltlistbibliography()
	{
		return reusltlistbibliography;
	}
	public void setReusltlistbibliography(List <BibliographyEntry> reusltlistbibliography)
	{
		this.reusltlistbibliography = reusltlistbibliography;
	}

}