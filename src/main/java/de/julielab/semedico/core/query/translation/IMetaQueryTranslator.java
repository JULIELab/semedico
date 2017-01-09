package de.julielab.semedico.core.query.translation;

import java.util.List;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.concepts.Concept;

public interface IMetaQueryTranslator {

	SearchServerQuery combine(List<SearchServerQuery> queries, List<Concept> facetFilterConcepts);
}
