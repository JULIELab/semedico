/** 
 * DocumentHit.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.search.components.data;

import java.util.List;

public class HighlightedSemedicoDocument
{
	private SemedicoDocument document;
	@Deprecated
	private String kwicTitle;
	@Deprecated
	private String[] kwics;
	private Highlight titleHighlight;
	private List<Highlight> textContentHighlights;
	private List<AuthorHighlight> authorHighlights;
	private List<Highlight> meshMajorHighlights;
	private Highlight journalTitleHighlight;
	private Highlight journalVolumeHighlight;
	private Highlight journalIssueHighlight;
	private List<Highlight> affiliationHighlights;
	private List<Highlight> meshMinorHighlights;
	private List<Highlight> substancesHighlights;
	private List<Highlight> keywordHighlights;
	private Highlight highlightedAbstract;

	public Highlight getHighlightedAbstract()
	{
		return highlightedAbstract;
	}

	public Highlight getTitleHighlight()
	{
		return titleHighlight;
	}

	public void setTitleHighlight(Highlight titleHighlight)
	{
		this.titleHighlight = titleHighlight;
	}

	public List<Highlight> getTextContentHighlights()
	{
		return textContentHighlights;
	}

	public void setTextContentHighlights(List<Highlight> textContentHighlights)
	{
		this.textContentHighlights = textContentHighlights;
	}

	public List<AuthorHighlight> getAuthorHighlights()
	{
		return authorHighlights;
	}

	public void setAuthorHighlights(List<AuthorHighlight> authorHighlights)
	{
		this.authorHighlights = authorHighlights;
	}

	public Highlight getJournalTitleHighlight()
	{
		return journalTitleHighlight;
	}

	public void setJournalHighlight(Highlight journalHighlight)
	{
		this.journalTitleHighlight = journalHighlight;
	}

	public HighlightedSemedicoDocument(SemedicoDocument document)
	{
		this.document = document;
	}

	public SemedicoDocument getDocument()
	{
		return document;
	}

	public void setDocument(SemedicoDocument document)
	{
		this.document = document;
	}

	@Deprecated
	public String getKwicTitle()
	{
		if (hasTitleKeywords())
		{
			return kwicTitle;
		}
		return document.getTitle();
	}

	@Deprecated
	public boolean hasTitleKeywords()
	{
		return kwicTitle != null && kwicTitle.length() > 0;
	}

	@Deprecated
	public boolean hasAbstractKeywords()
	{
		return null != kwics && kwics.length > 0;
	}

	@Deprecated
	public int getNumberAbstractKwics()
	{
		return kwics.length;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getTitleHighlight());
		sb.append("\n");
		sb.append(getAuthorHighlights());
		sb.append("\n");
		sb.append(getJournalTitleHighlight());
		
		for (Highlight textContentHl : getTextContentHighlights())
		{
			sb.append("\n");
			sb.append(textContentHl);
		}
		return sb.toString();
	}

	@Deprecated
	public void setKwicTitle(String kwicTitle)
	{
		this.kwicTitle = kwicTitle;
	}

	@Deprecated
	public String getKwicAbstractText()
	{
		String abstractText = document.getAbstractText();
		if (abstractText != null && abstractText.length() > 250)
		{
			return abstractText.substring(0, 250) + "...";
		}
		return abstractText;
	}

	@Deprecated
	public String[] getKwics()
	{
		return kwics;
	}

	@Deprecated
	public void setKwics(String[] kwics)
	{
		this.kwics = kwics;
	}

	public static class AuthorHighlight
	{
		public AuthorHighlight()
		{
			// TODO Auto-generated constructor stub
		}

		public float docscore;
		public String firstname;
		public String lastname;
		public String affiliation;
		public String firstnameForPrint;

		@Override
		public String toString()
		{
			if (null == firstnameForPrint)
			{
				setFirstnameForPrint();
			}
			StringBuilder sb = new StringBuilder();
			sb.append(lastname).append(" ").append(firstnameForPrint);
			return sb.toString();
		}

		private void setFirstnameForPrint()
		{
			if (firstname == null)
			{
				return;
			}
			String[] split = firstname.split("\\s+");
			String withoutWS = "";
			boolean initialsOnly = true;
			
			for (String name : split)
			{
				if (name.length() > 1)
				{
					initialsOnly = false;
				}
				withoutWS += name;
			}
			if (initialsOnly)
			{
				firstnameForPrint = withoutWS;
			}
			else
			{
				firstnameForPrint = firstname;
			}
		}
	}

	public void setMeshMajorHighlights(List<Highlight> meshMajorHighlights)
	{
		this.meshMajorHighlights = meshMajorHighlights;
	}

	public List<Highlight> getMeshMajorHighlights()
	{
		return meshMajorHighlights;
	}

	public void setJournalVolumeHighlights(List<Highlight> journalVolumeHighlights)
	{
		if (null == journalVolumeHighlights || journalVolumeHighlights.isEmpty())
		{
			this.journalVolumeHighlight = null;
		}
		this.journalVolumeHighlight = journalVolumeHighlights.get(0);
	}

	public void setJournalIssueHighlights(List<Highlight> journalIssueHighlights)
	{
		if (null == journalIssueHighlights || journalIssueHighlights.isEmpty())
		{
			this.journalIssueHighlight = null;
		}
		this.journalIssueHighlight = journalIssueHighlights.get(0);
	}

	public void setAffiliationHighlights(List<Highlight> affiliationHighlights)
	{
		this.affiliationHighlights = affiliationHighlights;
	}

	public void setKeywordHighlights(List<Highlight> keywordHighlights)
	{
		this.keywordHighlights = keywordHighlights;
	}

	public Highlight getJournalVolumeHighlight()
	{
		return journalVolumeHighlight;
	}

	public Highlight getJournalIssueHighlight()
	{
		return journalIssueHighlight;
	}

	public List<Highlight> getAffiliationHighlights()
	{
		return affiliationHighlights;
	}

	public List<Highlight> getMeshMinorHighlights()
	{
		return meshMinorHighlights;
	}

	public List<Highlight> getSubstancesHighlights()
	{
		return substancesHighlights;
	}

	public List<Highlight> getKeywordHighlights()
	{
		return keywordHighlights;
	}

	public void setMeshMinorHighlights(List<Highlight> meshMinorHighlights)
	{
		this.meshMinorHighlights = meshMinorHighlights;
	}

	public void setSubstancesHighlights(List<Highlight> substancesHighlights)
	{
		this.substancesHighlights = substancesHighlights;
	}

	public void setHighlightedAbstract(Highlight highlightedAbstract)
	{
		this.highlightedAbstract = highlightedAbstract;
	}
}