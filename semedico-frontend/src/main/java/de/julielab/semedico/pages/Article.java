package de.julielab.semedico.pages;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import de.julielab.semedico.core.search.services.ISearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
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
import org.apache.tapestry5.services.PageRenderLinkSource;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.core.AbstractUserInterfaceState;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.Publication;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.components.data.Highlight;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IExternalLinkService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.IRelatedArticlesService;
import de.julielab.semedico.state.SemedicoSessionState;

@Import(library = { "article.js" }, stylesheet = "context:css/article.css")

public class Article {
	@SessionState(create = false)
	private SemedicoSessionState sessionState;

	// Only here to be passed to the FacetedSearchLayout component which then
	// passes it to the Tabs component in order to render the correct tabs and
	// facet boxes.
	@Property
	@Persist("tab")
	private AbstractUserInterfaceState uiState;

	@Inject
	private ComponentResources resources;

	@Property
	@Persist("tab")
	private String docId;

	@Property
	@Persist("tab")
	private String pmid;

	@Property
	@Persist("tab")
	private String pmcid;

	@Property
	@Persist("tab")
	private ParseTree highlightingQuery;

	@Property
	@Persist("tab")
	private HighlightedSemedicoDocument article;

	@Persist("tab")
	private String indexType;
	//
	// @ActivationRequestParameter("btermQuery")
	// private boolean bTermQuery;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@InjectPage
	private ResultList resultList;

	@Property
	private String ppiItem;

	@Property
	private Author authorItem;

	@Property
	private int authorIndex;

	@Property
	private ExternalLink externalLinkItem;

	@Property
	private SemedicoDocument relatedArticleItem;

	@Property
	private Highlight pmcHlItem;

	@Inject
	private ISearchService searchService;

	@Inject
	private IRelatedArticlesService relatedArticlesService;

	@Inject
	private IExternalLinkService externalLinkService;

	@InjectComponent("fulltextLinksZone")
	private Zone fulltextLinksZone;

	@InjectComponent("relatedLinksZone")
	private Zone relatedLinksZone;

	@Inject
	@Path("context:images/loader.gif")
	private Asset loaderImage;

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy MMMMM dd");
	@Inject
	private PageRenderLinkSource pageRenderLinkSource;
	@Inject
	Logger logger;

	@Property
	private Collection<SemedicoDocument> relatedArticles;

	@Property
	private Collection<ExternalLink> externalLinks;

	@InjectPage
	private Index index;

	@Inject
	private Request request;

	public Object onActivate() {
		if (null != sessionState) {
			sessionState.setActiveTabFromRequest(request);
		} else {
			return index;
		}
		return null;
	}

	public void setupRender() throws IOException {
		try {
			// read parameters from request, if given
			String pmidParameter = request.getParameter("docId");
			if (null != pmidParameter) {
				docId = pmidParameter;
			}
//			if (sessionState == null) {
//
//				LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
//						.doArticleSearch(docId, indexType, highlightingQuery).get();
//				resultList.setSearchResult(searchResult);
//			}
//
//			LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult) searchService
//					.doArticleSearch(docId, indexType, highlightingQuery).get();

//			article = searchResult.semedicoDoc;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Object onGetFulltextLinks(String pmid) throws IOException {
		externalLinks = externalLinkService.fetchExternalLinks(pmid);
		return fulltextLinksZone;
	}

	public Object onGetRelatedArticles(String pmid) throws IOException {
		// relatedArticles = relatedArticlesService.fetchRelatedArticles(pmid);
		return relatedLinksZone;
	}

	@AfterRender
	public void afterRender() {
		Link loadFulltextLinksEventLink = resources.createEventLink("getFulltextLinks", docId);
		Link loadRelatedArticlesEventLink = resources.createEventLink("getRelatedArticles", docId);

		javaScriptSupport.addScript("getFulltextLinks('%s', '%s')", loadFulltextLinksEventLink,
				loaderImage.toClientURL());
		javaScriptSupport.addScript("getRelatedArticles('%s', '%s')", loadRelatedArticlesEventLink,
				loaderImage.toClientURL());

		logger.info("Viewed document: \"" + docId + "\"");
	}

	public void onDisplayRelatedArticle(String pmid) {
		this.docId = pmid;
	}

	public boolean isNotLastAuthor() {
		return authorIndex < article.getDocument().getAuthors().size() - 1;
	}

	public SimpleDateFormat getDateFormat() {
		return dateFormat;
	}

	public String getPubmedURL() {
		return "http://www.ncbi.nlm.nih.gov/pubmed/" + article.getDocument().getPmid();
	}

	public String getPubmedCentralURL() {
		return "http://www.ncbi.nlm.nih.gov/pmc/" + article.getDocument().getPmcid();
	}

	public boolean hasRelatedArticles() {
		return relatedArticles != null && relatedArticles.size() > 0;
	}

	public boolean hasFulltextLinks() {
		return externalLinks != null && externalLinks.size() > 0;
	}

	public boolean isPmc() {
		return false;
		//return article.getDocument().getIndexType().equals(IIndexInformationService.Indexes.Indices.pmc);
	}

	public Link set(String docId, String indexType, ParseTree highlightingQuery, AbstractUserInterfaceState uiState) {
		this.docId = docId;
		this.indexType = indexType;
		this.highlightingQuery = highlightingQuery;
		this.uiState = uiState;

		return pageRenderLinkSource.createPageRenderLink(this.getClass());
	}

	public String getReferenceString() {
		Publication publication = article.getDocument().getPublication();
		String title = publication.getTitle();
		Date date = publication.getDate();
		String volume = publication.getVolume();
		String issue = publication.getIssue();
		String pages = publication.getPages();

		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(title)) {
			sb.append("<span id=\"publicationTitle\">");
			sb.append(title);
			sb.append("</span>");
			sb.append(". ");
		}
		if (date != null) {
			sb.append(dateFormat.format(date));
			sb.append("; ");
		}
		if (!StringUtils.isBlank(volume)) {
			sb.append(volume);
			if (!StringUtils.isBlank(issue)) {
				sb.append(" (");
				sb.append(issue);
				sb.append(")");
			}
			sb.append(": ");
			if (!StringUtils.isBlank(pages))
				sb.append(pages);
		}

		return sb.toString();
	}
}