package de.julielab.semedico.core.search.query.translation;

import java.util.Collection;
import java.util.List;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.search.SearchScope;

public interface IMetaQueryTranslator {

	SearchServerQuery combine(List<SearchServerQuery> queries, Collection<SearchScope> searchScopes);
}
