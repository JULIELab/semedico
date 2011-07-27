package de.julielab.semedico.suggestions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTermSuggestionStream;

/**
 * An interface for services responsible to create an index providing term
 * suggestions and retrieving these suggestions given a user delivered string.
 * <p>
 * This is typically done by N-Gram-analyzing term name and synonyms. The
 * service is used for automatically delivering search query suggestions
 * beginning with or containing the string a user has already typed in a text
 * input field.
 * </p>
 * 
 * @author faessler
 * 
 */
public interface ITermSuggestionService {

	public final static class Fields {
		// The term name/synonym for which can be searched.
		public final static String SUGGESTION_TEXT = "suggestionText";
		// The term id the above suggestion text stands for.
		public final static String TERM_ID = "termId";
		// The facet IDs the term is contained in.
		public final static String FACETS = "facets";
		// A string representing other synonyms than the suggestion string for
		// this term.
		public final static String TERM_SYNONYMS = "synonyms";
	}

	/**
	 * Returns a list of facet grouped suggestions for the user delivered string
	 * <code>termFragment</code>.
	 * 
	 * @param termFragment
	 *            User delivered string for which suggestions are searched.
	 * @param facets
	 *            List of <code>Facet</code>s for which suggestions shall be
	 *            returned.
	 * @return A list of <code>FacetTermSuggestionStream</code> objects, each
	 *         delivering the term suggestions corresponding to
	 *         <code>termFragment</code> in a particular facet in
	 *         <code>facets</code>.
	 * @throws IOException
	 */
	public List<FacetTermSuggestionStream> getSuggestionsForFragment(
			String termFragment, List<Facet> facets);

	public void createSuggestionIndex(String path) throws IOException,
			SQLException;
}