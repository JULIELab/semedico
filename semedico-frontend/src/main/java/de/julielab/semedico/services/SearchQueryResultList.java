package de.julielab.semedico.services;


import java.util.ArrayList;
import java.util.List;

import de.julielab.semedico.core.services.SortCriterium;
import de.julielab.semedico.core.services.BibliographyEntry;

/**
 * This class prepares the data for the REST-API for Bexis to convert java data with Gson into json.
 * 
 * @author lohr
 * 
 * December 2016
 */

public class SearchQueryResultList 
{
	private String inputstring;
	private List <String> tokens;
	private SortCriterium sortcriterium;
	private int subsetstart;
	private int subsetsize;
	private long countallresults;
	
	private List <BibliographyEntry> bibliographylist = new ArrayList <>();
	
	private String error;

	
	public SearchQueryResultList(
				)
	{
		this.setInputstring(inputstring);
		this.setTokens(tokens);
		this.setSortcriterium(sortcriterium);
		this.setSubsetstart(subsetstart);
		this.setSubsetend(subsetsize);
		this.setCountallresults(countallresults);
		this.setBibliographylist(bibliographylist);
		this.setError(error);
	}
	
	public String getInputstring()
	{
		return inputstring;
	}
	public void setInputstring(String inputstring)
	{
		this.inputstring = inputstring;
	}

	
	public List<String> getTokens() 
	{
		return tokens;
	}
	public void setTokens(List<String> tokens)
	{
		this.tokens = tokens;
	}

	
	public SortCriterium getSortcriterium() //TODO
	{
		return sortcriterium;
	}
	public void setSortcriterium(SortCriterium sortcriterium)
	{
		this.sortcriterium = sortcriterium;
	}


	public int getSubsetstart()
	{
		return subsetstart;
	}
	public void setSubsetstart(int subsetstart)
	{
		this.subsetstart = subsetstart;
	}

	
	public int getSubsetend()
	{
		return subsetsize;
	}
	public void setSubsetend(int subsetend)
	{
		this.subsetsize = subsetend;
	}
	
	
	public long getCountallresults()
	{
		return countallresults;
	}
	public void setCountallresults(long countallresults)
	{
		this.countallresults = countallresults;
	}

	
	public List <BibliographyEntry> getBibliographylist()
	{
		return bibliographylist;
	}
	public void setBibliographylist(List <BibliographyEntry> bibliographylist)
	{
		this.bibliographylist = bibliographylist;
	}

	public String getError()
	{
		return error;
	}
	public void setError(String error)
	{
		this.error = error;
	}
	
}