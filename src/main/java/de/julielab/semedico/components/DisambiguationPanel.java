package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

public class DisambiguationPanel {


	@SessionState
	private SearchSessionState searchSessionState;

	
	@Property
	@Parameter
	private Collection<FacetTerm> mappedTerms;
	
	@Property
	@Parameter
	private Multimap<Integer, FacetTerm> sortedTerms;	

	@Persist
	private Multimap<Integer, FacetTerm> sortedTermsPersistent;	

	@Property
	@Parameter
	private String queryTerm;
	
	@Property
	@Parameter	
	private int ambigueTermIndex;	
	
	@Property
	@Parameter	
	private FacetTerm facetItem;
	
	@Property
	@Parameter	
	private int facetItemIndex;	
	
	@Property
	private int currentKey;
		
	@SetupRender
	public void initialize() {
		sortedTermsPersistent = sortedTerms;
	}
	
	public List<FacetTerm> getCurrentTermSet() {
		return new ArrayList<FacetTerm>( sortedTerms.get(this.currentKey));

	}
	
	public void onDisambiguateTerm(String keyIndex) {
	    ArrayList<FacetTerm> termSet = new ArrayList<FacetTerm>(sortedTermsPersistent.get(Integer.valueOf(keyIndex.split("_")[0])));
	    IFacetTerm selectedTerm = termSet.get(Integer.valueOf(keyIndex.split("_")[1]));
	    searchSessionState.getSearchState().setSelectedTerm(selectedTerm);
	}
	
	public String getCurrentKeyIndex() {
		return  currentKey + "_" + facetItemIndex;
	}
}
