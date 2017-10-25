package de.julielab.semedico.core.search.query.translation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import com.google.common.collect.Sets;

public abstract class AbstractQueryTranslator implements IQueryTranslator {
	protected Set<SearchTask> applicableTasks = Collections.emptySet();
	protected Set<String> applicableIndexes = Collections.emptySet();
	protected Set<String> applicableFields = Collections.emptySet();
	private String name;
	protected Logger log;

	public AbstractQueryTranslator(Logger log, String name) {
		this.log = log;
		this.name = name;
	}

	protected void addApplicableTask(SearchTask... tasks) {
		if (applicableTasks.isEmpty())
			applicableTasks = new HashSet<>();
		for (int i = 0; i < tasks.length; i++) {
			SearchTask task = tasks[i];
			applicableTasks.add(task);

		}
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
	 * @param string
	 * @return
	 */
	protected boolean applies(SearchTask task, String index,
			Set<String> requestedFields) {
		boolean applies = (applicableTasks.contains(task))
				&& (applicableIndexes.contains(index));
		// if there are "no requested fields" it actually means there is no search field restriction
		applies = applies
				&& (requestedFields.isEmpty() || !Sets.intersection(requestedFields,
						applicableFields).isEmpty());
		log.trace(
				"Translator {} does {}apply to requested tasks {}, requested index types {} and requested search fields {}.",
				new Object[] { name, applies ? "" : "not ", task, index,
						requestedFields });
		return applies;
	}
}
