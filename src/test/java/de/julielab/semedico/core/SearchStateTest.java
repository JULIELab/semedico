package de.julielab.semedico.core;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import de.julielab.lucene.ParseTree;
import de.julielab.lucene.Parser;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

public class SearchStateTest {
	SearchState state;

	@Test
	public void testRemoveTerm() throws Exception {
		newState();
		state.removeTerm("foo");
		assertEquals("(x AND y)", state.getParseTree().toString());
		assertEquals(true, state.getQueryTerms().get("foo").isEmpty());
	}

	@Test
	public void testcorrectSpelling() throws Exception {
		newState();
		state.correctSpelling("foo", "bar");
		assertEquals("(bar OR (x AND y))", state.getParseTree().toString());
		FacetTerm term = (FacetTerm) state.getQueryTerms().get("bar").toArray()[0];
		assertEquals("fooID", term.getId());
	}

	private SearchState newState() throws Exception {
		state = new SearchState();
		state.setQueryTerms(getQueryTerms());
		state.setParseTree(getParseTree());
		return state;
	}

	private Multimap<String, IFacetTerm> getQueryTerms() {
		Multimap<String, IFacetTerm> mmap = LinkedHashMultimap.create();
		IFacetTerm term = new FacetTerm("fooID", "fooName");
		mmap.put("foo", term);
		return mmap;
	}

	private ParseTree getParseTree() throws Exception {
		String toParse = "foo OR (x y)";
		Parser parser = new Parser(toParse);
		return parser.parse();
	}
}