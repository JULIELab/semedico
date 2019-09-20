package de.julielab.semedico.core.search.results;

import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.util.LazyDisplayGroup;

public class DocumentSearchResult extends SemedicoESSearchResult {
	public LazyDisplayGroup<HighlightedSemedicoDocument> documentHits;
	public long totalNumDocs;
}
