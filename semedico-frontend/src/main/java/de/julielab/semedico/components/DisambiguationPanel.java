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

import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
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
	private Collection<IHierarchicalConcept> mappedTerms;
	
	@Property
	@Parameter
	private Multimap<String, IHierarchicalConcept> sortedTerms;	

	@Persist
	private Multimap<String, IHierarchicalConcept> sortedTermsPersistent;	

	@Property
	@Parameter
	private String queryTerm;
	
	@Property
	@Parameter	
	private int ambigueTermIndex;	
	
	@Property
	@Parameter	
	private IHierarchicalConcept facetItem;
	
	@Property
	@Parameter	
	private int facetItemIndex;	
	
	@Property
	private String currentKey;
		
	@SetupRender
	public void initialize() {
		sortedTermsPersistent = sortedTerms;
	}
	
	public List<IHierarchicalConcept> getCurrentTermSet() {
		return new ArrayList<>( sortedTerms.get(this.currentKey));

	}
	
	public void onDisambiguateTerm(String keyIndex) throws Exception {
	    ArrayList<IHierarchicalConcept> termList = new ArrayList<>(sortedTermsPersistent.get(keyIndex.split("_")[0]));
	    IHierarchicalConcept selectedTerm = termList.get(Integer.valueOf(keyIndex.split("_")[1]));
	    
	    ParseTree queryTerms = sessionState.getDocumentRetrievalSearchState().getSemedicoQuery();
		logger.debug("Selected term from disambiguation panel: " + selectedTerm);
		queryTerms.selectTerm(selectedTerm);
	}
	
	public String getCurrentKeyIndex() {
		return  currentKey + "_" + facetItemIndex;
	}
}
