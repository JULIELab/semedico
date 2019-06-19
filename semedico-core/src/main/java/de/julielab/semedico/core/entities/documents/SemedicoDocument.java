package de.julielab.semedico.core.entities.documents;

import de.julielab.semedico.core.ExternalLink;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Hinweise von Chrisitna, v.a. bzgl. Änderungen für Bexis
 * 
 *   Funktion setPmcId war 2mal da, eine davon gelöscht
 *   
 */

public class SemedicoDocument
{
	private String title;
	private Date date;
	// TODO has actually to be set in document service
	private String documentText;
	private String abstractText;
	private Publication publication;
	private List<Author> authors;
	private String docId;
	
	// The type is supposed to tell whether this document as only a title, title
	// and abstract or is even a full text document.
	private int type;
	
	// this corresponds to the PubMed "review" state: Those are not original
	// research articles but review articles
	private boolean review;
	private Collection<ExternalLink> externalLinks;
	private Collection<SemedicoDocument> relatedArticles;
	@Deprecated
	private String highlightedTitle;
	@Deprecated
	private String highlightedAbstract;

	public static final int TYPE_TITLE = 0;
	public static final int TYPE_ABSTRACT = 1;
	public static final int TYPE_FULL_TEXT = 2;
	private final Logger log;
	private String pmcid;
	// The ElasticSearch index type - medline or Pubmed Central (PMC) currently
	private String indexType;
	private String pmid;



	public String getIndexType()
	{
		return indexType;
	}
	public void setIndexType(String indexType)
	{
		this.indexType = indexType;
	}

	public SemedicoDocument(Logger log, String docId, String indexType)
	{
		super();
		this.log = log;
		this.docId = docId;
		this.indexType = indexType;
		authors = new ArrayList<Author>();
		relatedArticles = new ArrayList<SemedicoDocument>();
		externalLinks = new ArrayList<ExternalLink>();
	}

	/**
	 * @return the highlightedTitle
	 */
	@Deprecated
	public String getHighlightedTitle()
	{
		if (highlightedTitle != null)
			return highlightedTitle;
		log.debug("Document with ID \"{}\" does not have title highlights. Returning plain title text.", getDocId());
		return getTitle();
	}

	/**
	 * @param highlightedTitle
	 *            the highlightedTitle to set
	 */
	@Deprecated
	public void setHighlightedTitle(String highlightedTitle)
	{
		this.highlightedTitle = highlightedTitle;
	}

	/**
	 * @return the highlightedAbstract
	 */
	@Deprecated
	public String getHighlightedAbstract()
	{
		if (highlightedAbstract != null)
			return highlightedAbstract;
		log.debug("Document with ID \"{}\" does not have abstract highlights. Returning plain abstract text.",
				getDocId());
		return getAbstractText();
	}

	/**
	 * @param highlightedAbstract
	 *            the highlightedAbstract to set
	 */
	@Deprecated
	public void setHighlightedAbstract(String highlightedAbstract)
	{
		this.highlightedAbstract = highlightedAbstract;
	}

	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
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

	
	public Publication getPublication()
	{
		return publication;
	}
	public void setPublication(Publication publication)
	{
		this.publication = publication;
	}

	public int getType()
	{
		return type;
	}
	/**
	 * Sets the type of the document represented by this object. Valid values
	 * are one of {@link #TYPE_TITLE}, {@link #TYPE_ABSTRACT} and
	 * {@link #TYPE_FULL_TEXT}, determining the appropriate type of available
	 * data for this document, respectively.
	 * 
	 * @param type
	 *            The type of this document.
	 * @see #TYPE_TITLE
	 * @see #TYPE_ABSTRACT
	 * @see #TYPE_FULL_TEXT
	 */
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

	public Collection<ExternalLink> getExternalLinks()
	{
		return externalLinks;
	}
	public void setExternalLinks(Collection<ExternalLink> externalLinks)
	{
		this.externalLinks = externalLinks;
	}
	
	public Collection<SemedicoDocument> getRelatedArticles()
	{
		return relatedArticles;
	}
	public void setRelatedArticles(Collection<SemedicoDocument> relatedArticles)
	{
		this.relatedArticles = relatedArticles;
	}

	public String getPmcid()
	{
		return pmcid;
	}
	public void setPmcid(String pmcId)
	{
		this.pmcid = pmcId;
	}

	public void setPmid(String pmid)
	{
		this.pmid = pmid;
	}
	public String getPmid()
	{
		return pmid;
	}
	public String getDocumentText() {
		return documentText;
	}
	public void setDocumentText(String documentText) {
		this.documentText = documentText;
	}

}