package de.julielab.semedico.core.search.query;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.translation.SearchTask;

/**
 * The main query of this class is actually the article ID of the article that
 * should be fetched. The ParseTree query object returned by {@link #getQuery()}
 * is just the highlighting query for the article.
 * 
 * @author faessler
 *
 */
public class ArticleQuery extends ParseTreeQueryBase {

	private String articleId;

	public ArticleQuery(String articleId) {
		super(SearchTask.GET_ARTICLE);
		this.articleId = articleId;
	}

	public String getArticleId() {
		return articleId;
	}

	@Override
	public void setQuery(ParseTree query) {
		// The same method as in the super class except we do not assert the
		// query to be non-null - because it might be null, there will just be
		// no highlighting, then.
		this.query = query;
	}

}
