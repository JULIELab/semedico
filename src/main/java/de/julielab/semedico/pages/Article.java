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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SearchConfiguration;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.IDocumentCacheService;
import de.julielab.semedico.core.services.IDocumentService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.search.IFacetHitCollectorService;
import de.julielab.semedico.search.IKwicService;

public class Article extends Search{

	@Property
	private Author authorItem;
	
	@Property
	private int authorIndex;
	
	@Property
	private ExternalLink externalLinkItem;
	
	@Property
	private SemedicoDocument relatedArticleItem;

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
	private SemedicoDocument article;

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
	private FacetHit currentFacetHit;
	
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

	@Inject
	private ITermService termService;
	
	public void onActivate(int pmid) throws IOException{

		article = documentCacheService.getCachedDocument(pmid);
		if( article == null ){
			article = documentService.readDocumentWithPubmedId(pmid);
			documentCacheService.addDocument(article);
		}
		
//		documents = new OpenBitSet();
//		documents.set(article.getLuceneId());
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
		
		currentFacetHit = facetHitCollectorService.collectFacetHits(biomedFacetConfigurations);
	}
	
	public Object onTermSelect() throws IOException {
		setQuery(null);
		Multimap<String, FacetTerm> queryTerms = searchConfiguration
				.getQueryTerms();
		if (selectedTerm == null) {
			throw new IllegalStateException(
					"The FacetTerm object reflecting the newly selected term is null.");
		}
		// Get the FacetConfiguration associated with the selected term.
		FacetConfiguration selectedFacetConf = searchConfiguration
				.getFacetConfigurations().get(selectedTerm.getFirstFacet());
		// Are there already any terms chosen in this facet? If not, just add
		// the new one.
		if (!selectedFacetConf.containsSelectedTerms()) {
			queryTerms.put(selectedTerm.getName(), selectedTerm);
		} else {
			// Otherwise, we have to take caution when refining a term. Only the
			// deepest term of each root-node-path in the hierarchy may be
			// included in our queryTerms map.
			// Reason 1: The root-node-path of _each_ term in queryTerms is
			// computed automatically in the QueryPanel
			// currently.
			// Reason 2: We associate refined terms with the (user) query string
			// of the original term. Multiple terms per string -> disambiguation
			// triggers.
			Multimap<String, FacetTerm> newQueryTerms = HashMultimap.create();
			List<FacetTerm> rootPath = termService
					.getPathFromRoot(selectedTerm);
			String refinedQueryStr = null;
			// Build a new queryTerms map with all not-refined terms.
			// The copying is done because in rare cases writing on the
			// queryTokens map while iterating over it can lead to a
			// ConcurrentModificationException.
			for (Map.Entry<String, FacetTerm> entry : queryTerms.entries()) {
				String queryToken = entry.getKey();
				FacetTerm term = entry.getValue();
				if (!rootPath.contains(term))
					newQueryTerms.put(queryToken, term);
				else
					// If there IS a term in queryTerms which lies on the root
					// path, just memorize its key.
					refinedQueryStr = queryToken;
			}
			// If there was an ancestor of the selected term in queryTerms, now
			// associate the new term with its ancestor's query string.
			if (refinedQueryStr != null)
				newQueryTerms.put(refinedQueryStr, selectedTerm);
			else
				// Otherwise, add a new mapping.
				queryTerms.put(selectedTerm.getName(), selectedTerm);
			queryTerms = newQueryTerms;
		}

		// List<FacetTerm> parents = selectedTerm.getAllParents();
		// for (FacetTerm parent : parents) {
		// if (queryTerms.containsValue(parent)) {
		// Collection<String> queryTermKeys = new ArrayList<String>(
		// queryTerms.keys());
		// for (String queryTerm : queryTermKeys)
		// if (queryTerms.get(queryTerm).contains(parent)) {
		// removedParentTerm[0] = queryTerm;
		// removedParentTerm[1] = parent;
		// queryTerms.remove(queryTerm, parent);
		// queryTerms.put(queryTerm, selectedTerm);
		// }
		// break;
		// }
		// }
		//
		// if (!queryTerms.containsValue(selectedTerm))
		// queryTerms.put(selectedTerm.getName(), selectedTerm);

		// searchByTermSelect = true;
		hits.doSearch(queryTerms, searchConfiguration.getSortCriterium(),
				searchConfiguration.isReviewsFiltered());
		return hits;
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
		return "http://www.ncbi.nlm.nih.gov/pubmed/" + article.getPubmedId();
	}
	
	public boolean hasRelatedArticles(){
		return article.getRelatedArticles().size() > 0;
	}
	
	public boolean hasFulltextLinks(){
		return article.getExternalLinks().size() > 0;
	}
}
