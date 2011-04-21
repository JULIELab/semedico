package de.julielab.semedico.pages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.OpenBitSet;
import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;

import com.google.common.collect.Multimap;

import de.julielab.semedico.base.Search;
import de.julielab.stemnet.core.Author;
import de.julielab.stemnet.core.Document;
import de.julielab.stemnet.core.ExternalLink;
import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.SearchConfiguration;
import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.services.IDocumentCacheService;
import de.julielab.stemnet.core.services.IDocumentService;
import de.julielab.stemnet.query.IQueryTranslationService;
import de.julielab.stemnet.search.IFacetHitCollectorService;
import de.julielab.stemnet.search.IKwicService;

public class Article extends Search{

	@Property
	private Author authorItem;
	
	@Property
	private int authorIndex;
	
	@Property
	private ExternalLink externalLinkItem;
	
	@Property
	private Document relatedArticleItem;

	@Property
	@ApplicationState
	private SearchConfiguration searchConfiguration;
	
	@Inject
	private IDocumentCacheService documentCacheService;

	@Inject
	private IDocumentService documentService;

	@Inject
	private IQueryTranslationService queryTranslationService;
	
	@Inject
	private IFacetHitCollectorService facetHitCollectorService;
	
	@Inject
	private IKwicService kwicService;
	
	@Property
	@Persist
	private Document article;

	@Property
	@Persist
	private String highlightedTitle;

	@Property
	@Persist
	private String highlightedAbstractText;
	
	@Persist
	@Property
	private int selectedFacetType;
	
	@Property
	@Persist
	private FacetTerm selectedTerm;
	
	@Property
	@Persist	
	private OpenBitSet documents;
	
	@Property
	@Persist	
	private List<FacetHit> currentFacetHits;
	
	@InjectPage
	private Hits hits;
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMMMM dd");
	
	@Persist
	private Collection<FacetConfiguration> biomedFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> immunologyFacetConfigurations;
	@Persist
	private Collection<FacetConfiguration> bibliographyFacetConfigurations;
	
	@Property
	@Persist
	private Multimap<String, String> spellingCorrections;
	@Property
	@Persist
	private Multimap<String, FacetTerm> spellingCorrectedQueryTerms;
	
	public void onActivate(int docId) throws IOException{

		article = documentCacheService.getCachedDocument(docId);
		if( article == null ){
			article = documentService.readDocumentWithLuceneId(docId);
			documentCacheService.addDocument(article);
		}
		
		documents = new OpenBitSet();
		documents.set(article.getLuceneId());
		createHighlightedArticle();
		this.selectedFacetType = Facet.BIO_MED;
		
		spellingCorrections = searchConfiguration.getSpellingCorrections();
		spellingCorrectedQueryTerms = searchConfiguration.getSpellingCorrectedQueryTerms();
		
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
		
		currentFacetHits = facetHitCollectorService.collectFacetHits(biomedFacetConfigurations, documents);
	}
	
	public Object onTermSelect() throws IOException{
		Multimap<String, FacetTerm> queryTerms = searchConfiguration.getQueryTerms();
		if( selectedTerm != null ){
			List<FacetTerm> parents = selectedTerm.getAllParents();
			for (FacetTerm parent : parents) {
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
			
			
			hits.doSearch(queryTerms, searchConfiguration.getSortCriterium(), searchConfiguration.isReviewsFiltered());
			return hits;
		}
		return this;
	}

	public Object onRemoveTerm() throws IOException{
		return hits.onRemoveTerm();
	}

	public Object onSuccessFromSearch() throws IOException{
		hits.doNewSearch(getQuery(), getTermId());
		return hits;
	}

	public Object onActionFromSearchInputField() throws IOException{
		hits.doNewSearch(getQuery(), getTermId());
		return hits;
	}
	
	public void createHighlightedArticle() throws IOException{
		String query = queryTranslationService.createKwicQueryFromTerms(searchConfiguration.getQueryTerms());
		
		String text = kwicService.createHighlightedAbstract(query, article);
		String title = kwicService.createHighlightedTitle(query, article);

		if( text != null && !text.equals("") )
			highlightedAbstractText = text;
		else
			highlightedAbstractText = article.getAbstractText();
		
		if( title != null && !title.equals("") )
			highlightedTitle = title;
		else
			highlightedTitle = article.getTitle();
	}
	
	public boolean isNotLastAuthor() {
		return authorIndex < article.getAuthors().size() - 1;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}


	public String getPubmedURL(){
		return "http://www.ncbi.nlm.nih.gov/pubmed/" + article.getPubMedId();
	}
	
	public boolean hasRelatedArticles(){
		return article.getRelatedArticles().size() > 0;
	}
	
	public boolean hasFulltextLinks(){
		return article.getExternalLinks().size() > 0;
	}
}
