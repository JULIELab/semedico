package de.julielab.scicopia.core.elasticsearch.legacy;

import org.apache.tapestry5.ioc.ServiceBinder;

import de.julielab.scicopia.core.elasticsearch.legacy.ElasticSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerComponent;

public class ElasticQueryComponentsModule {
	public static void bind(ServiceBinder binder) {
		binder.bind(ISearchClientProvider.class, ElasticSearchClientProvider.class);
		binder.bind(ISearchServerComponent.class, ElasticSearchComponent.class);
		binder.bind(IIndexingService.class, ElasticSearchIndexingService.class);
	}
}
