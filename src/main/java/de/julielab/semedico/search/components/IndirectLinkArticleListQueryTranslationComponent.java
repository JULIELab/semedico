package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.apache.tapestry5.services.ApplicationStateManager;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.query.IQueryTranslationService;

public class IndirectLinkArticleListQueryTranslationComponent implements
		ISearchComponent {

	private final IQueryTranslationService queryTranslationService;
	private final ApplicationStateManager asm;

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IndirectLinkArticleListQueryTranslation {
	}

	public IndirectLinkArticleListQueryTranslationComponent(ApplicationStateManager asm,
			IQueryTranslationService queryTranslationService) {
		this.asm = asm;
		this.queryTranslationService = queryTranslationService;

	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		SearchNodeSearchCommand nodeCmd = searchCarrier.searchCmd.nodeCmd;
		if (null == nodeCmd)
			throw new IllegalArgumentException("An instance of "
					+ SearchNodeSearchCommand.class.getName() + " is expected.");
		List<Multimap<String,IFacetTerm>> searchNodes = nodeCmd.searchNodes;
		IFacetTerm linkTerm = nodeCmd.linkTerm;
		int nodeIndex = nodeCmd.nodeIndex;
		
		String solrQuery = queryTranslationService.createQueryForBTermSearchNode(searchNodes, linkTerm, nodeIndex);
		
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		if (null == solrCmd) {
			solrCmd = new SolrSearchCommand();
			searchCarrier.solrCmd = solrCmd;
		}
		solrCmd.solrQuery = solrQuery;
		
		SearchState searchState = asm.get(SearchState.class);
		searchState.setBTermQueryString(nodeIndex, solrQuery);
		
		return false;
	}

}
