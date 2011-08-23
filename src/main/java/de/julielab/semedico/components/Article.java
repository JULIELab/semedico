package de.julielab.semedico.components;

import java.io.IOException;
import java.text.SimpleDateFormat;

import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SetupRender;
import org.apache.tapestry5.ioc.annotations.Inject;

import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.ExternalLink;
import de.julielab.semedico.core.SearchSessionState;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.services.IDocumentCacheService;
import de.julielab.semedico.core.services.IDocumentService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.search.IKwicService;

public class Article {

	@Parameter
	private int pubMedId;

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
	private SearchSessionState searchSessionState;
	
	@Inject
	private IQueryTranslationService queryTranslationService;
	

	@Inject
	private IDocumentCacheService documentCacheService;

	@Inject
	private IDocumentService documentService;

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


	private static SimpleDateFormat dateFormat = new SimpleDateFormat(
			"yyyy MMMMM dd");

	@SetupRender
	public void initialize() throws IOException {
		System.out.println(pubMedId);
		article = documentCacheService.getCachedDocument(pubMedId);
		if (article == null) {
			article = documentService.readDocumentWithPubmedId(pubMedId);
			documentCacheService.addDocument(article);
		}
		createHighlightedArticle();
	}

	public void createHighlightedArticle() throws IOException {
		String query = queryTranslationService
				.createKwicQueryFromTerms(searchSessionState.getSearchState().getQueryTerms());

		String text = kwicService.createHighlightedAbstract(query, article);
		String title = kwicService.createHighlightedTitle(query, article);

		if (text != null && !text.equals(""))
			highlightedAbstractText = text;
		else
			highlightedAbstractText = article.getAbstractText();

		if (title != null && !title.equals(""))
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

	public String getPubmedURL() {
		return "http://www.ncbi.nlm.nih.gov/pubmed/" + article.getPubmedId();
	}

	public boolean hasRelatedArticles() {
		return article.getRelatedArticles().size() > 0;
	}

	public boolean hasFulltextLinks() {
		return article.getExternalLinks().size() > 0;
	}
}
