package de.julielab.semedico.search;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.util.OpenBitSet;
import org.junit.Before;
import org.junit.Test;

import de.julielab.semedico.search.DocumentSetLimitizerService;
import de.julielab.semedico.search.IDocumentSetLimitizerService;

public class DocumentSetLimitizerServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testLimitizeDocumentSetWithIncludedScoreDocs(){
		IDocumentSetLimitizerService limitizer = new DocumentSetLimitizerService();
		OpenBitSet docs = new OpenBitSet();
		docs.set(0);
		docs.set(1);
		docs.set(2);
		docs.set(3);
		docs.set(5);
		docs.set(7);
		docs.set(9);
		docs.set(11);
		
		ScoreDoc[] scoreDocs = new ScoreDoc[3];
		scoreDocs[0] = new ScoreDoc(1, 2);
		scoreDocs[1] = new ScoreDoc(3, 1.5f);
		scoreDocs[2] = new ScoreDoc(7, 1.2f);
		
		OpenBitSet limitedDocs = limitizer.limitDocumentSetWithIncludedScoreDocs(docs, scoreDocs, 5);
		assertEquals(5, limitedDocs.cardinality());
		assertTrue(docs.get(1));
		assertTrue(docs.get(3));
		assertTrue(docs.get(7));
	}
}
