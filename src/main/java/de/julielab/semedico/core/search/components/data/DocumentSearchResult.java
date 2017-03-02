package de.julielab.semedico.core.search.components.data;

import de.julielab.semedico.core.util.LazyDisplayGroup;

public class DocumentSearchResult extends SemedicoSearchResult {
	public LazyDisplayGroup<HighlightedSemedicoDocument> documentHits;
	public long totalNumDocs;
}
