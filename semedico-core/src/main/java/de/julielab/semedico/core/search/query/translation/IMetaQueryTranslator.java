package de.julielab.semedico.core.search.query.translation;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;

import java.util.List;

public interface IMetaQueryTranslator {

	SearchServerQuery combine(List<SearchServerQuery> queries);
}
