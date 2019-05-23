package de.julielab.semedico.services;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.Request;

import de.julielab.semedico.base.TabPersistentField;
import de.julielab.semedico.core.services.ConfigurationSymbolProvider;
import de.julielab.semedico.core.services.DocumentRetrievalSearchStateCreator;
import de.julielab.semedico.core.services.SemedicoCoreProductionModule;
import de.julielab.semedico.core.services.interfaces.DocumentRetrievalUserInterfaceCreator;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.ClientIdentificationService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.state.SemedicoSessionStateCreator;
import de.julielab.semedico.util.RequireSessionFilter;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 */
// Without that, the core module would not be loaded.
@ImportModule({ SemedicoCoreProductionModule.class })
public class SemedicoFrontendModule {

	public static void contributeSymbolSource(@Autobuild ConfigurationSymbolProvider symbolProvider,
			final OrderedConfiguration<SymbolProvider> configuration) {
		configuration.add("SemedicoConfigurationSymbols", symbolProvider, "before:ApplicationDefaults");
	}

	public static void bind(ServiceBinder binder) {
		binder.bind(IStatefulSearchService.class, StatefulSearchService.class);
		binder.bind(ITokenInputService.class, TokenInputService.class);
	}

	public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
		// The application version number is incorprated into URLs for some
		// assets. Web browsers will cache assets because of the far future
		// expires
		// header. If existing assets are changed, the version number should
		// also
		// change, to force the browser to download new versions.
		configuration.override(SymbolConstants.APPLICATION_VERSION, "2.2.2");
		configuration.override(SymbolConstants.PRODUCTION_MODE, "false");
		// Deactivate the use of the default hibernate.cfg.xml configuration
		// file in favor of the direct Hibernate service contribution above
		// (contributeHibernateSessionSource).
		// configuration.override(HibernateSymbols.DEFAULT_CONFIGURATION,
		// "false");

	}

	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
		// Contributions to ApplicationDefaults will override any contributions to
		// FactoryDefaults (with the same key). Here we're restricting the supported
		// locales to just "en" (English). As you add localised message catalogs
		// and other assets,
		// you can extend this list of locales (it's a comma separated series of
		// locale names;
		// the first locale name is the default when there's no reasonable match).

		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");

		configuration.add(SymbolConstants.HMAC_PASSPHRASE, "juliesemedicopassphrase");

		// Support for jQuery is new in Tapestry 5.4 and will become the only
		// supported option in 5.5.
		configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
		configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "context:mybootstrap");
		configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
	}

	@Contribute(ComponentRequestHandler.class)
	public static void contributeRequestFilters(final OrderedConfiguration<ComponentRequestFilter> filters) {

		filters.addInstance("RequiresSessionFilter", RequireSessionFilter.class, "after:ErrorFilter");
	}

	public void contributePersistentFieldManager(MappedConfiguration<String, PersistentFieldStrategy> configuration,
			ApplicationStateManager asm) {
		configuration.add(TabPersistentField.TAB, new TabPersistentField(asm));
	}

	public void contributeApplicationStateManager(
			MappedConfiguration<Class<?>, ApplicationStateContribution> configuration, @Inject Request request,
			@Inject LoggerSource loggerSource, @Autobuild DocumentRetrievalSearchStateCreator docRetrievalSSCreator,
			@Autobuild DocumentRetrievalUserInterfaceCreator docRetrievalUiStateCreator) {
		configuration.add(SemedicoSessionState.class, new ApplicationStateContribution("session",
				new SemedicoSessionStateCreator(loggerSource, docRetrievalSSCreator, docRetrievalUiStateCreator)));
		configuration.add(Client.class,
				new ApplicationStateContribution("session", new ClientIdentificationService(request)));
	}
}
