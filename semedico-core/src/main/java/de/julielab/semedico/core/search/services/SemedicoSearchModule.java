package de.julielab.semedico.core.search.services;

import java.util.List;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerComponent;
import de.julielab.semedico.core.search.annotations.StatementSearch;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;

public class SemedicoSearchModule {
	
	private ChainBuilder chainBuilder;
	private ISearchComponent queryTranslationComponent;
	private ISearchServerComponent searchServerComponent;
	
	public SemedicoSearchModule(@QueryTranslation ISearchComponent queryTranslationComponent, ISearchServerComponent searchServerComponent) {
		this.queryTranslationComponent = queryTranslationComponent;
		this.searchServerComponent = searchServerComponent;
		
	}

	public SemedicoSearchModule(ChainBuilder chainBuilder) {
		this.chainBuilder = chainBuilder;
	}
	
	@Marker(StatementSearch.class)
	public ISearchComponent buildFactSearchChain(List<ISearchComponent> commands) {
		return chainBuilder.build(ISearchComponent.class, commands);
	}
	
	@Contribute(ISearchComponent.class)
	@StatementSearch
	public void contributeDocumentChain(OrderedConfiguration<ISearchComponent> configuration) {
		configuration.add("QueryTranslation", queryTranslationComponent);
		configuration.add("SearchServer", searchServerComponent);
	}
}
