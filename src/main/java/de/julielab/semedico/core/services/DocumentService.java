package de.julielab.semedico.core.services;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;


import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.Publication;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.solr.ISolrServerWrapper;

public class DocumentService implements IDocumentService {
	private static final String FULL_TEXT_LINK_DELIMITER = "\\&\\&";

	private static final String REVIEW = "REVIEW";

	private static Logger logger = Logger.getLogger(DocumentService.class);
	
	private FieldSelector displayFieldSelector;
	private ISolrServerWrapper solr;
	
	private class DisplayFieldSelector implements FieldSelector{

		public FieldSelectorResult accept(String fieldName) {
			if( fieldName.equals(IndexFieldNames.PUBMED_ID) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.ABSTRACT) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.TITLE) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.JOURNAL) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.DATE) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.AUTHORS) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.PUBLICATION_TYPES) )
				return FieldSelectorResult.LOAD;	
			else if( fieldName.equals(IndexFieldNames.FULLTEXT_LINKS) )
				return FieldSelectorResult.LOAD;
			else if( fieldName.equals(IndexFieldNames.RELATED_ARTICLES) )
				return FieldSelectorResult.LOAD;
			else
				return FieldSelectorResult.NO_LOAD;
		}
		
	}	
		
	public DocumentService() throws IOException {
		displayFieldSelector = new DisplayFieldSelector();
	}

//	public void readDocument(SemedicoDocument document) throws IOException{
//		long time = System.currentTimeMillis();
//		org.apache.lucene.document.Document doc = readIndexDocument(document);
//		readAbstract(document, doc);
//		readTitle(document, doc);
//		readPubMedId(document, doc);
//		readPublications(document, doc);
//		determinePubType(document);
//		readPublicationTypes(document, doc);
//		readAuthors(document, doc);
//		readFullTextLinks(document, doc);
//		readRelatedArticles(document, doc);
//		time = System.currentTimeMillis() - time;
//		logger.info("reading document takes " + time + "ms");
//	}

	protected void readRelatedArticles(SemedicoDocument document,
			SolrDocument doc) {
		
		String idsConcatenated = (String)doc.get(IndexFieldNames.RELATED_ARTICLES);
		if (idsConcatenated == null)
			return;
		
		String[] idsSplitted = idsConcatenated.split("\\|");
		for( String id: idsSplitted ){
			Integer pubMedId = 0;//  new Integer(id); // TODO comment in again
			SemedicoDocument relatedDocument = readDocumentStubWithPubMedId(pubMedId);
			if( relatedDocument != null )
				document.getRelatedArticles().add(relatedDocument);
		}
	}

	protected void readFullTextLinks(SemedicoDocument document,
			SolrDocument doc) {
		if(true)// TODO remove this if and return as soon as an appropriate index is available
			return;
		String fullTextLinksConcatenated = (String)doc.get(IndexFieldNames.FULLTEXT_LINKS);
		if( fullTextLinksConcatenated == null )
			return;
		
		String[] fullTextLinksSplitted = fullTextLinksConcatenated.split(FULL_TEXT_LINK_DELIMITER);
		for (String fullTextLink: fullTextLinksSplitted){
			String[] urlAndIconUrl = fullTextLink.split("\\|");
			String url = "hihi"; //urlAndIconUrl[0]; // TODO comment in again
			String iconUrl = "hoho"; //urlAndIconUrl[1]; // TODO comment in again
			ExternalLink externalLink = new ExternalLink(url, iconUrl);
			document.getExternalLinks().add(externalLink);
		}
	}

	protected void readPubMedId(SemedicoDocument document,
			SolrDocument doc) {
		document.setPubMedId(Integer.parseInt((String)doc.get(IndexFieldNames.PUBMED_ID)));
	}

	protected void readTitle(SemedicoDocument document,
			SolrDocument doc) {
		document.setTitle((String)doc.get(IndexFieldNames.TITLE));
	}

	protected void readAbstract(SemedicoDocument document,
			SolrDocument doc) {
		document.setAbstractText((String)doc.get(IndexFieldNames.ABSTRACT));
	}

//	protected org.apache.lucene.document.Document readIndexDocument(SemedicoDocument document)
//			throws CorruptIndexException, IOException {
//		org.apache.lucene.document.Document doc;
//		doc = searcherWrapper.getIndexSearcher().doc(document.getLuceneId(), displayFieldSelector);
//		
//		document.turnToObject();
//		return doc;
//	}

	protected void determinePubType(SemedicoDocument document) {
		if( document.getAbstractText() != null && document.getAbstractText().length() > 0 )
			document.setType(SemedicoDocument.TYPE_ABSTRACT);
	}

	protected void readAuthors(SemedicoDocument document,
			SolrDocument doc) {
		String fieldValue = (String)doc.get(IndexFieldNames.AUTHORS);
		String[] authors = fieldValue.split("\\|");		
		for( String authorString: authors ){
			
			Author author = new Author();
			String[] names = authorString.split(",");
			if( names.length == 2){
				author.setForename(names[1]);
				author.setLastname(names[0]);
			}
			else
				author.setLastname(authorString);
			
			document.getAuthors().add(author);
		}
	}

	protected void readPublicationTypes(SemedicoDocument document,
			SolrDocument doc) {
		
		Collection<Object> publicationTypes = (Collection<Object>)doc.getFieldValues(IndexFieldNames.PUBLICATION_TYPES);
		if( publicationTypes != null)
			for( Object publicationType : publicationTypes )
				if( ((String)publicationType).contains(REVIEW) )
					document.setReview(true);
	}

	protected void readPublications(SemedicoDocument document,
			SolrDocument doc) {
		String publicationString = (String)doc.get(IndexFieldNames.JOURNAL);
		if( publicationString != null ){
			String[] publicationParts = publicationString.split("\\|");
			Publication publication = new Publication();
			publication.setTitle(publicationParts[0]);
			if( publicationParts.length > 1 ){
				publication.setVolume(publicationParts[1]);
				if( publicationParts.length == 4 )
					publication.setIssue(publicationParts[2]);

				publication.setPages(publicationParts[publicationParts.length-1]);
			}
			document.setPublication(publication);
			
//			TODO Handle data appropriately
			String dateString = (String)doc.get(IndexFieldNames.DATE);
			if( dateString != null && !dateString.equals("") ){
				String[] dateCompounds = dateString.split("\\|");
				
				for( int i = 0; i < dateCompounds.length; i++ )
					if( dateCompounds[i].equals("0") )
						dateCompounds[i] = "1";
				
				dateString = dateCompounds[0] + "|"+ 
							 dateCompounds[1] + "|"+ 
							 dateCompounds[2];
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd");
				Date date = null;
				try {
					date = sdf.parse(dateString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
//				Date date = new Date();
				publication.setDate(date);
			}
		}
	}

	public SemedicoDocument readDocumentStubWithPubMedId(Integer pmid) {
		SolrDocument readIndexDocument = getSolrDocWithPubmedId(pmid);

		if (readIndexDocument == null)
			return null;
		
		SemedicoDocument hit = new SemedicoDocument();
		hit.turnToObject();
		hit.setPubMedId(pmid);
		
		readTitle(hit, readIndexDocument);
		readPublicationTypes(hit, readIndexDocument);
		readPublications(hit, readIndexDocument);
		return hit;
	}

	public SemedicoDocument buildSemedicoDocFromSolrDoc(SolrDocument solrDoc) {
		long time = System.currentTimeMillis();
		SemedicoDocument semedicoDoc = new SemedicoDocument();
		semedicoDoc.turnToObject();
		readAbstract(semedicoDoc, solrDoc);
		readTitle(semedicoDoc, solrDoc);
		readPubMedId(semedicoDoc, solrDoc);
		readPublications(semedicoDoc, solrDoc);
		determinePubType(semedicoDoc);
		readPublicationTypes(semedicoDoc, solrDoc);
		readAuthors(semedicoDoc, solrDoc);
		readFullTextLinks(semedicoDoc, solrDoc);
		readRelatedArticles(semedicoDoc, solrDoc);
		time = System.currentTimeMillis() - time;
		logger.info("reading document takes " + time + "ms");
		return semedicoDoc;
	}
	
	public SemedicoDocument readDocumentWithPubmedId(int pmid) {
		SolrDocument solrDoc = getSolrDocWithPubmedId(pmid);
		if (solrDoc == null)
			return null;
		return buildSemedicoDocFromSolrDoc(solrDoc);
	}
	
	private SolrDocument getSolrDocWithPubmedId(int pmid) {
		SolrQuery query = new SolrQuery(IndexFieldNames.PUBMED_ID + ":" + pmid);
		SolrDocumentList docList = null;
		try {
			solr = getSearcher();
			docList = solr.query(query).getResults();
		} catch (SolrServerException e) {
			e.printStackTrace();
		}
		if (docList == null || docList.size() == 0)
			return null;
		return docList.get(0);
	}

//	public void readDocuments(List<SemedicoDocument> hits) throws IOException {
//		for( SemedicoDocument hit: hits)
//			readDocumentWithLuceneId(hit);
//	}

	public ISolrServerWrapper getSearcher() {
		return solr;
	}

	public void setSearcher(ISolrServerWrapper solr) {
		this.solr = solr;
	}
}