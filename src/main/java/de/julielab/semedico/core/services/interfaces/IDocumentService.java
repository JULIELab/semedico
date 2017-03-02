package de.julielab.semedico.core.services.interfaces;

import java.util.List;
import java.util.Map;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.HighlightedStatement;
import de.julielab.semedico.core.search.components.data.SemedicoDocument;

public interface IDocumentService {

	/**
	 * Only used by related article service to get an unhighlighted document
	 * from which only the title, publication title and review status a
	 * currrently used
	 * 
	 * @param pmid
	 * @return
	 */
	@Deprecated
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
	HighlightedSemedicoDocument getHitListDocument(
		ISearchServerDocument solrDoc,
		Map<String, Map<String, List<String>>> highlighting);

	/**
	 * Only used by Article to get a fully highlighted abstract.
	 * 
	 * @param pubMedId
	 * @param originalQueryString
	 * @return
	 */
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(
			ISearchServerDocument solrDoc);
	
	public List<HighlightedStatement> getHighlightedStatements(ISearchServerDocument serverDoc);
}