package de.julielab.semedico.pages;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.Log;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.google.common.collect.Multimap;

import de.julielab.semedico.base.FacetDefinitions;
import de.julielab.semedico.base.Search;
import de.julielab.semedico.util.LazyDisplayGroup;
import de.julielab.stemnet.core.Author;
import de.julielab.stemnet.core.Document;
import de.julielab.stemnet.core.DocumentHit;
import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.FacettedSearchResult;
import de.julielab.stemnet.core.Term;
import de.julielab.stemnet.query.IQueryDisambiguationService;
import de.julielab.stemnet.search.IFacettedSearchService;
import de.julielab.stemnet.search.ISearchService.SortCriterium;

public class Hits extends Search{

	@Inject
	private IFacettedSearchService searchService;

	@Inject
	private IQueryDisambiguationService queryDisambiguationService;
	
	@Persist
	private Multimap<String, Term> queryTerms;
	
	@Persist
	private boolean reviewsFiltered;
	
	@Persist
	private String selectedSortCriterium;
	
	@Property
	@Persist
	private FacettedSearchResult searchResult;
	
	@ApplicationState
	private Map<Facet, FacetConfiguration> facetConfigurations;
	
	@Persist
	private LazyDisplayGroup<DocumentHit> displayGroup;
	
	@Persist
	private long elapsedTime;
	
	@Property
	@Persist
	private Term selectedTerm;
	
	private static int MAX_DOCS_PER_PAGE = 10;
	private static int MAX_BATCHES = 5;
	
	public final static String SORT_DATE = "date";
	public final static String SORT_RELEVANCE = "relevance";
	public final static String SORT_DATE_AND_RELEVANCE = "date and relevance";
	public final static String[] sortCriterias = { SORT_DATE, SORT_RELEVANCE, SORT_DATE_AND_RELEVANCE };
	
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
	@Property
	@Persist
	private Map<String, FacetHit> facetHitsById;
	@Property
	@Persist
	private Map<String, FacetConfiguration> facetConfigurationsById;

	public void initialize(){
		this.selectedSortCriterium = SORT_DATE;
	}
	
	@Log
	public void onTermSelect() throws IOException{
		if( selectedTerm != null ){
			queryTerms.put(selectedTerm.getLabel(), selectedTerm);
			doSearch(queryTerms);
		}
	}
	
	public void doNewSearch(String query, String termId) throws IOException {
		queryTerms = queryDisambiguationService.disambiguateQuery(query, termId);
		doSearch(queryTerms);
	}

	public void doSearch(Multimap<String, Term> queryTerms) throws IOException{
		long time = System.currentTimeMillis();
		SortCriterium sortCriterium = mapSortCriterium(selectedSortCriterium);
		searchResult = searchService.search(facetConfigurations.values(), 
													queryTerms, 
													sortCriterium, 
													reviewsFiltered);
		
		displayGroup = new LazyDisplayGroup<DocumentHit>(searchResult.getTotalHits(), 
														 MAX_DOCS_PER_PAGE, 
														 MAX_BATCHES, 
														 searchResult.getDocumentHits());
		facetHitsById = new HashMap<String, FacetHit>();
		for( FacetHit facetHit: searchResult.getFacetHits() )
			facetHitsById.put(facetHit.getFacet().getCssId(), facetHit);
		
		facetConfigurationsById = new HashMap<String, FacetConfiguration>();
		for( Facet facet: facetConfigurations.keySet() )
			facetConfigurationsById.put(facet.getCssId(), facetConfigurations.get(facet));
		
		elapsedTime = System.currentTimeMillis() - time;
		
	}
	
	public void onActionFromPagerLink(int page) throws IOException{
		displayGroup.setCurrentBatchIndex(page);
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService.createDocumentHitsForPositions(queryTerms, 
																							searchResult.getScoreDocs(), 
																							startPosition );
		displayGroup.setDisplayedObjects(documentHits);	
	}
	
	public void onActionFromPreviousBatchLink() throws IOException{
		displayGroup.displayPreviousBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService.createDocumentHitsForPositions(queryTerms, 
																							searchResult.getScoreDocs(), 
																							startPosition );
		displayGroup.setDisplayedObjects(documentHits);	
	}
	
	public void onActionFromNextBatchLink() throws IOException{
		displayGroup.displayNextBatch();
		int startPosition = displayGroup.getIndexOfFirstDisplayedObject();
		Collection<DocumentHit> documentHits = searchService.createDocumentHitsForPositions(queryTerms, 
																							searchResult.getScoreDocs(), 
																							startPosition );
		displayGroup.setDisplayedObjects(documentHits);	
	}

	
	public boolean getIsCurrentPage(){
		return pagerItem == displayGroup.getCurrentBatchIndex();
	}
	
	private SortCriterium mapSortCriterium(String selectedSortCriterium) {
		if( selectedSortCriterium.equals(SORT_DATE) )
			return SortCriterium.DATE;
		else if( selectedSortCriterium.equals(SORT_RELEVANCE) )
			return SortCriterium.RELEVANCE;
		else
			return SortCriterium.DATE_AND_RELEVANCE;
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
	
	public FacetHit getProteinFacetHit(){
		return facetHitsById.get(FacetDefinitions.PROTEIN_FACET_CSSID);
	}

	public void setProteinFacetHit(FacetHit proteinFacetHit){
		facetHitsById.put(FacetDefinitions.PROTEIN_FACET_CSSID, proteinFacetHit);
	}
	
	public FacetConfiguration getProteinFacetConfiguration(){
		return facetConfigurationsById.get(FacetDefinitions.PROTEIN_FACET_CSSID);
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
