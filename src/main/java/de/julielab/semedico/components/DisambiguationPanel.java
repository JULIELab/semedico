package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTerm;

public class DisambiguationPanel {


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
	private FacetTerm selectedTerm;
	
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
		
	public void initialize() {
		sortedTermsPersistent = sortedTerms;
	}
	
	public List<FacetTerm> getCurrentTermSet() {
		return new ArrayList<FacetTerm>( sortedTerms.get(this.currentKey));

	}
	
	public void onDisambiguateTerm(String keyIndex) {
		
		
	    ArrayList<FacetTerm> termSet = new ArrayList<FacetTerm>(sortedTermsPersistent.get(Integer.valueOf(keyIndex.split("_")[0])));

	    System.out.println(termSet);
	    System.out.println(Integer.valueOf(keyIndex.split("_")[0]));
	    
	    for (Integer facetId : sortedTermsPersistent.keySet()) {
			System.out.println("FacetId: "  + facetId);
			for (FacetTerm term : sortedTermsPersistent.get(facetId))
				System.out.println(term.getName());
		}



	    selectedTerm = termSet.get(Integer.valueOf(keyIndex.split("_")[1]));

		
	}
	
	public String getCurrentKeyIndex() {
		return  currentKey + "_" + facetItemIndex;
	}
}
