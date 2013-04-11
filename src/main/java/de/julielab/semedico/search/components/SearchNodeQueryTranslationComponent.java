package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.semedico.query.IQueryTranslationService;

/**
 * Should be largely obsolete as soon as there is a single SemedicoQuery class,
 * propably the current ParseTree class. This class should be able to produce
 * the correct Solr query on its own. It will even not be necessary to store the
 * query in the session since the SemedicoQuery object may save that itself.
 * There could also be an additional method for temporary structure changes for
 * the conjunction with the link term, when needed.
 * 
 * @author faessler
 * 
 */
public class SearchNodeQueryTranslationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface SearchNodeQueryTranslation {
	}

	private final IQueryTranslationService queryTranslationService;

	public SearchNodeQueryTranslationComponent(
			IQueryTranslationService queryTranslationService) {
		this.queryTranslationService = queryTranslationService;

	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		SemedicoSearchCommand searchCmd = searchCarrier.searchCmd;
		if (null == searchCmd || null == searchCmd.nodeCmd)
			throw new IllegalArgumentException("An instance of "
					+ SearchNodeSearchCommand.class.getName()
					+ " is expected but was not given.");
		SearchNodeSearchCommand nodeCmd = searchCmd.nodeCmd;
		if (0 > nodeCmd.nodeIndex || null == nodeCmd.searchNodes)
			throw new IllegalArgumentException(
					"The given "
							+ SearchNodeSearchCommand.class.getName()
							+ " has either an invalid node index or the search nodes are null.");
		String solrQuery = null;
		if (null == nodeCmd.linkTerm)
			solrQuery = queryTranslationService.createQueryForSearchNode(
					nodeCmd.searchNodes, nodeCmd.nodeIndex);
		else
			solrQuery = queryTranslationService.createQueryForBTermSearchNode(
					nodeCmd.searchNodes, nodeCmd.linkTerm, nodeCmd.nodeIndex);

		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		if (null == solrCmd) {
			solrCmd = new SolrSearchCommand();
			searchCarrier.solrCmd = solrCmd;
		}
		solrCmd.solrQuery = solrQuery;

		return false;
	}

}
