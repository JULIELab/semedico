package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.List;

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
		index = 0;
	}
	
	public boolean incrementTermSuggestion() {
		++index;
		return index < termIds.size();
	}
	
	/**
	 * @param termId
	 * @param termName
	 * @param termSynonyms2
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
	 * Calling <code>reset</code> sets the internal suggestion counter back to 0.
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
		return "{ facet: " + facet.getName() + " hits:" + termIds.size()
				+ " }";
	}
}
