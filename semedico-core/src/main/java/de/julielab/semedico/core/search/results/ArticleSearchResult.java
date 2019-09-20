package de.julielab.semedico.core.search.results;

import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;

public class ArticleSearchResult extends SemedicoESSearchResult {
	/**
	 * The single document (or <tt>null</tt>) resulting from an article search.
	 */
	public HighlightedSemedicoDocument article;
}
