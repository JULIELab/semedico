package de.julielab.semedico.core.search.query.translation;

import java.util.List;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;

public interface IMetaQueryTranslator {

	SearchServerQuery combine(List<SearchServerQuery> queries);
}
