package de.julielab.semedico.core.services;

import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.entities.documents.Author;
import de.julielab.semedico.core.entities.documents.Publication;

import java.util.Collection;
import java.util.Date;
import java.util.List;

// TODO IN ENTWICKLUNG

/**
 * 
 * @author lohr
 * --> kann eigentlich aus SemedicoDocument erzeugt werden
 * --> Überlegung, ob sinnvoll...?!
 * --> geht aus irgendeinem Grund wegen Klassenüberladung o.ä. nicht für gson,
 * 		deswegen diese Klasse für den Export mit gson für json
 */

public class BibliographyEntry
{
	private String articleTitle;
	private Date date;
	private String abstractText;
	private String docId;
	private String pmid;
	private String pmcid;
	private List <Author> authors;
	private Publication publication;
	private Collection <ExternalLink> externalLinks;
	
	private int type;
	private boolean review;
	private String indextype;

	public BibliographyEntry(
		String articleTitle,
		Date date,
		String abstractText,
		String docId,
		String pmid,
		String pmcid,
		List <Author> authors,
		Publication publication,
		Collection <ExternalLink> externalLinks
		)
	{
		this.articleTitle = articleTitle;
		this.date = date;
		this.abstractText = abstractText;
		this.docId = docId;
		this.pmid = pmid;
		this.pmcid = pmcid;	
		this.authors = authors;
		this.publication = publication;
		this.externalLinks = externalLinks;
	}
	
	public BibliographyEntry()
	{
		// TODO Auto-generated constructor stub
	}

	
	public String getArticleTitle()
	{
		return articleTitle;
	}

	public void setArticleTitle(String articleTitle)
	{
		this.articleTitle = articleTitle;
	}

	
	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	
	public List<Author> getAuthors()
	{
		return authors;
	}

	public void setAuthors(List<Author> authors)
	{
		this.authors = authors;
	}

	
	public Publication getPublication()
	{
		return publication;
	}
	public void setPublication(Publication publication)
	{
		this.publication = publication;
	}
	
	
	public String getDocId()
	{
		return docId;
	}
	public void setDocId(String docId)
	{
		this.docId = docId;
	}


	public String getAbstractText()
	{
		return abstractText;
	}
	public void setAbstractText(String abstractText)
	{
		this.abstractText = abstractText;
	}
	
	
	public void setPmid(String pmid)
	{
		this.pmid = pmid;	
	}
	public String getPmid()
	{
		return pmid;
	}

	
	public void setPmcid(String pmcid)
	{
		this.pmcid = pmcid;	
	}
	public String getPmcid()
	{
		return pmcid;
	}
	

	public void setExternalLinks(Collection <ExternalLink> externalLinks)
	{
		this.externalLinks = externalLinks;
	}
	public Collection <ExternalLink> getExternalLinks()
	{
		return externalLinks;
	}
	
	
	public int getType()
	{
		return type;
	}
	public void setType(int type)
	{
		this.type = type;
	}

	
	public boolean isReview()
	{
		return review;
	}
	public void setReview(boolean review)
	{
		this.review = review;
	}

	
	public String getIndextype()
	{
		return indextype;
	}
	public void setIndextype(String indextype)
	{
		this.indextype = indextype;
	}
}
