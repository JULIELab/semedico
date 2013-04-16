package de.julielab.semedico.core.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Publication;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IDocumentCacheService;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.search.interfaces.IKwicService;

public class DocumentService implements IDocumentService {

	private static final String REVIEW = "Review";

	private static Logger logger = LoggerFactory
			.getLogger(DocumentService.class);

	private SolrServer solr;

	private final IDocumentCacheService documentCacheService;

	private final IKwicService kwicService;

	private final LoggerSource loggerSource;

	public DocumentService(@InjectService("SolrSearcher") SolrServer solr,
			IDocumentCacheService documentCacheService,
			IKwicService kwicService, LoggerSource loggerSource) {
		this.solr = solr;
		this.documentCacheService = documentCacheService;
		this.kwicService = kwicService;
		this.loggerSource = loggerSource;
	}

	// public void readDocument(SemedicoDocument document) throws IOException{
	// long time = System.currentTimeMillis();
	// org.apache.lucene.document.Document doc = readIndexDocument(document);
	// readAbstract(document, doc);
	// readTitle(document, doc);
	// readPubMedId(document, doc);
	// readPublications(document, doc);
	// determinePubType(document);
	// readPublicationTypes(document, doc);
	// readAuthors(document, doc);
	// readFullTextLinks(document, doc);
	// readRelatedArticles(document, doc);
	// time = System.currentTimeMillis() - time;
	// logger.info("reading document takes " + time + "ms");
	// }

	// TODO must be fetched directly from NLM by e-Tools
	// protected void readRelatedArticles(SemedicoDocument document,
	// SolrDocument doc) {
	//
	// String idsConcatenated =
	// (String)doc.get(IndexFieldNames.RELATED_ARTICLES);
	// if (idsConcatenated == null)
	// return;
	//
	// String[] idsSplitted = idsConcatenated.split("\\|");
	// for( String id: idsSplitted ){
	// Integer pubMedId = 0;// new Integer(id); // TODO comment in again
	// SemedicoDocument relatedDocument =
	// readDocumentStubWithPubMedId(pubMedId);
	// if( relatedDocument != null )
	// document.getRelatedArticles().add(relatedDocument);
	// }
	// }

	// TODO must be fetched directly from NLM by e-Tools
	// protected void readFullTextLinks(SemedicoDocument document,
	// SolrDocument doc) {
	// if(true)// TODO remove this if and return as soon as an appropriate index
	// is available
	// return;
	// String fullTextLinksConcatenated =
	// (String)doc.get(IndexFieldNames.FULLTEXT_LINKS);
	// if( fullTextLinksConcatenated == null )
	// return;
	//
	// String[] fullTextLinksSplitted =
	// fullTextLinksConcatenated.split(FULL_TEXT_LINK_DELIMITER);
	// for (String fullTextLink: fullTextLinksSplitted){
	// String[] urlAndIconUrl = fullTextLink.split("\\|");
	// String url = "hihi"; //urlAndIconUrl[0]; // TODO comment in again
	// String iconUrl = "hoho"; //urlAndIconUrl[1]; // TODO comment in again
	// ExternalLink externalLink = new ExternalLink(url, iconUrl);
	// document.getExternalLinks().add(externalLink);
	// }
	// }

	protected void readPubMedId(SemedicoDocument document, SolrDocument doc) {
		document.setPubMedId(getPmid(doc));
	}

	protected void readTitle(SemedicoDocument document, SolrDocument doc) {
		document.setTitle((String) doc.get(IIndexInformationService.TITLE));
	}

	protected void readAbstract(SemedicoDocument document, SolrDocument doc) {
		document.setAbstractText((String) doc.get(IIndexInformationService.ABSTRACT));
	}

	// protected org.apache.lucene.document.Document
	// readIndexDocument(SemedicoDocument document)
	// throws CorruptIndexException, IOException {
	// org.apache.lucene.document.Document doc;
	// doc = searcherWrapper.getIndexSearcher().doc(document.getLuceneId(),
	// displayFieldSelector);
	//
	// document.turnToObject();
	// return doc;
	// }

	protected void determinePubType(SemedicoDocument document)
			throws NumberFormatException {
		if (document.getAbstractText() != null
				&& document.getAbstractText().length() > 0)
			document.setType(SemedicoDocument.TYPE_ABSTRACT);
	}

	protected void readAuthors(SemedicoDocument document, SolrDocument doc) {
		if (doc.getFieldValues(IIndexInformationService.AUTHORS) == null)
			return;
		for (Object authorString : doc.getFieldValues(IIndexInformationService.AUTHORS)) {
			Author author = new Author();
			String[] names = ((String) authorString).split(",");
			if (names.length == 2) {
				author.setForename(names[1]);
				author.setLastname(names[0]);
			} else
				author.setLastname((String) authorString);

			document.getAuthors().add(author);
		}
		// String fieldValue = (String)doc.get(IndexFieldNames.AUTHORS);
		// String[] authors = fieldValue.split("\\|");
		// for( String authorString: authors ){
		//
		// Author author = new Author();
		// String[] names = authorString.split(",");
		// if( names.length == 2){
		// author.setForename(names[1]);
		// author.setLastname(names[0]);
		// }
		// else
		// author.setLastname(authorString);
		//
		// document.getAuthors().add(author);
		// }
	}

	protected void readPublicationTypes(SemedicoDocument document,
			SolrDocument doc) {

		Collection<Object> publicationTypes = (Collection<Object>) doc
				.getFieldValues(IIndexInformationService.FACET_PUBTYPES);
		// System.out.println("DocumentService, readPublicationTypes:"
		// + publicationTypes);
		if (publicationTypes != null)
			for (Object publicationType : publicationTypes) {
				System.out.println(publicationType);
				System.out.println(((String) publicationType).contains(REVIEW));
				if (((String) publicationType).contains(REVIEW))
					document.setReview(true);
			}
	}

	protected void readPublications(SemedicoDocument document, SolrDocument doc) {
		String publicationString = (String) doc.get(IIndexInformationService.JOURNAL);
		if (publicationString != null) {
			String[] publicationParts = publicationString.split("\\|");
			Publication publication = new Publication();
			publication.setTitle(publicationParts[0]);
			if (publicationParts.length > 1) {
				publication.setVolume(publicationParts[1]);
				if (publicationParts.length == 4)
					publication.setIssue(publicationParts[2]);

				publication
						.setPages(publicationParts[publicationParts.length - 1]);
			}
			document.setPublication(publication);

			// TODO Handle data appropriately
			String dateString = (String) doc.get(IIndexInformationService.DATE);
			if (dateString != null && !dateString.equals("")) {
				String[] dateCompounds = dateString.split("\\|");

				for (int i = 0; i < dateCompounds.length; i++)
					if (dateCompounds[i].equals("0"))
						dateCompounds[i] = "1";

				dateString = dateCompounds[0] + "|" + dateCompounds[1] + "|"
						+ dateCompounds[2];

				SimpleDateFormat sdf = new SimpleDateFormat("yyyy|MM|dd");
				Date date = null;
				try {
					date = sdf.parse(dateString);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				// Date date = new Date();
				publication.setDate(date);
			}
		}
	}

	// /**
	// * @param semedicoDoc
	// * @param solrDoc
	// */
	// protected void readRelatedArticles(SemedicoDocument semedicoDoc,
	// SolrDocument solrDoc) throws NumberFormatException {
	// try {
	// Collection<SemedicoDocument> relatedArticles = relatedArticlesService
	// .fetchRelatedArticles(getPmid(solrDoc));
	// semedicoDoc.setRelatedArticles(relatedArticles);
	// } catch (IOException e) {
	// logger.error("IOException while trying to resolve related articles of document with ID \""
	// + ((String) solrDoc
	// .getFieldValue(IndexFieldNames.PUBMED_ID)) + "\".");
	// e.printStackTrace();
	// }
	// }

	/**
	 * @param semedicoDoc
	 * @param solrDoc
	 */
	// protected void readFullTextLinks(SemedicoDocument semedicoDoc,
	// SolrDocument solrDoc) throws NumberFormatException {
	// Collection<ExternalLink> externalLinks;
	// try {
	// int pmid = getPmid(solrDoc);
	// externalLinks = externalLinkService.fetchExternalLinks(pmid);
	// semedicoDoc.setExternalLinks(externalLinks);
	// } catch (IOException e) {
	// logger.error("IOException while trying to resolve full text link�s of document with ID \""
	// + ((String) solrDoc
	// .getFieldValue(IndexFieldNames.PUBMED_ID)) + "\".");
	// e.printStackTrace();
	// }
	// }

	// /**
	// * Creates SemedicoDocuments with minimal content (only title and
	// * publication types). These stubs are used to represent related articles
	// of
	// * search results (if we would just get full documents we would end up
	// with
	// * resolving the related articles of the related articles of the related
	// * articles of...).
	// */
	// public SemedicoDocument getRelatedArticleDocument(int pmid) {
	// SemedicoDocument hit = documentCacheService.getCachedDocument(pmid);
	// if (hit == null) {
	// SolrDocument readIndexDocument = getSolrDocWithPubmedId(pmid);
	//
	// if (readIndexDocument == null) {
	// return null;
	// }
	//
	// if (pmid != getPmid(readIndexDocument))
	// throw new IllegalStateException("Document with ID \"" + pmid
	// + "\" was queried from Solr, but a document with ID \""
	// + getPmid(readIndexDocument) + "\" has been returned.");
	//
	// hit = new SemedicoDocument(pmid);
	//
	// readTitle(hit, readIndexDocument);
	// readPublicationTypes(hit, readIndexDocument);
	// readPublications(hit, readIndexDocument);
	// }
	// return hit;
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IDocumentService#getSemedicoDocument
	 * (int)
	 */
	@Override
	@Deprecated
	public SemedicoDocument getSemedicoDocument(int pmid) {
//		SolrDocument solrDoc = getSolrDocById(pmid);
//		SemedicoDocument semedicoDoc = getSemedicoDocument(solrDoc);
//		return semedicoDoc;
		throw new NotImplementedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IDocumentService#getHitListDocument
	 * (org.apache.solr.common.SolrDocument, java.util.Map)
	 */
	@Override
	public DocumentHit getHitListDocument(SolrDocument solrDoc,
			Map<String, Map<String, List<String>>> highlighting) {
		int pmid = getPmid(solrDoc);
		SemedicoDocument semedicoDoc = getSemedicoDocument(solrDoc);

		DocumentHit hit = new DocumentHit(semedicoDoc);

		if (highlighting == null)
			return hit;

		Map<String, List<String>> docHighlights = highlighting.get(String
				.valueOf(pmid));

		String[] abstractHighlights = kwicService
				.getAbstractHighlights(docHighlights);
		String titleHighlights = kwicService.getHighlightedTitle(docHighlights);

		hit.setKwics(abstractHighlights);
		hit.setKwicTitle(titleHighlights);

		return hit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IDocumentService#
	 * getHighlightedSemedicoDocument(int, java.lang.String)
	 */
	@Override
	public SemedicoDocument getHighlightedSemedicoDocument(
			SolrDocument solrDoc, Map<String, List<String>> docHighlights) {
		SemedicoDocument semedicoDoc = getSemedicoDocument(solrDoc);
		String highlightedAbstract = kwicService.getHighlightedAbstract(docHighlights);
		String highlightedTitle = kwicService
				.getHighlightedTitle(docHighlights);

		semedicoDoc.setHighlightedTitle(highlightedTitle);
		semedicoDoc.setHighlightedAbstract(highlightedAbstract);
		return semedicoDoc;
	}

	private SemedicoDocument getSemedicoDocument(SolrDocument solrDoc) {
		if (solrDoc == null)
			return null;
		long time = System.currentTimeMillis();
		int pmid = getPmid(solrDoc);

		SemedicoDocument semedicoDoc = documentCacheService
				.getCachedDocument(pmid);
		if (semedicoDoc == null) {
			semedicoDoc = new SemedicoDocument(loggerSource.getLogger(SemedicoDocument.class), pmid);

			readPubMedId(semedicoDoc, solrDoc);
			readAbstract(semedicoDoc, solrDoc);
			readTitle(semedicoDoc, solrDoc);
			readPublications(semedicoDoc, solrDoc);
			determinePubType(semedicoDoc);
			readPublicationTypes(semedicoDoc, solrDoc);
			readAuthors(semedicoDoc, solrDoc);
			readPPIs(semedicoDoc, solrDoc);

			time = System.currentTimeMillis() - time;
			// logger.info("Reading document \"{}\" from index took {}ms", pmid,
			// time);

			documentCacheService.addDocument(semedicoDoc);
		}
		// else
		// logger.debug("Returned cached semedico document \"{}\".", pmid);

		return semedicoDoc;
	}
	
	private void readPPIs(SemedicoDocument semedicoDoc, SolrDocument solrDoc) {
		Collection<Object> PPIs = solrDoc.getFieldValues(IIndexInformationService.PPI);
		if(PPIs == null)
			semedicoDoc.setPPIs(new String[]{});
		else
			semedicoDoc.setPPIs(PPIs.toArray(new String[]{}));
	}

	private Pair<SolrDocument, Map<String, List<String>>> getHighlightedSolrDocById(
			int pmid, String originalQueryString) {
//		QueryResponse queryResponse = querySolr(pmid, originalQueryString);
//		SolrDocument solrDoc = null;
//		Map<String, List<String>> highlighting = null;
//		if (queryResponse.getHighlighting() != null) {
//			highlighting = queryResponse.getHighlighting().get(
//					String.valueOf(pmid));
//			logger.debug(
//					"Highlighting has been returned for document with ID \"{}\".",
//					pmid);
//		}
//
//		SolrDocumentList docList = queryResponse.getResults();
//		if (docList != null && docList.size() > 0)
//			solrDoc = docList.get(0);
//		return new ImmutablePair<SolrDocument, Map<String, List<String>>>(
//				solrDoc, highlighting);
		throw new NotImplementedException();
	}

	private SolrDocument getSolrDocById(int pmid) {
//		QueryResponse queryResponse = querySolr(pmid, null);
//		SolrDocumentList docList = queryResponse.getResults();
//		if (docList != null && docList.size() > 0)
//			return docList.get(0);
//		return null;
		throw new NotImplementedException();
	}

	private QueryResponse querySolr(int pmid, String originalQueryString) {
//		SolrQuery solrQuery = new SolrQuery("*:*");
//		solrQuery.setFilterQueries(IIndexInformationService.PUBMED_ID + ":" + pmid);
//		if (originalQueryString != null && originalQueryString.length() > 0) {
//			solrQuery.setQuery(originalQueryString);
//			solrQuery.setHighlight(true);
//			solrQuery.setHighlightFragsize(50000);
//			solrQuery.add("hl.fl", TEXT + "," + TITLE);
//			solrQuery.setHighlightSimplePre("<span class=\"highlightFull\">");
//			solrQuery.setHighlightSimplePost("</span>");
//		}
//		try {
//			// Try to get a highlighted document. Since this method is also
//			// called when clicking on related articles, it might be that the
//			// restriction to "originalQueryString" prohibits a hit. In this
//			// case we have to try again without a particular query (besided the
//			// PMID itself) and thus there will be no highlighting.
//			QueryResponse queryResponse = solr.query(solrQuery);
//			SolrDocumentList docList = queryResponse.getResults();
//			// No highlighted results found. Try again without restriction to
//			// the original query. There will be no highlighting.
//			if (docList.getNumFound() == 0) {
//				solrQuery.setQuery("*:*");
//				queryResponse = solr.query(solrQuery);
//				docList = queryResponse.getResults();
//			}
//			if (docList.getNumFound() == 0) {
//				logger.warn(
//						"Document with ID \"{}\" was queried from Solr but no result has been returned.",
//						pmid);
//			}
//			return queryResponse;
//		} catch (SolrServerException e) {
//			e.printStackTrace();
//		}
//		return null;
		throw new NotImplementedException();
	}

	private Integer getPmid(SolrDocument solrDoc) {
		try {
			Integer pmid = Integer.parseInt((String) solrDoc
					.getFieldValue(IIndexInformationService.PUBMED_ID));
			return pmid;
		} catch (NumberFormatException e) {
			logger.error("Could not parse pubmed ID String \""
					+ ((String) solrDoc
							.getFieldValue(IIndexInformationService.PUBMED_ID))
					+ "\" to a number.");
			e.printStackTrace();
		}
		return null;
	}
}