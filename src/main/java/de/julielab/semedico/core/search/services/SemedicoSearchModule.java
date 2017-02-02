package de.julielab.semedico.core.search.services;

import java.util.List;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.apache.tapestry5.ioc.annotations.Marker;
import org.apache.tapestry5.ioc.annotations.Primary;
import org.apache.tapestry5.ioc.services.ChainBuilder;

import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.elastic.query.components.ISearchComponent.DocumentChain;
import de.julielab.elastic.query.components.ISearchComponent.FacetedDocumentSearchSubchain;
import de.julielab.semedico.core.query.translation.IQueryTranslator;
import de.julielab.semedico.core.search.components.FromQueryUIPreparatorComponent.FromQueryUIPreparation;
import de.julielab.semedico.core.search.components.NewSearchUIPreparationComponent.NewSearchUIPreparation;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.TextSearchPreparationComponent.TextSearchPreparation;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class SemedicoSearchModule {
	
	private ChainBuilder chainBuilder;

	public SemedicoSearchModule(ChainBuilder chainBuilder) {
		this.chainBuilder = chainBuilder;
	}
	
//	@Marker(DocumentChain.class)
//	public ISearchComponent buildDocumentChain(List<ISearchComponent> commands) {
//		return chainBuilder.build(ISearchComponent.class, commands);
//	}
//	
//	@Contribute(ISearchComponent.class)
//	@DocumentChain
//	public static void contributeDocumentChain(OrderedConfiguration<ISearchComponent> configuration,
//			@NewSearchUIPreparation ISearchComponent newSearchUIPreparationComponent,
//			@FromQueryUIPreparation ISearchComponent fromQueryUIPreparationComponent,
//			@QueryAnalysis ISearchComponent queryAnalysisComponent,
//			@QueryTranslation ISearchComponent queryTranslationComponent,
//			@TextSearchPreparation ISearchComponent textSearchPreparationComponent,
//			@FacetedDocumentSearchSubchain ISearchComponent facetedDocumentSearchSubchain) {
//		configuration.add("NewSearchUIPreparation", newSearchUIPreparationComponent);
//		configuration.add("QueryAnalysis", queryAnalysisComponent);
//		configuration.add("QueryTranslation", queryTranslationComponent);
//		configuration.add("TextSearchPreparation", textSearchPreparationComponent);
//		// configuration.add("FacetCountPreparation", facetCountComponent);
//		configuration.add("FacetedDocumentSearch", facetedDocumentSearchSubchain);
//		configuration.add("FromQueryUIPreparation", fromQueryUIPreparationComponent);
//	}
}
