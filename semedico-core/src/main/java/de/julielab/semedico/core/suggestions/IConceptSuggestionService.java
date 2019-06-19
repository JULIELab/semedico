package de.julielab.semedico.core.suggestions;

import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.core.facets.Facet;

import java.io.IOException;
import java.util.List;

/**
 * An interface for services responsible to create an index providing term suggestions and retrieving these suggestions
 * given a user delivered string.
 * <p>
 * This is typically done by N-Gram-analyzing term name and synonyms. The service is used for automatically delivering
 * search query suggestions beginning with or containing the string a user has already typed in a text input field.
 * </p>
 * 
 * @author faessler
 * 
 */
public interface IConceptSuggestionService {

	public final static class Fields {
		/**
		 * The term name/synonym for which can be searched.
		 */
		public final static String SUGGESTION_TEXT = "suggestionText";
		/**
		 * The term id the above suggestion text stands for.
		 */
		public final static String TERM_ID = "termId";
		/**
		 * The facet IDs the term is contained in.
		 */
		public final static String FACETS = "facets";
		@Deprecated
		public final static String FACET_NAME = "facetName";
		/**
		 * A string representing other synonyms than the suggestion string for this term.
		 */
		public final static String TERM_SYNONYMS = "synonyms";
		public final static String TERM_PREF_NAME = "preferredName";
		/**
		 * The lowercased suggestion text without NGram-analysis for sorting purposes. Is created automatically by Solr
		 * via an <code>copy-field</code> directive in schema.xml.
		 */
		public final static String SORTING = "sorting";
		/**
		 * The qualifiers of a suggestion item, similar to a "context". For example, genes have their organism as a
		 * qualifier to allow for a more specific search.
		 */
		public static final String QUALIFIERS = "qualifiers";
		/**
		 * The length of the suggestion item (i.e. some synonym or the preferred name of a term). This is used mainly
		 * for scoring / sorting.
		 */
		public static final String LENGTH = "length";
	}

	/**
	 * Constants for the inner structure of the {@link Fields#SUGGESTION_TEXT} field. This structure is motivated by
	 * ElasticSearch, other search server technologies would have to re-structure the resulting suggestion document maps
	 * to apply to their requirements.
	 * 
	 * @author faessler
	 * 
	 */
	public final static class SUGGESTION_TEXT {
		/**
		 * The actual term string(s) to be indexed for a suggestion
		 */
		public final static String INPUT = "input";
		/**
		 * The weight of a suggestion. We weight shorter suggestions higher so that users get at least what they type.
		 */
		public final static String WEIGHT = "weight";
	}

	public final static class Context {
		public final static String FACET_CONTEXT = "facetContext";
	}

	/**
	 * Returns a list of facet grouped suggestions for the user delivered string <code>termFragment</code>.
	 * 
	 * @param termFragment
	 *            User delivered string for which suggestions are searched.
	 * @param facets
	 *            List of <code>Facet</code>s for which suggestions shall be returned.
	 * @return A list of <code>FacetTermSuggestionStream</code> objects, each delivering the term suggestions
	 *         corresponding to <code>termFragment</code> in a particular facet in <code>facets</code>.
	 * @throws IOException
	 */
	public List<FacetTermSuggestionStream> getSuggestionsForFragment(String termFragment, List<Facet> facets);

	public void createSuggestionIndex();
}