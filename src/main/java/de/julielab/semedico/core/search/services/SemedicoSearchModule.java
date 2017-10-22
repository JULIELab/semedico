package de.julielab.semedico.core.search.services;

import java.util.List;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.semedico.core.search.annotations.ArticleChain;
import de.julielab.semedico.core.search.annotations.DocumentChain;
import de.julielab.semedico.core.search.annotations.FacetedDocumentSearchSubchain;
import de.julielab.semedico.core.search.annotations.SentenceSearchChain;
import de.julielab.semedico.core.search.annotations.StatementSearchChain;
import de.julielab.semedico.core.search.annotations.TermSelectChain;
import de.julielab.semedico.core.search.components.ArticleResponseProcessComponent.ArticleResponseProcess;
import de.julielab.semedico.core.search.components.ArticleSearchPreparationComponent.ArticleSearchPreparation;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent.FromQueryUIPreparation;
import de.julielab.semedico.core.search.components.NewSearchUIPreparationComponent.NewSearchUIPreparation;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.ResultListCreationComponent.ResultListCreation;
import de.julielab.semedico.core.search.components.TermSelectUIPreparationComponent.TermSelectUIPreparation;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent.TextSearchPreparation;

public class SemedicoSearchModule {

	private ChainBuilder chainBuilder;
	private ISearchComponent queryTranslationComponent;
	private ISearchServerComponent searchServerComponent;
	private ISearchComponent textSearchPreparationComponent;
	private ISearchComponent resultListCreationComponent;

	public SemedicoSearchModule(ChainBuilder chainBuilder, @QueryTranslation ISearchComponent queryTranslationComponent,
			ISearchServerComponent searchServerComponent,
			@TextSearchPreparation ISearchComponent textSearchPreparationComponent,
			@ResultListCreation ISearchComponent resultListCreationComponent) {
		this.chainBuilder = chainBuilder;
		this.queryTranslationComponent = queryTranslationComponent;
		this.searchServerComponent = searchServerComponent;
		this.textSearchPreparationComponent = textSearchPreparationComponent;
		this.resultListCreationComponent = resultListCreationComponent;

	}

	@Marker(StatementSearchChain.class)
	public ISearchComponent buildStatementSearchChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@StatementSearchChain
	public void contributeStatementSearchChain(OrderedConfiguration<ISearchComponent> configuration) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("SearchServer", searchServerComponent);
		// TODO statement result component
	}

	@Contribute(ISearchComponent.class)
	@SentenceSearchChain
	public void contributeSentenceSearchChain(OrderedConfiguration<ISearchComponent> configuration) {
		configuration.add(QueryTranslation.class.getSimpleName(), queryTranslationComponent);
		configuration.add(ISearchServerComponent.class.getSimpleName(), searchServerComponent);
	}

	@Marker(TermSelectChain.class)
	public ISearchComponent buildTermSelectChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@TermSelectChain
	public void contributeTermSelectChain(OrderedConfiguration<ISearchComponent> configuration,
			@TermSelectUIPreparation ISearchComponent termSelectUIPreparationComponent,
			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain) {
		configuration.add("TermSelectUIPreparation", termSelectUIPreparationComponent);
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
		configuration.add("FacetedDocumentSearch", facetedDocumentSearchSubchain);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add("ResultListCreation", resultListCreationComponent);
	}

	@Marker(DocumentChain.class)
	public ISearchComponent buildDocumentChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@DocumentChain
	public void contributeDocumentChain(OrderedConfiguration<ISearchComponent> configuration,
			@NewSearchUIPreparation ISearchComponent newSearchUIPreparationComponent,
			@FromQueryUIPreparation ISearchComponent fromQueryUIPreparationComponent) {
		configuration.add("NewSearchUIPreparation", newSearchUIPreparationComponent);
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add("ResultListCreation", resultListCreationComponent);
		configuration.add("FromQueryUIPreparation", fromQueryUIPreparationComponent);
	}

	@Marker(ArticleChain.class)
	public ISearchComponent buildArticleChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}

	@Contribute(ISearchComponent.class)
	@ArticleChain
	public void contributeArticleChain(OrderedConfiguration<ISearchComponent> configuration,
			@ArticleSearchPreparation ISearchComponent articleSearchPreparationComponent,
			@ArticleResponseProcess ISearchComponent articleResponseProcessComponent) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("ArticleSearchPreparation", articleSearchPreparationComponent);
		configuration.add("SearchServer", searchServerComponent);
		configuration.add("ArticleResponseProcess", articleResponseProcessComponent);
	}
}
