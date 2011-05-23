package de.julielab.semedico.search;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.OpenBitSet;

public interface IDocumentSetLimitizerService {

	/**
	 * Unmarks (arbitrary) documents as found until only <code>limit</code>
	 * documents are still marked.<br>
	 * The scoreDocs are treated separately and accounted for. The scoreDocs are
	 * unmarked in the resulting BitSet and their amount subtracted from
	 * <code>limit</code> (thus, exactly <code>limit</code> documents are still
	 * marked afterwards).
	 * 
	 * @param documents
	 * @param scoreDocs
	 * @param limit
	 * @return
	 */
	public OpenBitSet limitDocumentSetWithIncludedScoreDocs(
			OpenBitSet documents, ScoreDoc[] scoreDocs, int limit);
}
