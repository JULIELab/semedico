package de.julielab.semedico.pages;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.google.common.collect.Multimap;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.util.LazyDisplayGroup;
import de.julielab.stemnet.core.Author;
import de.julielab.stemnet.core.Document;
import de.julielab.stemnet.core.DocumentHit;
import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.FacettedSearchResult;
import de.julielab.stemnet.core.SearchConfiguration;
import de.julielab.stemnet.core.SortCriterium;
import de.julielab.stemnet.core.Term;
import de.julielab.stemnet.query.IQueryDisambiguationService;
import de.julielab.stemnet.search.IFacettedSearchService;

public class Hits extends Search{

	@Inject
	private IFacettedSearchService searchService;

	@Inject
	private IQueryDisambiguationService queryDisambiguationService;
	
	@Property
	@Persist
	private FacettedSearchResult searchResult;
	
	@Property
	@Persist	
	private List<FacetHit> currentFacetHits;
	
	@Property
	@ApplicationState
	private SearchConfiguration searchConfiguration;
	
	@Persist
	private LazyDisplayGroup<DocumentHit> displayGroup;
	
	@Persist
	private long elapsedTime;
	
	@Property
	@Persist
	private Term selectedTerm;
	
	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;
	
	
	@Property
	private DocumentHit hitItem;

	@Property
	private int hitIndex;

	@Property
	private int authorIndex;

	@Property
	private Author authorItem;

	@Property
	private String kwicItem;

	@Property
	private int pagerItem;

	@Persist
	@Property
	private int selectedFacetType;
	
	@Persist
	private Collection<FacetConfiguration> biomedFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> immunologyFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> bibliographyFacetConfigurations;
	
	public void initialize(){
		this.selectedFacetType = Facet.BIO_MED;

		biomedFacetConfigurations = new ArrayList<FacetConfiguration>();
		immunologyFacetConfigurations = new ArrayList<FacetConfiguration>();
		bibliographyFacetConfigurations = new ArrayList<FacetConfiguration>();
		Map<Facet, FacetConfiguration> facetConfigurations = searchConfiguration.getFacetConfigurations();
		
		for( FacetConfiguration facetConfiguration: facetConfigurations.values()){
			Facet facet = facetConfiguration.getFacet();
			if( facet.getType() == Facet.BIO_MED ){
				biomedFacetConfigurations.add(facetConfiguration);
			}
			else if( facet.getType() == Facet.IMMUNOLOGY ){
				immunologyFacetConfigurations.add(facetConfiguration);
			}
			else if( facet.getType() == Facet.BIBLIOGRAPHY ){
				bibliographyFacetConfigurations.add(facetConfiguration);
			}
		}
	}
	
	public void onTermSelect() throws IOException{
		Multimap<String, Term> queryTerms = searchConfiguration.getQueryTerms();
		if( selectedTerm != null ){
			List<Term> parents = selectedTerm.getAllParents();
			for (Term parent : parents) {
				if( queryTerms.containsValue(parent) ){
					Collection<String> queryTermKeys = new ArrayList<String>(queryTerms.keys());
					for( String queryTerm: queryTermKeys )
						if( queryTerms.get(queryTerm).contains(parent) ){
							queryTerms.remove(queryTerm, parent);
							queryTerms.put(queryTerm, selectedTerm);
						}
					break;
				}
			}
		
			if (!queryTerms.containsValue(selectedTerm))
				queryTerms.put(selectedTerm.getLabel(), selectedTerm);
			
			doSearch(queryTerms, searchConfiguration.getSortCriterium(), searchConfiguration.isReviewsFiltered());
		}
	}
	
	public void onDrillUp() throws IOException{
		doSearch(searchConfiguration.getQueryTerms(),
				 searchConfiguration.getSortCriterium(), 
				 searchConfiguration.isReviewsFiltered());
	}
	
	public void onActionFromQueryPanel() throws IOException{
		doSearch(searchConfiguration.getQueryTerms(),
				 searchConfiguration.getSortCriterium(), 
				 searchConfiguration.isReviewsFiltered());
	}
	
	
	public void onDisableReviewFilter() throws IOException{
		doSearch(searchConfiguration.getQueryTerms(),
				 searchConfiguration.getSortCriterium(), 
				 searchConfiguration.isReviewsFiltered());
	}

	public void onEnableReviewFilter() throws IOException{
		doSearch(searchConfiguration.getQueryTerms(),
				 searchConfiguration.getSortCriterium(), 
				 searchConfiguration.isReviewsFiltered());
	}

	public void drillDownFacetConfigurations(Collection<Term> terms, Map<Facet, FacetConfiguration> facetConfigurations){
			
		for ( Term searchTerm: terms ){
			if( searchTerm.getSubTerms().size() == 0 )
				continue;
			
			FacetConfiguration configuration = facetConfigurations.get(searchTerm.getFacet());
			if( configuration == null )
				continue;
			
			if( configuration.isHierarchicMode() && configuration.getCurrentPath().size() == 0 ){
				configuration.getCurrentPath().addAll(searchTerm.getAllParents());
				if( searchTerm.getParent() == null )
					configuration.getCurrentPath().add(searchTerm);
			}
		}		
	}
	
	public Object doNewSearch(String query, String termId) throws IOException {
		Multimap<String, Term> queryTerms = queryDisambiguationService.disambiguateQuery(query, termId);
		searchConfiguration.setQueryTerms(queryTerms);
		
		if( queryTerms.size() == 0 )
			return Index.class;

		Map<Facet, FacetConfiguration> facetConfigurations = searchConfiguration.getFacetConfigurations();
		resetConfigurations(facetConfigurations.values());
		drillDownFacetConfigurations(queryTerms.values(), facetConfigurations);
		doSearch(queryTerms,
				 searchConfiguration.getSortCriterium(), 
				 searchConfiguration.isReviewsFiltered());

		return this;
	}

	public void resetConfigurations(Collection<FacetConfiguration> configurations) {
		for( FacetConfiguration configuration: configurations )
			configuration.reset();
	}

	public void doSearch(Multimap<String, Term> queryTerms, SortCriterium sortCriterium, boolean reviewsFiltered) throws IOException{
		long time = System.currentTimeMillis();
		Collection<FacetConfiguration> facetConfigurations = getConfigurationsForFacetType(selectedFacetType);
		searchResult = searchService.search(facetConfigurations, 
													queryTerms, 
													sortCriterium, 
													reviewsFiltered);
		
		displayGroup = new LazyDisplayGroup<DocumentHit>(searchResult.getTotalHits(), 
														 MAX_DOCS_PER_PAGE, 
														 MAX_BATCHES, 
														 searchResult.getDocumentHits());

		currentFacetHits = searchResult.getFacetHits();
		elapsedTime = System.currentTimeMillis() - time;
		
	}
	
	public void onRemoveTerm() throws IOException{
		doSearch(searchConfiguration.getQueryTerms(),
				 searchConfiguration.getSortCriterium(), 
				 searchConfiguration.isReviewsFiltered());	}
	
	private Collection<FacetConfiguration> getConfigurationsForFacetType(int facetType){
		if( facetType == Facet.BIO_MED )
			return biomedFacetConfigurations;
		else if( facetType == Facet.IMMUNOLOGY )
			return immunologyFacetConfigurations;
		else
			return bibliographyFacetConfigurations;
	}
	
	public void onActionFromPagerLink(int page) throws IOException{
		displayGroup.setCurrentBatchIndex(page);
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService.createDocumentHitsForPositions(searchConfiguration.getQueryTerms(), 
																							searchResult.getScoreDocs(), 
																							startPosition );
		displayGroup.setDisplayedObjects(documentHits);	
	}
	
	public void onActionFromPreviousBatchLink() throws IOException{
		displayGroup.displayPreviousBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService.createDocumentHitsForPositions(searchConfiguration.getQueryTerms(), 
																							searchResult.getScoreDocs(), 
																							startPosition );
		displayGroup.setDisplayedObjects(documentHits);	
	}
	
	public void onActionFromNextBatchLink() throws IOException{
		displayGroup.displayNextBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService.createDocumentHitsForPositions(searchConfiguration.getQueryTerms(), 
																							searchResult.getScoreDocs(), 
																							startPosition );
		displayGroup.setDisplayedObjects(documentHits);	
	}

	
	public boolean getIsCurrentPage(){
		return pagerItem == displayGroup.getCurrentBatchIndex();
	}
	
	public String getCurrentHitClass(){
		return hitIndex % 2 == 0 ? "evenHit": "oddHit";
	}
	
	public String getCurrentArticleTypeClass(){
		if( hitItem.getDocument().getType() == Document.TYPE_ABSTRACT )
			return "hitIconAbstract";
		else if( hitItem.getDocument().getType() == Document.TYPE_TITLE )
			return "hitIconTitle";
		else if( hitItem.getDocument().getType() == Document.TYPE_FULL_TEXT )
			return "hitIconFull";
		else
			return null;
	}

	public int getIndexOfFirstArticle(){
		return displayGroup.getIndexOfFirstDisplayedObject() + 1;
	}
	
	public boolean isNotLastAuthor() {
		return authorIndex < hitItem.getDocument().getAuthors().size() - 1;
	}
	
	public LazyDisplayGroup<DocumentHit> getDisplayGroup() {
		return displayGroup;
	}

	public void setDisplayGroup(LazyDisplayGroup<DocumentHit> displayGroup) {
		this.displayGroup = displayGroup;
	}

	public FacettedSearchResult getFacettedSearchResult() {
		return searchResult;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}
}
