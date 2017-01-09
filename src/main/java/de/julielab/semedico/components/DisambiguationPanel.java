package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.pages.ResultList;
import de.julielab.semedico.state.SemedicoSessionState;

public class DisambiguationPanel {

	@InjectPage
	private ResultList resultList;
	
	@Inject
	Logger logger;
	
	@SessionState
	private SemedicoSessionState sessionState;

	@Property
	@Parameter
	private Collection<IFacetTerm> mappedTerms;
	
	@Property
	@Parameter
	private Multimap<String, IFacetTerm> sortedTerms;	

	@Persist
	private Multimap<String, IFacetTerm> sortedTermsPersistent;	

	@Property
	@Parameter
	private String queryTerm;
	
	@Property
	@Parameter	
	private int ambigueTermIndex;	
	
	@Property
	@Parameter	
	private IFacetTerm facetItem;
	
	@Property
	@Parameter	
	private int facetItemIndex;	
	
	@Property
	private String currentKey;
		
	@SetupRender
	public void initialize() {
		sortedTermsPersistent = sortedTerms;
	}
	
	public List<IFacetTerm> getCurrentTermSet() {
		return new ArrayList<>( sortedTerms.get(this.currentKey));

	}
	
	public void onDisambiguateTerm(String keyIndex) throws Exception {
	    ArrayList<IFacetTerm> termList = new ArrayList<>(sortedTermsPersistent.get(keyIndex.split("_")[0]));
	    IFacetTerm selectedTerm = termList.get(Integer.valueOf(keyIndex.split("_")[1]));
	    
	    ParseTree queryTerms = sessionState.getDocumentRetrievalSearchState().getSemedicoQuery();
		logger.debug("Selected term from disambiguation panel: " + selectedTerm);
		queryTerms.selectTerm(selectedTerm);
	}
	
	public String getCurrentKeyIndex() {
		return  currentKey + "_" + facetItemIndex;
	}
}
