//package de.julielab.semedico.core.services;
//
//// TODO KLASSE AUFRÄUMEN
//// Broken since the index format has changed. Think about ways to minimize this impact in the future.
//
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Collection;
//import java.util.Collections;
//import java.util.Date;
//import java.util.List;
//
//import org.apache.commons.lang.NotImplementedException;
//import org.apache.commons.lang3.StringUtils;
//import org.apache.tapestry5.ioc.LoggerSource;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.google.common.base.Predicate;
//import com.google.common.collect.Collections2;
//
//import de.julielab.elastic.query.components.data.ISearchServerDocument;
//import de.julielab.semedico.core.Author;
//import de.julielab.semedico.core.Publication;
//import de.julielab.semedico.core.search.components.data.Highlight;
//import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
//import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument.AuthorHighlight;
//import de.julielab.semedico.core.search.components.data.HighlightedStatement;
//import de.julielab.semedico.core.search.components.data.SemedicoDocument;
//import de.julielab.semedico.core.search.interfaces.IHighlightingService;
//import de.julielab.semedico.core.services.interfaces.IDocumentCacheService;
//import de.julielab.semedico.core.services.interfaces.IDocumentService;
//import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
//import de.julielab.semedico.core.services.interfaces.IConceptService;
////import example_gson.BibliographyEntry;
//
//public class DocumentServiceErrors implements IDocumentService {
//	private static final String REVIEW = "Review";
//
//	private static Logger logger = LoggerFactory.getLogger(DocumentService.class);
//
//	private final IDocumentCacheService documentCacheService;
//
//	private final IHighlightingService highlightingService;
//
//	private final LoggerSource loggerSource;
//
//	private IConceptService conceptService;
//
//	public DocumentServiceErrors(IDocumentCacheService documentCacheService, IHighlightingService kwicService,
//			IConceptService conceptService, LoggerSource loggerSource) {
//		this.documentCacheService = documentCacheService;
//		this.highlightingService = kwicService;
//		this.conceptService = conceptService;
//		this.loggerSource = loggerSource;
//	}
//
//	// Diese Read-Funktionen können für den einen Aufruf, wo sie benötigt
//	// werden, auch anders gelöst werden
//	// Lesbarkeit vom Code
//
//	protected void readPubMedId(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		document.setPmid(getPmid(solrDoc));
//	}
//
//	protected void readPmcId(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		document.setPmcid(getPmcId(solrDoc));
//	}
//
//	protected void readTitle(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		document.setTitle((String) solrDoc.get(IIndexInformationService.TITLE).get());
//	}
//
//	protected void readAbstract(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		document.setAbstractText((String) solrDoc.get(IIndexInformationService.ABSTRACT).get());
//	}
//
//	protected void determinePubType(SemedicoDocument document, ISearchServerDocument serverDoc)
//			throws NumberFormatException {
//		if (serverDoc.getIndexType().equals(IIndexInformationService.Indexes.Indices.medline)) {
//			if (document.getAbstractText() != null && document.getAbstractText().length() > 0) {
//				document.setType(SemedicoDocument.TYPE_ABSTRACT);
//			} else {
//				document.setType(SemedicoDocument.TYPE_TITLE);
//			}
//		} else if (serverDoc.getIndexType().equals(IIndexInformationService.Indexes.Indices.pmc)) {
//			document.setType(SemedicoDocument.TYPE_FULL_TEXT);
//		}
//	}
//
//	protected void readAuthors(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		if (solrDoc.get(IIndexInformationService.AUTHORS) == null) {
//			return;
//		}
//
//		List<Object> authors = solrDoc.getFieldValues(IIndexInformationService.AUTHORS).get();
//		List<Object> affiliations = solrDoc.getFieldValues(IIndexInformationService.GeneralIndexStructure.affiliation)
//				.get();
//
//		for (int i = 0; i < authors.size(); ++i) {
//			String authorString = (String) authors.get(i);
//			Author author = new Author();
//			String[] names = authorString.split(",");
//
//			if (names.length == 2) {
//				author.setForename(names[1]);
//				author.setLastname(names[0]);
//			} else {
//				author.setLastname((String) authorString);
//			}
//
//			if (affiliations != null && i < affiliations.size()) {
//				String affiliationString = (String) affiliations.get(i);
//				author.setAffiliation(affiliationString);
//			}
//
//			document.getAuthors().add(author);
//		}
//	}
//
//	protected void readPublicationTypes(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		String pubtype = (String) solrDoc.get(IIndexInformationService.GeneralIndexStructure.pubtype).orElse(null);
//		if (pubtype != null && pubtype.contains(REVIEW)) {
//			document.setReview(true);
//		}
//	}
//
//	protected void readPublications(SemedicoDocument document, ISearchServerDocument solrDoc) {
//		Publication publication = new Publication();
//		String title = (String) solrDoc.get(IIndexInformationService.GeneralIndexStructure.journaltitle).orElse("");
//
//		if (!StringUtils.isBlank(title)) {
//			String volume = (String) solrDoc.get(IIndexInformationService.GeneralIndexStructure.journalvolume)
//					.orElse("");
//			String issue = (String) solrDoc.get(IIndexInformationService.GeneralIndexStructure.journalissue).orElse("");
//			String pages = (String) solrDoc.get(IIndexInformationService.GeneralIndexStructure.journalpages).orElse("");
//
//			publication.setTitle(title);
//			if (!StringUtils.isBlank(volume)) {
//				publication.setVolume(volume);
//			}
//			if (!StringUtils.isBlank(issue)) {
//				publication.setIssue(issue);
//			}
//			publication.setPages(pages);
//		}
//
//		String dateString = (String) solrDoc.get(IIndexInformationService.DATE).orElse("");
//		if (!StringUtils.isBlank(dateString)) {
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
//			Date date = null;
//			try {
//				date = sdf.parse(dateString);
//			} catch (ParseException e) {
//				e.printStackTrace();
//			}
//			publication.setDate(date);
//			Calendar cal = Calendar.getInstance();
//			cal.setTime(date);
//
//			int month = cal.get(Calendar.MONTH);
//			int day = cal.get(Calendar.DAY_OF_MONTH);
//
//			if (solrDoc.getIndexType().equals(IIndexInformationService.Indexes.Indices.pmc) && month == Calendar.JANUARY
//					&& day == 1) {
//				publication.setDateComplete(false);
//			}
//		}
//
//		document.setPublication(publication);
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see de.julielab.semedico.core.services.IDocumentService#getSemedicoDocument
//	 * (int)
//	 */
//	@Override
//	@Deprecated
//	public SemedicoDocument getSemedicoDocument(int pmid) {
//		// SolrDocument solrDoc = getSolrDocById(pmid);
//		// SemedicoDocument semedicoDoc = getSemedicoDocument(solrDoc);
//		// return semedicoDoc;
//		throw new NotImplementedException();
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see de.julielab.semedico.core.services.IDocumentService#getHitListDocument
//	 * (org.apache.solr.common.SolrDocument, java.util.Map)
//	 */
//	@Override
//	public HighlightedSemedicoDocument getHitListDocument(ISearchServerDocument serverDoc) {
//		SemedicoDocument semedicoDoc = getSemedicoDocument(serverDoc);
//
//		BibliographyEntry currentEntry = new BibliographyEntry(); // das soll
//																	// Rückgabewert
//																	// werden
//
//		currentEntry.setArticleTitle(semedicoDoc.getTitle());
//		currentEntry.setAbstractText(semedicoDoc.getAbstractText());
//		currentEntry.setDocId(semedicoDoc.getDocId());
//		currentEntry.setPmid(semedicoDoc.getPmid());
//		currentEntry.setPmcid(semedicoDoc.getPmcid());
//		currentEntry.setAuthors(semedicoDoc.getAuthors());
//
//		Publication publication = new Publication();
//		publication.setTitle(semedicoDoc.getPublication().getTitle());
//
//		publication.setPages(semedicoDoc.getPublication().getVolume());
//		publication.setIssue(semedicoDoc.getPublication().getIssue());
//		publication.setDate(semedicoDoc.getPublication().getDate());
//		publication.setPages(semedicoDoc.getPublication().getPages());
//
//		currentEntry.setPublication(publication);
//		currentEntry.setExternalLinks(semedicoDoc.getExternalLinks());
//
//		HighlightedSemedicoDocument hit = new HighlightedSemedicoDocument(semedicoDoc); // Rückgabewert
//
//		int maxContentHighlights = 3;
//		Highlight titleHighlight = highlightingService.getTitleHighlight(serverDoc);
//
//		List<Highlight> textContentHighlights = highlightingService.getBestTextContentHighlights(serverDoc,
//				maxContentHighlights, IIndexInformationService.TITLE);
//
//		List<AuthorHighlight> authorHighlights = highlightingService.getAuthorHighlights(serverDoc);
//
//		List<Highlight> journalTitleHighlight = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.journaltitle, false, true, false);
//
//		List<Highlight> journalVolumeHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.journalvolume, false, true);
//
//		List<Highlight> journalIssueHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.journalissue, false, true);
//
//		List<Highlight> affiliationHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.affiliation, true);
//
//		List<Highlight> keywordsHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.keywords, true);
//
//		List<Highlight> meshMajorHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.meshmajor, true, false, false, true);
//
//		List<Highlight> meshMinorHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.meshminor, true, false, false, true);
//
//		List<Highlight> substancesHl = highlightingService.getFieldHighlights(serverDoc,
//				IIndexInformationService.GeneralIndexStructure.substances, true, false, false, true);
//
//		hit.setTitleHighlight(titleHighlight);
//		hit.setTextContentHighlights(textContentHighlights);
//		hit.setAuthorHighlights(authorHighlights);
//		hit.setJournalHighlight(journalTitleHighlight.get(0));
//		hit.setJournalVolumeHighlights(journalVolumeHl);
//		hit.setJournalIssueHighlights(journalIssueHl);
//		hit.setAffiliationHighlights(affiliationHl);
//		hit.setKeywordHighlights(keywordsHl);
//		hit.setMeshMajorHighlights(meshMajorHl);
//		hit.setMeshMinorHighlights(meshMinorHl);
//		hit.setSubstancesHighlights(substancesHl);
//		hit.setHighlightedAbstract(null);
//
//		return hit;
//	}
//
//	/*
//	 * (non-Javadoc)
//	 * 
//	 * @see de.julielab.semedico.core.services.IDocumentService#
//	 * getHighlightedSemedicoDocument(int, java.lang.String)
//	 */
//	@Override
//	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(ISearchServerDocument serverDoc) {
//		SemedicoDocument semedicoDoc = getSemedicoDocument(serverDoc);
//		Highlight highlightedTitle = highlightingService.getTitleHighlight(serverDoc);
//		Highlight highlightedAbstract = highlightingService.getHighlightedAbstract(serverDoc);
//
//		List<Highlight> textContentHighlights = highlightingService.getBestTextContentHighlights(serverDoc, -1,
//				IIndexInformationService.TITLE, IIndexInformationService.ABSTRACT);
//
//		HighlightedSemedicoDocument hlDoc = new HighlightedSemedicoDocument(semedicoDoc);
//		hlDoc.setTitleHighlight(highlightedTitle);
//		hlDoc.setHighlightedAbstract(highlightedAbstract);
//		hlDoc.setTextContentHighlights(textContentHighlights);
//
//		return hlDoc;
//	}
//
//	private SemedicoDocument getSemedicoDocument(ISearchServerDocument solrDoc) {
//		if (solrDoc == null) {
//			return null;
//		}
//
//		long time = System.currentTimeMillis();
//
//		SemedicoDocument semedicoDoc = null;
//		// documentCacheService
//		// .getCachedDocument(pmid);
//
//		if (semedicoDoc == null) {
//			semedicoDoc = new SemedicoDocument(loggerSource.getLogger(SemedicoDocument.class), solrDoc.getId(),
//					solrDoc.getIndexType());
//
//			readPubMedId(semedicoDoc, solrDoc);
//			readPmcId(semedicoDoc, solrDoc);
//			readAbstract(semedicoDoc, solrDoc);
//			readTitle(semedicoDoc, solrDoc);
//			readPublications(semedicoDoc, solrDoc);
//			determinePubType(semedicoDoc, solrDoc);
//
//			readPublicationTypes(semedicoDoc, solrDoc);
//			readAuthors(semedicoDoc, solrDoc);
//
//			time = System.currentTimeMillis() - time;
//			// logger.info("Reading document \"{}\" from index took {}ms", pmid,
//			// time);
//
//			// documentCacheService.addDocument(semedicoDoc);
//		}
//		// else
//		// logger.debug("Returned cached semedico document \"{}\".", pmid);
//
//		return semedicoDoc;
//	}
//
//	private String getPmid(ISearchServerDocument solrDoc) {
//		String idString = (String) solrDoc.get(IIndexInformationService.PUBMED_ID).orElse(null);
//		return idString;
//	}
//
//	private String getPmcId(ISearchServerDocument solrDoc) {
//		String idString = (String) solrDoc.get(IIndexInformationService.pmcid).orElse(null);
//		return idString;
//	}
//
//	// TODO continue writing this method
//	@Override
//	public List<HighlightedStatement> getHighlightedStatements(ISearchServerDocument serverDoc) {
//		List<ISearchServerDocument> innerStatementHits = serverDoc.getInnerHits()
//				.get(IIndexInformationService.GeneralIndexStructure.events);
//		if (null == innerStatementHits || innerStatementHits.isEmpty())
//			throw new IllegalStateException("Inner hits for statements in nested ffield "
//					+ IIndexInformationService.GeneralIndexStructure.events
//					+ " should be present but could not be found.");
//
//		List<HighlightedStatement> statementList = new ArrayList<>();
//		SemedicoDocument doc = getSemedicoDocument(serverDoc);
//		for (ISearchServerDocument innerDoc : innerStatementHits) {
//
//			List<String> highlights = innerDoc.getHighlights()
//					.get(IIndexInformationService.GeneralIndexStructure.EventFields.sentence);
//			if (highlights.isEmpty())
//				throw new IllegalStateException("An event hit does not have any highlights");
//
//			// its only one event, it should have exactly one highlight
//			String highlight = highlights.get(0);
//			String sentence = (String) innerDoc
//					.getFieldValue(IIndexInformationService.GeneralIndexStructure.EventFields.sentence).orElse("");
//			String likelihood = (String) innerDoc
//					.getFieldValue(IIndexInformationService.GeneralIndexStructure.EventFields.likelihood).orElse("");
//			List<Object> arguments = innerDoc
//					.getFieldValues(IIndexInformationService.GeneralIndexStructure.EventFields.allarguments)
//					.orElse(Collections.emptyList());
//			// The main event type is "multivalued" because it contains the
//			// concept ID and the actual word string in the document
//			List<Object> predicate = innerDoc
//					.getFieldValues(IIndexInformationService.GeneralIndexStructure.EventFields.maineventtype)
//					.orElse(Collections.emptyList());
//
//			Predicate<Object> isTermIdPredicate = new Predicate<Object>() {
//
//				@Override
//				public boolean apply(Object input) {
//					return conceptService.isConceptID((String) input);
//				}
//
//			};
//			Collection<Object> argumentIds = Collections2.filter(arguments, isTermIdPredicate);
//			Collection<Object> eventTypeId = Collections2.filter(predicate, isTermIdPredicate);
//			HighlightedStatement statement = new HighlightedStatement();
//			statement.setParentDocument(doc);
//
//		}
//		return null;
//	}
//}