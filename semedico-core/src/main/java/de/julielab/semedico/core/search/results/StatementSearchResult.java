package de.julielab.semedico.core.search.results;

import de.julielab.semedico.core.search.components.data.HighlightedStatement;

import java.util.List;

public class StatementSearchResult extends SemedicoESSearchResult {
	public List<HighlightedStatement> statements;
}
