package de.julielab.semedico.pages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.FacetedSearchResult;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.services.interfaces.IDocumentService;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;
import de.julielab.semedico.core.services.interfaces.IRelatedArticlesService;
import de.julielab.semedico.search.interfaces.IFacetedSearchService;

@Import(library = { "article.js" })
public class Article {

	@SessionState(create = false)
	private SearchState searchState;

	// Only here to be passed to the FacetedSearchLayout component which then
	// passes it to the Tabs component in order to render the correct tabs and
	// facet boxes.
	@SuppressWarnings("unused")
	@SessionState
	@Property
	private UserInterfaceState uiState;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@InjectPage
	private ResultList resultList;

	@Inject
	private ComponentResources resources;

	@Persist(PersistenceConstants.FLASH)
	private int pubMedId;

	@SuppressWarnings("unused")
	@Property
	private String ppiItem;
	
	@SuppressWarnings("unused")
	@Property
	private Author authorItem;

	@Property
	private int authorIndex;

	@SuppressWarnings("unused")
	@Property
	private ExternalLink externalLinkItem;

	@SuppressWarnings("unused")
	@Property
	private SemedicoDocument relatedArticleItem;

	@Inject
	private IFacetedSearchService searchService;

	@Inject
	private IDocumentService documentService;

	@Inject
	private IRelatedArticlesService relatedArticlesService;

	@Inject
	private IExternalLinkService externalLinkService;

	@Property
	@Persist
	private HighlightedSemedicoDocument article;

	@InjectComponent("fulltextLinksZone")
	private Zone fulltextLinksZone;

	@InjectComponent("relatedLinksZone")
	private Zone relatedLinksZone;

	@Inject
	@Path("context:images/loader.gif")
	private Asset loaderImage;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy MMMMM dd");

	@Inject
	Logger logger;

	@Property
	private Collection<SemedicoDocument> relatedArticles;

	@Property
	private Collection<ExternalLink> externalLinks;

	public void onActivate(Integer pmid) {
		this.pubMedId = pmid;
	}

	public void setupRender() throws IOException {
		String userQueryString = null;
		if (searchState != null)
			userQueryString = searchState.getSolrQueryString();
		else {
			String pmidString = String.valueOf(pubMedId);
			FacetedSearchResult searchResult = searchService.search(pmidString,
					null, IFacetedSearchService.DO_FACET);
			resultList.setSearchResult(searchResult);
		}
		article = documentService.getHighlightedSemedicoDocument(pubMedId,
				userQueryString);
	}

	public Object onGetFulltextLinks(int pmid) throws IOException {
		externalLinks = externalLinkService.fetchExternalLinks(pmid);
		return fulltextLinksZone;
	}

	public Object onGetRelatedArticles(int pmid) throws IOException {
		relatedArticles = relatedArticlesService.fetchRelatedArticles(pmid);
		return relatedLinksZone;
	}

	@AfterRender
	public void afterRender() {
		Link loadFulltextLinksEventLink = resources.createEventLink(
				"getFulltextLinks", pubMedId);
		Link loadRelatedArticlesEventLink = resources.createEventLink(
				"getRelatedArticles", pubMedId);

		javaScriptSupport.addScript("getFulltextLinks('%s', '%s')",
				loadFulltextLinksEventLink, loaderImage.toClientURL());
		javaScriptSupport.addScript("getRelatedArticles('%s', '%s')",
				loadRelatedArticlesEventLink, loaderImage.toClientURL());

		logger.info("Viewed document: \"" + pubMedId + "\"");
	}

	public void onDisplayRelatedArticle(int pmid) {
		pubMedId = pmid;
	}

	public boolean isNotLastAuthor() {
		return authorIndex < article.getAuthors().size() - 1;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public String getPubmedURL() {
		return "http://www.ncbi.nlm.nih.gov/pubmed/" + article.getPubmedId();
	}

	public boolean hasRelatedArticles() {
		return relatedArticles != null && relatedArticles.size() > 0;
	}

	public boolean hasFulltextLinks() {
		return externalLinks != null && externalLinks.size() > 0;
	}
}
