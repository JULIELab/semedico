package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.List;

/**
 * This object represents a set of suggestions which have been retrieved from
 * the suggestion index by searching for a use typed string.
 * <p>
 * The design of this class is inspired by the newer Lucene TokenStream API
 * which aims to avoid massive object creation.<br>
 * Thus, all suggestions stored in an instance of this class are saved in a set
 * of parallel lists containing all information necessary. I.e. the
 * <code>i</code>th suggestion is comprised of the assembly of the values of all
 * lists at position <code>i</code>.
 * </p>
 * <p>
 * To retrieve the suggestions, {@link #incrementTermSuggestion()} must be
 * called. Each call will continue to the next suggestion. Then, the values of
 * interest may be obtained by calling the appropriate method (e.g.
 * {@link #getTermId()}).
 * </p>
 * 
 * @author faessler
 * 
 */
public final class FacetTermSuggestionStream {
	private final int INIT_SIZE = 100;

	private Facet facet;

	private List<String> termNames;
	private List<String> termIds;
	private List<String> termSynonyms;

	private int index;

	public FacetTermSuggestionStream(Facet facet) {
		super();
		this.facet = facet;
		termIds = new ArrayList<String>(INIT_SIZE);
		termNames = new ArrayList<String>(INIT_SIZE);
		termSynonyms = new ArrayList<String>(INIT_SIZE);
		index = -1;
	}

	/**
	 * Proceeds the stream to the next suggestion. This method must be called
	 * before retrieving any values concerning suggestions.
	 * 
	 * @return True if there is at least one more suggestions, false otherwise.
	 */
	public boolean incrementTermSuggestion() {
		++index;
		return index < termIds.size();
	}

	/**
	 * Adds a suggestion comprised of a term ID, a particular term name (out of
	 * all possible values) and the rest of possible names/writing variants in
	 * <code>termSynonyms</code>.
	 * 
	 * @param termId
	 * @param termName
	 * @param termSynonyms
	 */
	public void addTermSuggestion(String termId, String termName,
			String termSynonyms) {
		this.termIds.add(termId);
		this.termNames.add(termName);
		this.termSynonyms.add(termSynonyms);

	}

	public String getTermId() {
		return termIds.get(index);
	}

	public String getTermName() {
		return termNames.get(index);
	}

	public String getTermSynonyms() {
		return termSynonyms.get(index);
	}

	public Facet getFacet() {
		return facet;
	}

	public int size() {
		return termIds.size();
	}

	/**
	 * Calling <code>reset</code> sets the internal suggestion counter back to
	 * 0.
	 * <p>
	 * Resetting allows to retrieve the suggestions several times. This method
	 * is currently unused (Semedico Core v. 1.3-SNAPSHOT).
	 * </p>
	 */
	public void reset() {
		index = 0;
	}

	@Override
	public String toString() {
		return "{ facet: " + facet.getName() + " hits:" + termIds.size() + " }";
	}
}
