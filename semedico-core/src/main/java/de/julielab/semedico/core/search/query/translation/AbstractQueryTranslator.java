package de.julielab.semedico.core.search.query.translation;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import de.julielab.semedico.core.search.query.ISemedicoQuery;
import org.slf4j.Logger;

import com.google.common.collect.Sets;

import de.julielab.semedico.core.search.SearchScope;

public abstract class AbstractQueryTranslator<Q extends ISemedicoQuery> implements IQueryTranslator<Q> {
	protected Set<SearchScope> applicableScopes = Collections.emptySet();
	protected Set<String> applicableIndexes = Collections.emptySet();
	protected Set<String> applicableFields = Collections.emptySet();
	private String name;
	protected Logger log;

	public AbstractQueryTranslator(Logger log, String name) {
		this.log = log;
		this.name = name;
	}

	protected void addApplicableScope(SearchScope... scopes) {
		if (applicableScopes == null || applicableScopes.isEmpty())
			applicableScopes = EnumSet.copyOf(Arrays.asList(scopes));
		else
			applicableScopes.addAll(Arrays.asList(scopes));
	}

	protected void addApplicableIndex(String... indexTypes) {
		if (applicableIndexes.isEmpty())
			applicableIndexes = new HashSet<>();
		for (int i = 0; i < indexTypes.length; i++) {
			String indexType = indexTypes[i];
			applicableIndexes.add(indexType);
		}
	}

	protected void addApplicableField(String... fields) {
		if (applicableFields.isEmpty())
			applicableFields = new HashSet<>();
		for (int i = 0; i < fields.length; i++) {
			String field = fields[i];
			applicableFields.add(field);

		}
	}

	/**
	 * Returns <tt>true</tt> if this translator is applicable to at least one of
	 * the requested tasks and one of the requested indexes and type.
	 * 
	 * @param index
	 * @return
	 */
	protected boolean applies(Set<SearchScope> task, String index, Set<String> requestedFields) {
		boolean applies = (task.isEmpty() || !Sets.intersection(applicableScopes, task).isEmpty())
				&& (applicableIndexes.contains(index));
		// if there are "no requested fields" it actually means there is no
		// search field restriction
		applies = applies
				&& (requestedFields == null || requestedFields.isEmpty() || !Sets.intersection(requestedFields, applicableFields).isEmpty());
		log.trace(
				"Translator {} does {}apply to requested tasks {}, requested index {} and requested search fields {}.",
				name, applies ? "" : "not ", task, index, requestedFields );
		return applies;
	}
}
