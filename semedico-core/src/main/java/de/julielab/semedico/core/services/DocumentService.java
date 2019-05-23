package de.julielab.semedico.core.services;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.LoggerSource;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.Publication;
import de.julielab.semedico.core.search.components.data.Highlight;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument.AuthorHighlight;
import de.julielab.semedico.core.search.components.data.SemedicoDocument;
import de.julielab.semedico.core.search.interfaces.IHighlightingService;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.GeneralIndexStructure;

public class DocumentService implements IDocumentService {
	private static final String REVIEW = "Review";

	private final IHighlightingService highlightingService;

	private final LoggerSource loggerSource;

	public DocumentService(IHighlightingService kwicService, LoggerSource loggerSource) {
		this.highlightingService = kwicService;
		this.loggerSource = loggerSource;
	}

	// Diese Read-Funktionen können für den einen Aufruf, wo sie benötigt
	// werden, auch anders gelöst werden
	// Lesbarkeit vom Code

	protected void readPubMedId(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		document.setPmid(getPmid(elasticDoc));
	}

	protected void readPmcId(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		document.setPmcid(getPmcId(elasticDoc));
	}

	protected void readTitle(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		document.setTitle((String) elasticDoc.get(IIndexInformationService.GeneralIndexStructure.title));
	}

	protected void readAbstract(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		document.setAbstractText((String) elasticDoc.get(IIndexInformationService.GeneralIndexStructure.abstracttext));
	}

	protected void determinePubType(SemedicoDocument document, ISearchServerDocument elasticDoc)
			throws NumberFormatException {
		if (elasticDoc.getIndexType().equals(IIndexInformationService.Indexes.DocumentTypes.MEDLINE)) {
			if (document.getAbstractText() != null && document.getAbstractText().length() > 0) {
				document.setType(SemedicoDocument.TYPE_ABSTRACT);
			} else {
				document.setType(SemedicoDocument.TYPE_TITLE);
			}
		} else if (elasticDoc.getIndexType().equals(IIndexInformationService.Indexes.DocumentTypes.PMC)) {
			document.setType(SemedicoDocument.TYPE_FULL_TEXT);
		}
	}

	protected void readAuthors(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		if (elasticDoc.get(IIndexInformationService.GeneralIndexStructure.authors) == null) {
			return;
		}

		List<Object> authors = elasticDoc.getFieldValues(IIndexInformationService.GeneralIndexStructure.authors);
		List<Object> affiliations = elasticDoc.getFieldValues(IIndexInformationService.GeneralIndexStructure.affiliation);

		for (int i = 0; i < authors.size(); ++i) {
			String authorString = (String) authors.get(i);
			Author author = new Author();
			String[] names = authorString.split(",");

			if (names.length == 2) {
				author.setForename(names[1]);
				author.setLastname(names[0]);
			} else {
				author.setLastname(authorString);
			}

			if (affiliations != null && i < affiliations.size()) {
				String affiliationString = (String) affiliations.get(i);
				author.setAffiliation(affiliationString);
			}

			document.getAuthors().add(author);
		}
	}

	protected void readPublicationTypes(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		String pubtype = elasticDoc.get(IIndexInformationService.GeneralIndexStructure.pubtype);
		if (pubtype != null && pubtype.contains(REVIEW)) {
			document.setReview(true);
		}
	}

	protected void readPublications(SemedicoDocument document, ISearchServerDocument elasticDoc) {
		Publication publication = new Publication();
		String title = elasticDoc.get(IIndexInformationService.GeneralIndexStructure.journaltitle);

		if (!StringUtils.isBlank(title)) {
			String volume = elasticDoc.get(IIndexInformationService.GeneralIndexStructure.journalvolume);
			String issue = elasticDoc.get(IIndexInformationService.GeneralIndexStructure.journalissue);
			String pages = elasticDoc.get(IIndexInformationService.GeneralIndexStructure.journalpages);

			publication.setTitle(title);
			if (!StringUtils.isBlank(volume)) {
				publication.setVolume(volume);
			}
			if (!StringUtils.isBlank(issue)) {
				publication.setIssue(issue);
			}
			publication.setPages(pages);
		}

		String dateString = (String) elasticDoc.get(IIndexInformationService.GeneralIndexStructure.date);
		if (!StringUtils.isBlank(dateString)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try {
				date = sdf.parse(dateString);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			publication.setDate(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);

			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);

			if (elasticDoc.getIndexType().equals(IIndexInformationService.Indexes.DocumentTypes.PMC)
					&& month == Calendar.JANUARY && day == 1) {
				publication.setDateComplete(false);
			}
		}

		document.setPublication(publication);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IDocumentService#getHitListDocument
	 * (org.apache.solr.common.SolrDocument, java.util.Map)
	 */
	@Override
	public HighlightedSemedicoDocument getHitListDocument(ISearchServerDocument serverDoc) {
		SemedicoDocument semedicoDoc = getSemedicoDocument(serverDoc);

		BibliographyEntry currentEntry = new BibliographyEntry(); // das soll
																	// Rückgabewert
																	// werden

		currentEntry.setArticleTitle(semedicoDoc.getTitle());
		currentEntry.setAbstractText(semedicoDoc.getAbstractText());
		currentEntry.setDocId(semedicoDoc.getDocId());
		currentEntry.setPmid(semedicoDoc.getPmid());
		currentEntry.setPmcid(semedicoDoc.getPmcid());
		currentEntry.setAuthors(semedicoDoc.getAuthors());

		Publication publication = new Publication();
		publication.setTitle(semedicoDoc.getPublication().getTitle());

		publication.setPages(semedicoDoc.getPublication().getVolume());
		publication.setIssue(semedicoDoc.getPublication().getIssue());
		publication.setDate(semedicoDoc.getPublication().getDate());
		publication.setPages(semedicoDoc.getPublication().getPages());

		currentEntry.setPublication(publication);
		currentEntry.setExternalLinks(semedicoDoc.getExternalLinks());

		HighlightedSemedicoDocument hit = new HighlightedSemedicoDocument(semedicoDoc); // Rückgabewert

		int maxContentHighlights = 3;
		Highlight titleHighlight = highlightingService.getTitleHighlight(serverDoc);

		List<Highlight> textContentHighlights = highlightingService.getBestTextContentHighlights(serverDoc,
				maxContentHighlights, IIndexInformationService.GeneralIndexStructure.title);

		List<AuthorHighlight> authorHighlights = highlightingService.getAuthorHighlights(serverDoc);

		List<Highlight> journalTitleHighlight = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.journaltitle, false, true, false);

		List<Highlight> journalVolumeHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.journalvolume, false, true);

		List<Highlight> journalIssueHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.journalissue, false, true);

		List<Highlight> affiliationHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.affiliation, true);

		List<Highlight> keywordsHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.keywords, true);

		List<Highlight> meshMajorHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.meshmajor, true, false, false, true);

		List<Highlight> meshMinorHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.meshminor, true, false, false, true);

		List<Highlight> substancesHl = highlightingService.getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.substances, true, false, false, true);

		hit.setTitleHighlight(titleHighlight);
		hit.setTextContentHighlights(textContentHighlights);
		hit.setAuthorHighlights(authorHighlights);
		hit.setJournalHighlight(journalTitleHighlight.get(0));
		hit.setJournalVolumeHighlights(journalVolumeHl);
		hit.setJournalIssueHighlights(journalIssueHl);
		hit.setAffiliationHighlights(affiliationHl);
		hit.setKeywordHighlights(keywordsHl);
		hit.setMeshMajorHighlights(meshMajorHl);
		hit.setMeshMinorHighlights(meshMinorHl);
		hit.setSubstancesHighlights(substancesHl);
		hit.setHighlightedAbstract(null);

		return hit;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IDocumentService#
	 * getHighlightedSemedicoDocument(int, java.lang.String)
	 */
	@Override
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(ISearchServerDocument serverDoc) {
		SemedicoDocument semedicoDoc = getSemedicoDocument(serverDoc);
		Highlight highlightedTitle = highlightingService.getTitleHighlight(serverDoc);
		Highlight highlightedAbstract = highlightingService.getHighlightedAbstract(serverDoc);

		List<Highlight> textContentHighlights = highlightingService.getBestTextContentHighlights(serverDoc, -1,
				IIndexInformationService.GeneralIndexStructure.title, IIndexInformationService.GeneralIndexStructure.abstracttext);

		HighlightedSemedicoDocument hlDoc = new HighlightedSemedicoDocument(semedicoDoc);
		hlDoc.setTitleHighlight(highlightedTitle);
		hlDoc.setHighlightedAbstract(highlightedAbstract);
		hlDoc.setTextContentHighlights(textContentHighlights);

		return hlDoc;
	}

	private SemedicoDocument getSemedicoDocument(ISearchServerDocument elasticDoc) {
		if (elasticDoc == null) {
			return null;
		}
		SemedicoDocument semedicoDoc = null;

		semedicoDoc = new SemedicoDocument(loggerSource.getLogger(SemedicoDocument.class), elasticDoc.getId(),
				elasticDoc.getIndexType());

		semedicoDoc.setPmid(getPmid(elasticDoc));
		readPmcId(semedicoDoc, elasticDoc);
		readAbstract(semedicoDoc, elasticDoc);
		readTitle(semedicoDoc, elasticDoc);
		readPublications(semedicoDoc, elasticDoc);
		determinePubType(semedicoDoc, elasticDoc);

		readPublicationTypes(semedicoDoc, elasticDoc);
		readAuthors(semedicoDoc, elasticDoc);
		

		return semedicoDoc;
	}

	private String getPmid(ISearchServerDocument elasticDoc) {
		return elasticDoc.get(IIndexInformationService.GeneralIndexStructure.pmid);
	}

	private String getPmcId(ISearchServerDocument elasticDoc) {
		return elasticDoc.get(GeneralIndexStructure.pmcid);
	}

}