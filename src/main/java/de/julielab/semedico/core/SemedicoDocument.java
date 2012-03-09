package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class SemedicoDocument {

	private String title;
	private Date date;
	private String abstractText;
	private Publication publication;
	private List<Author> authors;
	private Integer pmid;
	// The type is supposed to tell whether this document as only a title, title
	// and abstract or is even a full text document.
	private int type;
	private boolean review;
	private Collection<ExternalLink> externalLinks;
	private Collection<SemedicoDocument> relatedArticles;

	public static final int TYPE_TITLE = 0;
	public static final int TYPE_ABSTRACT = 1;
	public static final int TYPE_FULL_TEXT = 2;

	public SemedicoDocument(Integer pubMedId) {
		super();
		this.pmid = pubMedId;
		authors = new ArrayList<Author>();
		relatedArticles = new ArrayList<SemedicoDocument>();
		externalLinks = new ArrayList<ExternalLink>();
	}


	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public List<Author> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Author> authors) {
		this.authors = authors;
	}

	public Integer getPubmedId() {
		return pmid;
	}

	public void setPubMedId(Integer pubMedId) {
		this.pmid = pubMedId;
	}


	public String getAbstractText() {
		return abstractText;
	}

	public void setAbstractText(String abstractText) {
		this.abstractText = abstractText;
	}

	public Publication getPublication() {
		return publication;
	}

	public void setPublication(Publication publication) {
		this.publication = publication;
	}

	public int getType() {
		return type;
	}

	/**
	 * Sets the type of the document represented by this object. Valid values
	 * are one of {@link #TYPE_TITLE}, {@link #TYPE_ABSTRACT} and
	 * {@link #TYPE_FULL_TEXT}, determining the appropriate type of available
	 * data for this document, respectively.
	 * 
	 * @param type The type of this document.
	 * @see #TYPE_TITLE
	 * @see #TYPE_ABSTRACT
	 * @see #TYPE_FULL_TEXT
	 */
	public void setType(int type) {
		this.type = type;
	}

	public boolean isReview() {
		return review;
	}

	public void setReview(boolean review) {
		this.review = review;
	}

	public Collection<ExternalLink> getExternalLinks() {
		return externalLinks;
	}

	public void setExternalLinks(Collection<ExternalLink> externalLinks) {
		this.externalLinks = externalLinks;
	}

	public Collection<SemedicoDocument> getRelatedArticles() {
		return relatedArticles;
	}

	public void setRelatedArticles(Collection<SemedicoDocument> relatedArticles) {
		this.relatedArticles = relatedArticles;
	}
}