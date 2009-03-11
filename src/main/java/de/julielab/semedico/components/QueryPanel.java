package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.beaneditor.Validate;

import com.google.common.collect.Multimap;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.SortCriterium;
import de.julielab.stemnet.core.Term;

public class QueryPanel {

	@Property
	@Parameter
	private Multimap<String, Term> queryTerms;

	@Parameter
	private SortCriterium sortCriterium;

	@Property
	@Parameter
	private Map<Facet, FacetConfiguration> facetConfigurations;

	@Property
	@Parameter	
	private Multimap<String, String> spellingCorrections;
	
	@Property
	@Parameter
	private boolean reviewsFiltered;
	
	@Property
	private String queryTerm;
	
	@Property
	private int queryTermIndex;
	
	@Property
	@Persist
	private String termToDisambiguate;
	
	@Property
	@Persist
	private Term selectedTerm;
	
	@Property
	private Term pathItem;
	
	@Property
	private int pathItemIndex;
	
	public boolean isTermCorrected() {
		if( queryTerm == null || spellingCorrections == null)
			return false;
		
		return spellingCorrections.containsKey(queryTerm);
	}	
	
	public Object getCorrectedTerm() {
		if( queryTerm == null || spellingCorrections == null)
			return null;
		
		Object[] correctedTerms = spellingCorrections.get(queryTerm).toArray();
		return correctedTerms[0].toString();
	}
	
	public boolean isTermAmbigue(){
		if( queryTerm == null )
			return false;
		
		Collection<Term> terms = queryTerms.get(queryTerm);
		if( terms.size() > 1 )
			return true;
		else
			return false;
	}
	
	public boolean termIsSelectedForDisambiguation(){
		return queryTerm == termToDisambiguate;
	}
	
	public void onActionFromRefineLink(String queryTerm){
		termToDisambiguate = queryTerm;		
	}
	
	public void doQueryChanged(String queryTerm) throws Exception {
		if( queryTerm == null )
			return;
		
		queryTerms.removeAll(queryTerm);	
	}
	
	public Term getMappedTerm(){
		Collection<Term> mappedTerms = queryTerms.get(queryTerm);
		if( mappedTerms.size() > 0 )
			return mappedTerms.iterator().next();
		else
			return null;
	}
	
	public String getMappedTermClass(){
		Term mappedTerm = getMappedTerm();
		if( mappedTerm != null )
			return mappedTerm.getFacet().getCssId()+"ColorA filterBox";
		else
			return null;
	}
	
	private Map<String, Term> getUnambigousQueryTerms(){
		Map<String, Term> unambigousTerms = new HashMap<String, Term>();
		
		for( String queryTerm: queryTerms.keySet() ){
			Collection<Term> terms = queryTerms.get(queryTerm);
			if( terms.size() == 1)
				unambigousTerms.put(queryTerm, terms.iterator().next());
		}
		
		return unambigousTerms;
	}
	
	public void onDrillUp(String queryTerm, int pathItemIndex) throws Exception {

		if( queryTerm == null )
			return;

		Term searchTerm = queryTerms.get(queryTerm).iterator().next();

		if( searchTerm == null )
			return;

		if( pathItemIndex < 0 || pathItemIndex > searchTerm.getAllParents().size()-1 )
			return;

		Term parent = searchTerm.getAllParents().get(pathItemIndex);

		FacetConfiguration configuration = facetConfigurations.get(searchTerm.getFacet());
		List<Term> path = configuration.getCurrentPath(); 
		if( configuration.isHierarchicMode() && path.size() > 0 &&
				searchTerm.isOnPath(path)	
		){
			path.clear();
			path.addAll(parent.getAllParents());
			path.add(parent);
		}

		Map<String, Term> unambigousTerms = getUnambigousQueryTerms(); 

		for (String unambigousQueryTerm : unambigousTerms.keySet() ){
			Term term = unambigousTerms.get(unambigousQueryTerm);

			if (term.isParentTerm(parent) && term != searchTerm) {
				queryTerms.removeAll(unambigousQueryTerm);
				return;
			}
		}	

		Collection<Term> parentCollection = new ArrayList<Term>();
		parentCollection.add(parent);
		queryTerms.replaceValues(queryTerm, parentCollection);
	}
	
	public boolean showPathForTerm() {
		Term mappedTerm = getMappedTerm();
		Facet facet = mappedTerm.getFacet();
		FacetConfiguration facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null)
			return facetConfiguration.isHierarchicMode();
		else
			return false;
	}
	
	public Collection<Term> getMappedTerms(){
		if( queryTerm == null )
			return Collections.EMPTY_LIST;
		
		List<Term> mappedQueryTerms = new ArrayList<Term>(queryTerms.get(queryTerm));
		
		return mappedQueryTerms;
	}	
	
	public Object[] getDrillUpContext(){
		return new Object[]{queryTerm, pathItemIndex};
	}
	
	public void onRemoveTerm(String queryTerm) throws Exception {
		if( queryTerm == null )
			return;

		queryTerms.removeAll(queryTerm);
	}
	
	public void onEnableReviewFilter(){
		reviewsFiltered = true;
	}
	
	public void onDisableReviewFilter(){
		reviewsFiltered = false;
	}

	@Validate("required")
	public SortCriterium getSortCriterium() {
		return sortCriterium;
	}

	public void setSortCriterium(SortCriterium sortCriterium) {
		this.sortCriterium = sortCriterium;
	}
	
	public void onActionFromSortSelection(){

	}

}
