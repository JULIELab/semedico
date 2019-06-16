package de.julielab.scicopia.core.parsing;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import de.julielab.semedico.core.search.query.QueryToken;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ChunkerTest {
	private static DisambiguatingChunker chunker;

	@BeforeClass
	public static void setup() {
		Multimap<String, String> dict = MultimapBuilder.hashKeys(10).arrayListValues(5).build();
		dict.put("regulates", "tid1");
		chunker = new DisambiguatingChunker(dict);
	}

	@Test
	public void testEventTerms() {
		String test = "notindict regulates notindict";
		chunker.match(test, chunker);
		chunker.getMatches();
		chunker.match(test, chunker);
		chunker.getMatches();
		Multimap<QueryToken, String> matches = chunker.getMatches();
		// we have to event terms with a synonym for "regulation"
		assertEquals(1, matches.size());
		for (QueryToken key : matches.keys()) {
			String chunkString = key.getOriginalValue();
			assertEquals("regulates", chunkString);
		}
	}
}
