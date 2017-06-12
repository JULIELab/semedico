package de.julielab.semedico.core.query;

import de.julielab.semedico.core.query.translation.SearchTask;

public class ArticleQuery extends ParseTreeQueryBase {

	private String articleId;

	public ArticleQuery(String articleId) {
		super(SearchTask.GET_ARTICLE);
		this.articleId = articleId;
	}

	public String getArticleId() {
		return articleId;
	}

}
