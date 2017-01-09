package de.julielab.semedico.core.services;

// TODO KLASSE AUFRÄUMEN

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ioc.LoggerSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import de.julielab.elastic.query.components.data.ISearchServerDocument;

import com.google.gson.Gson;


import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.HighlightedSemedicoDocument.AuthorHighlight;
import de.julielab.semedico.core.HighlightedSemedicoDocument.Highlight;
import de.julielab.semedico.core.search.interfaces.IHighlightingService;
import de.julielab.semedico.core.Publication;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IDocumentCacheService;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
//import example_gson.BibliographyEntry;

public class DocumentService implements IDocumentService
{
	private static final String REVIEW = "Review";

	private static Logger logger = LoggerFactory.getLogger(DocumentService.class);

	private final IDocumentCacheService documentCacheService;

	private final IHighlightingService highlightingService;

	private final LoggerSource loggerSource;

	public DocumentService(
			IDocumentCacheService documentCacheService,
			IHighlightingService kwicService,
			LoggerSource loggerSource)
	{
		this.documentCacheService = documentCacheService;
		this.highlightingService = kwicService;
		this.loggerSource = loggerSource;
	}

	// Diese Read-Funktionen können für den einen Aufruf, wo sie benötigt werden, auch anders gelöst werden
	// Lesbarkeit vom Code
	
	protected void readPubMedId(SemedicoDocument document, ISearchServerDocument solrDoc)
	{
		document.setPmid(getPmid(solrDoc));
	}

	protected void readPmcId(SemedicoDocument document, ISearchServerDocument solrDoc)
	{
		document.setPmcid(getPmcId(solrDoc));
	}

	protected void readTitle(SemedicoDocument document, ISearchServerDocument solrDoc)
	{	
		document.setTitle((String) solrDoc.get(IIndexInformationService.TITLE));
	}

	protected void readAbstract(SemedicoDocument document, ISearchServerDocument solrDoc)
	{
		document.setAbstractText((String) solrDoc.get(IIndexInformationService.ABSTRACT));
	}

	protected void determinePubType(SemedicoDocument document,ISearchServerDocument solrDoc)
		throws NumberFormatException
	{
		if (solrDoc.getIndexType().equals(IIndexInformationService.Indexes.DocumentTypes.medline))
		{
			if (document.getAbstractText() != null && document.getAbstractText().length() > 0)
			{
				document.setType(SemedicoDocument.TYPE_ABSTRACT);
			}
			else
			{
				document.setType(SemedicoDocument.TYPE_TITLE);
			}
		}
		else if (solrDoc.getIndexType().equals(IIndexInformationService.Indexes.DocumentTypes.pmc))
		{
			document.setType(SemedicoDocument.TYPE_FULL_TEXT);
		}
	}

	protected void readAuthors(SemedicoDocument document, ISearchServerDocument solrDoc)
	{
		if (solrDoc.get(IIndexInformationService.AUTHORS) == null)
		{
			return;
		}
		
		List<Object> authors = solrDoc.getFieldValues(IIndexInformationService.AUTHORS);
		List<Object> affiliations = solrDoc.getFieldValues(IIndexInformationService.GeneralIndexStructure.affiliation);
		
		for (int i = 0; i < authors.size(); ++i)
		{
			String authorString = (String) authors.get(i);
			Author author = new Author();
			String[] names = authorString.split(",");
			
			if (names.length == 2)
			{
				author.setForename(names[1]);
				author.setLastname(names[0]);
			}
			else
			{
				author.setLastname((String) authorString);
			}

			if (affiliations != null && i < affiliations.size())
			{
				String affiliationString = (String) affiliations.get(i);
				author.setAffiliation(affiliationString);
			}

			document.getAuthors().add(author);
		}
	}

	protected void readPublicationTypes(SemedicoDocument document, ISearchServerDocument solrDoc)
	{
		String pubtype = solrDoc.get(IIndexInformationService.GeneralIndexStructure.pubtype);
		if (pubtype != null && pubtype.contains(REVIEW))
		{
			document.setReview(true);
		}
	}

	protected void readPublications(SemedicoDocument document, ISearchServerDocument solrDoc)
	{
		Publication publication = new Publication();
		String title = solrDoc.get(IIndexInformationService.GeneralIndexStructure.journaltitle);
		
		if (!StringUtils.isBlank(title))
		{
			String volume = solrDoc.get(IIndexInformationService.GeneralIndexStructure.journalvolume);
			String issue = solrDoc.get(IIndexInformationService.GeneralIndexStructure.journalissue);
			String pages = solrDoc.get(IIndexInformationService.GeneralIndexStructure.journalpages);

			publication.setTitle(title);
			if (!StringUtils.isBlank(volume))
			{
				publication.setVolume(volume);
			}
			if (!StringUtils.isBlank(issue))
			{
				publication.setIssue(issue);
			}
			publication.setPages(pages);
		}

		String dateString = (String) solrDoc.get(IIndexInformationService.DATE);
		if (!StringUtils.isBlank(dateString))
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date date = null;
			try
			{
				date = sdf.parse(dateString);
			}
			catch (ParseException e)
			{
				e.printStackTrace();
			}
			publication.setDate(date);
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			
			int month = cal.get(Calendar.MONTH);
			int day = cal.get(Calendar.DAY_OF_MONTH);
			
			if (
				solrDoc.getIndexType().equals(IIndexInformationService.Indexes.DocumentTypes.pmc)
				&&
				month == Calendar.JANUARY && day == 1
				)
			{
				publication.setDateComplete(false);
			}
		}

		document.setPublication(publication);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.core.services.IDocumentService#getSemedicoDocument
	 * (int)
	 */
	@Override
	@Deprecated
	public SemedicoDocument getSemedicoDocument(int pmid)
	{
		// SolrDocument solrDoc = getSolrDocById(pmid);
		// SemedicoDocument semedicoDoc = getSemedicoDocument(solrDoc);
		// return semedicoDoc;
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
	public HighlightedSemedicoDocument getHitListDocument(
			ISearchServerDocument serverDoc,
			Map<String,Map<String,List<String>>> highlighting)
	{
		SemedicoDocument semedicoDoc = getSemedicoDocument(serverDoc);
			
		BibliographyEntry currentEntry = new BibliographyEntry();	// das soll Rückgabewert werden
		
		currentEntry.setArticleTitle(			semedicoDoc.getTitle());
		currentEntry.setAbstractText(			semedicoDoc.getAbstractText());
		currentEntry.setDocId(					semedicoDoc.getDocId());
		currentEntry.setPmid(					semedicoDoc.getPmid());
		currentEntry.setPmcid(					semedicoDoc.getPmcid());
		currentEntry.setAuthors(				semedicoDoc.getAuthors());
		
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
		
		List<Highlight> textContentHighlights
			= highlightingService.getBestTextContentHighlights(serverDoc,maxContentHighlights, IIndexInformationService.TITLE);

		List<AuthorHighlight> authorHighlights
			= highlightingService.getAuthorHighlights(serverDoc);
		
		List<Highlight> journalTitleHighlight
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.journaltitle, false, true, false);

		List<Highlight> journalVolumeHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.journalvolume, false, true);
	
		List<Highlight> journalIssueHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.journalissue, false, true);
			
		List<Highlight> affiliationHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.affiliation, true);
		
		List<Highlight> keywordsHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.keywords, true);
		
		List<Highlight> meshMajorHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.meshmajor, true, false, false, true);
		
		List<Highlight> meshMinorHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.meshminor, true, false, false, true);
		
		List<Highlight> substancesHl
			= highlightingService.getFieldHighlights(serverDoc,IIndexInformationService.GeneralIndexStructure.substances, true, false, false, true);
	
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

		return hit;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.core.services.IDocumentService#
	 * getHighlightedSemedicoDocument(int, java.lang.String)
	 */
	@Override
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(ISearchServerDocument serverDoc)
	{
		SemedicoDocument semedicoDoc = getSemedicoDocument(serverDoc);
		Highlight highlightedTitle = highlightingService.getTitleHighlight(serverDoc);
		Highlight highlightedAbstract = highlightingService.getHighlightedAbstract(serverDoc);
		
		List<Highlight> textContentHighlights
			= highlightingService.getBestTextContentHighlights(
					serverDoc,
					-1,
					IIndexInformationService.TITLE,
					IIndexInformationService.ABSTRACT);
		
		HighlightedSemedicoDocument hlDoc = new HighlightedSemedicoDocument(semedicoDoc);
		hlDoc.setTitleHighlight(highlightedTitle);
		hlDoc.setHighlightedAbstract(highlightedAbstract);
		hlDoc.setTextContentHighlights(textContentHighlights);

		return hlDoc;
	}

	private SemedicoDocument getSemedicoDocument(ISearchServerDocument solrDoc)
	{
		if (solrDoc == null)
		{
			return null;
		}
		
		long time = System.currentTimeMillis();

		SemedicoDocument semedicoDoc = null;
		// documentCacheService
		// .getCachedDocument(pmid);
		
		if (semedicoDoc == null)
		{
			semedicoDoc = new SemedicoDocument(
				loggerSource.getLogger(SemedicoDocument.class),
				solrDoc.getId(),
				solrDoc.getIndexType()
				);

			readPubMedId(			semedicoDoc, solrDoc);
			readPmcId(				semedicoDoc, solrDoc);
			readAbstract(			semedicoDoc, solrDoc);
			readTitle(				semedicoDoc, solrDoc);
			readPublications(		semedicoDoc, solrDoc);
			determinePubType(		semedicoDoc, solrDoc);
			
			readPublicationTypes(	semedicoDoc, solrDoc);
			readAuthors(			semedicoDoc, solrDoc);

			time = System.currentTimeMillis() - time;
			// logger.info("Reading document \"{}\" from index took {}ms", pmid,
			// time);

			// documentCacheService.addDocument(semedicoDoc);
		}
		// else
		// logger.debug("Returned cached semedico document \"{}\".", pmid);

		return semedicoDoc;
	}

	private String getPmid(ISearchServerDocument solrDoc)
	{
		String idString = (String) solrDoc.get(IIndexInformationService.PUBMED_ID);
		return idString;
	}

	private String getPmcId(ISearchServerDocument solrDoc)
	{
		String idString = (String) solrDoc.get(IIndexInformationService.pmcid);
		return idString;
	}
}