package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.pages.ResultList;

public class DisambiguationPanel {

	@InjectPage
	private ResultList resultList;
	
	@Inject
	Logger logger;
	
	@SessionState
	private SearchState searchState;

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
	    ArrayList<FacetTerm> termList = new ArrayList<FacetTerm>(sortedTermsPersistent.get(Integer.valueOf(keyIndex.split("_")[0])));
	    IFacetTerm selectedTerm = termList.get(Integer.valueOf(keyIndex.split("_")[1]));
//	    searchState.setDisambiguatedTerm(selectedTerm);
	    
	    Multimap<String, IFacetTerm> queryTerms = searchState.getQueryTerms();
		logger.debug("Selected term from disambiguation panel: " + selectedTerm);
		String currentEntryKey = null;
		for (Map.Entry<String, IFacetTerm> queryTermEntry : queryTerms
				.entries()) {
			if (queryTermEntry.getValue().equals(selectedTerm)) {
				currentEntryKey = queryTermEntry.getKey();
			}
			logger.debug("Term in queryTerms: "
					+ queryTermEntry.getValue().getName());
		}
		queryTerms.removeAll(currentEntryKey);
		queryTerms.put(currentEntryKey, selectedTerm);
	}
	
	public String getCurrentKeyIndex() {
		return  currentKey + "_" + facetItemIndex;
	}
}
