package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.ReconfigurableService;

import java.util.List;
import java.util.Map;

/**
 * For experimental purposes, query translators need to be reconfigurable. Thus
 * the {@link ReconfigurableService} interface.
 * 
 * @author faessler
 *
 */
public interface IQueryTranslator<Q extends ISemedicoQuery> extends ReconfigurableService {
	void translate(Q query, List<SearchServerQuery> searchQueries,
			Map<String, SearchServerQuery> namedQueries);
}
