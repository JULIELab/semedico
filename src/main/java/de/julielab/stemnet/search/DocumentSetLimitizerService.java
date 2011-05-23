package de.julielab.stemnet.search;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.OpenBitSet;

public class DocumentSetLimitizerService implements IDocumentSetLimitizerService{

	@Override
	public OpenBitSet limitDocumentSetWithIncludedScoreDocs(OpenBitSet documents, ScoreDoc[] scoreDocs, int limit) {
		OpenBitSet scoreDocSet = new OpenBitSet();
		for( ScoreDoc scoreDoc: scoreDocs)
			scoreDocSet.set(scoreDoc.doc);
		
		OpenBitSet docs = new OpenBitSet();
		docs.union(documents);
		docs.remove(scoreDocSet);
		
		int nextSetBit = -1;
		int bitsToClear = (int) docs.cardinality() - (limit - scoreDocs.length);
		
		for( int i = 0; i < bitsToClear; i++ ){
			nextSetBit = docs.nextSetBit(nextSetBit+1);
			if( nextSetBit > -1)
				docs.clear(nextSetBit);
		}
		
		docs.union(scoreDocSet);
		return docs;
	}

}
