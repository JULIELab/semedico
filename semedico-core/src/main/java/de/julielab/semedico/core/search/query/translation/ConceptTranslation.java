package de.julielab.semedico.core.search.query.translation;

public enum ConceptTranslation {
	/**
	 * Represent concepts with their Semedico-internal concept ID in the query.
	 */
	ID,
	/**
	 * Expand recognized concepts to all their names, i.e. their preferred name
	 * and synonyms for the query. All names will be phrases for exact matches
	 * (possible allowing some slope).
	 */
	EXPANSION_PHRASES,
	/**
	 * Expand recognized concepts to all their names, i.e. their preferred name
	 * and synonyms for the query. The names won't be represented as phrases but
	 * as simple bag of words.
	 */
	EXPANSION_WORDS
}
