package de.julielab.semedico.core.services.interfaces;

import java.util.List;
import java.util.Map;

import org.apache.solr.common.SolrDocument;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.SemedicoDocument;

public interface IDocumentService {

	public SemedicoDocument getSemedicoDocument(int pmid);
//	public SemedicoDocument getRelatedArticleDocument(int pmid);
//	public SemedicoDocument readDocumentWithPubmedId(int pmid);
	/**
	 * @param solrDoc
	 * @param highlighting
	 * @return
	 */
	DocumentHit getHitListDocument(SolrDocument solrDoc,
			Map<String, Map<String, List<String>>> highlighting);
	/**
	 * @param pubMedId
	 * @param originalQueryString 
	 * @return
	 */
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(int pubMedId, String originalQueryString);
}