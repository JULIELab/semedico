package de.julielab.semedico.eval.services;

import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.internal.services.ClasspathResourceSymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;

import de.julielab.semedico.core.services.SemedicoCoreModule;

@ImportModule(SemedicoCoreModule.class)
public class SemedicoEvaluationModule {
	public static void contributeSymbolSource(final OrderedConfiguration<SymbolProvider> configuration) {
		String username = System.getProperty("user.name");
		String configFileName = "configuration.properties." + username;
		configuration.add("SemedicoToolsSymbols", new ClasspathResourceSymbolProvider(configFileName),
				"before:ApplicationDefaults");
	}
}
