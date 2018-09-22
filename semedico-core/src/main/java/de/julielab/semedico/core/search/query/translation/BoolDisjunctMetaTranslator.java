package de.julielab.semedico.core.search.query.translation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import de.julielab.elastic.query.components.data.query.BoolClause;
import de.julielab.elastic.query.components.data.query.BoolClause.Occur;
import de.julielab.elastic.query.components.data.query.BoolQuery;
import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.elastic.query.components.data.query.TermsQuery;
import de.julielab.semedico.core.search.SearchScope;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

/**
 * Creates a simple disjunction from the given queries and adds a filter for the
 * given search scopes (sentences, relations, ...).
 * 
 * @author faessler
 *
 */
public class BoolDisjunctMetaTranslator implements IMetaQueryTranslator {

	// Abstracts and Titles are just included within the base document.
	// Unfortunately, the base documents currently don't have DOC_ABSTRACTS and
	// DOC_TITLES in their scopes. So we must expand them here. Should be changed in
	// the index.
	private static final EnumSet<SearchScope> documentScopes = EnumSet.of(SearchScope.DOC_ABSTRACTS,
			SearchScope.DOC_TITLES);

	/**
	 * Creates a simple disjunction from the given queries. Also adds the given
	 * search scopes as a filter clause, if any are given.
	 * 
	 * @param searchScopes
	 */
	@Override
	public SearchServerQuery combine(List<SearchServerQuery> queries, Collection<SearchScope> searchScopes) {
		if (queries.size() == 1 && (searchScopes == null || searchScopes.isEmpty()))
			return queries.get(0);

		List<BoolClause> clauses = new ArrayList<>(queries.size());
		for (SearchServerQuery serverQuery : queries) {
			BoolClause clause = new BoolClause();
			clause.occur = BoolClause.Occur.MUST;
			clause.addQuery(serverQuery);
			clauses.add(clause);
		}
		if (searchScopes != null && !searchScopes.isEmpty()) {
			TermsQuery scopeQuery = new TermsQuery(
					searchScopes.stream().map(scope -> documentScopes.contains(scope) ? SearchScope.DOCUMENTS : scope)
							.map(SearchScope::name).collect(Collectors.toList()));
			scopeQuery.field = IIndexInformationService.Indices.All.scope;
			BoolClause scopeClause = new BoolClause();
			scopeClause.occur = Occur.FILTER;
			scopeClause.addQuery(scopeQuery);
			clauses.add(scopeClause);
		}
		BoolQuery boolQuery = new BoolQuery();
		boolQuery.clauses = clauses;

		return boolQuery;
	}

}
