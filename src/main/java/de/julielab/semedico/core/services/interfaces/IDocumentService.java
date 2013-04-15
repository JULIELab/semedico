package de.julielab.semedico.core.services.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.SemedicoDocument;

public interface IDocumentService {

	/**
	 * Only used by related article service to get an unhighlighted document
	 * from which only the title, publication title and review status a
	 * currrently used
	 * 
	 * @param pmid
	 * @return
	 */
	public SemedicoDocument getSemedicoDocument(int pmid);

	// public SemedicoDocument getRelatedArticleDocument(int pmid);
	// public SemedicoDocument readDocumentWithPubmedId(int pmid);
	/**
	 * Just a conversion function from a document search response with separate
	 * SolrDocs and corresponding Highlighting to a DocumentHit object used in
	 * the ResultList page.
	 * 
	 * @param solrDoc
	 * @param highlighting
	 * @return
	 */
	DocumentHit getHitListDocument(SolrDocument solrDoc,
			Map<String, Map<String, List<String>>> highlighting);

	/**
	 * Only used by Article to get a fully highlighted abstract.
	 * 
	 * @param pubMedId
	 * @param originalQueryString
	 * @return
	 */
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(
			SolrDocument solrDoc, Map<String, List<String>> docHighlights);
}