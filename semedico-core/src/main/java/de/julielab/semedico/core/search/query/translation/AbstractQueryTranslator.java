package de.julielab.semedico.core.search.query.translation;

import java.util.*;
import java.util.stream.Collectors;

import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.search.query.ISemedicoQuery;
import de.julielab.semedico.core.services.interfaces.IServiceReconfigurationHub;
import org.apache.tapestry5.ioc.services.SymbolSource;
import org.slf4j.Logger;

import com.google.common.collect.Sets;


/**
 * Used as a common extension point for query translators.
 *
 * @see {@link de.julielab.semedico.core.search.services.SemedicoSearchModule#buildQueryTranslatorChain(List, IServiceReconfigurationHub, SymbolSource)}
 */
public abstract class AbstractQueryTranslator<Q extends ISemedicoQuery> implements IQueryTranslator<Q> {
	protected Set<String> applicableIndexes = Collections.emptySet();
	protected Set<String> applicableFields = Collections.emptySet();
	private String name;
	protected Logger log;

	public AbstractQueryTranslator(Logger log, String name) {
		this.log = log;
		this.name = name;
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
	protected boolean applies(String index, List<SemedicoIndexField> searchedFields) {
		boolean applies = applicableIndexes.contains(index);
		Set<String> requestedFieldsSet = searchedFields.stream().map(SemedicoIndexField::getName).collect(Collectors.toSet());
		// if there are "no searched fields" it actually means there is no
		// search field restriction
		applies = applies
				&& (requestedFieldsSet == null || requestedFieldsSet.isEmpty() || !Sets.intersection(requestedFieldsSet, applicableFields).isEmpty());
		log.trace(
				"Translator {} does {}apply to requested index {} and requested search fields {}.",
				name, applies ? "" : "not ", index, searchedFields );
		return applies;
	}
}
