package de.julielab.semedico.core;

import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

import java.util.ArrayList;
import java.util.Collection;
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
	private List<String> preferredNames;
	private List<String> termIds;
	private List<List<String>> termSynonyms;
	private List<Collection<String>> termQualifiers;
	private List<String> facetNames;
	private List<String> shortFacetNames;
	private List<QueryToken.Category> lexerCategories;
	private List<TokenType> inputTokenTypes;

	private int index;

	private int begin;






	public FacetTermSuggestionStream(Facet facet) {
		super();
		this.facet = facet;
		termIds = new ArrayList<>(INIT_SIZE);
		termNames = new ArrayList<>(INIT_SIZE);
		preferredNames = new ArrayList<>(INIT_SIZE);
		termSynonyms = new ArrayList<>(INIT_SIZE);
		termQualifiers = new ArrayList<>(INIT_SIZE);
		facetNames = new ArrayList<>(INIT_SIZE);
		shortFacetNames = new ArrayList<>(INIT_SIZE);
		inputTokenTypes = new ArrayList<>(INIT_SIZE);
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
	 *  @param termId
	 * @param termName
	 * @param preferredName
	 * @param termSynonyms
	 * @param qualifiers
	 * @param facetName
	 * @param shortFacetName
	 * @param lexerCategory
	 * @param inputTokenType
	 */
	public void addTermSuggestion(String termId, String termName,
								  String preferredName, List<String> termSynonyms, Collection<String> qualifiers, String facetName, String shortFacetName, QueryToken.Category lexerCategory, TokenType inputTokenType) {
		this.termIds.add(termId);
		this.termNames.add(termName);
		this.preferredNames.add(preferredName);
		this.termSynonyms.add(termSynonyms);
		this.facetNames.add(facetName);
		this.shortFacetNames.add(shortFacetName);
		this.termQualifiers.add(qualifiers);
		this.lexerCategories.add(lexerCategory);
		this.inputTokenTypes.add(inputTokenType);
	}

	public String getTermId() {
		return termIds.get(index);
	}

	public String getTermName() {
		return termNames.get(index);
	}
	
	public String getPreferredName() {
		return preferredNames.get(index);
	}

	public List<String> getTermSynonyms() {
		return termSynonyms.get(index);
	}

	public TokenType getInputTokenType() {
		return inputTokenTypes.get(index);
	}
	
	/**
	 * Returns the facet these suggestions are grouped to, if set.
	 * @return
	 */
	public Facet getFacet() {
		return facet;
	}
	
	/**
	 * Returns the name of the facet the current concept suggestion belongs to. Note that this is NOT the same as {@link #getFacet().getName()}
	 * @return
	 */
	public String getFacetName() {
		return facetNames.get(index);
	}
	
	public String getShortFacetName() {
		return shortFacetNames.get(index);
	}
	
	public Collection<String> getTermQualifiers(){
		return termQualifiers.get(index);
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
		return "{  hits:" + termIds.size() + " }";
	}

	public void setBegin(int begin) {
		this.begin = begin;
		
	}

	/**
	 * 
	 * @return The begin offset in the original user input string wo which this suggestion stream applies.
	 */
	public int getBegin() {
		return begin;
	}

}
