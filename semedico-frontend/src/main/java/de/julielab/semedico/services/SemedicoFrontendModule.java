package de.julielab.semedico.services;

import de.julielab.semedico.base.TabPersistentField;
import de.julielab.semedico.core.services.ConfigurationSymbolProvider;
import de.julielab.semedico.core.services.DocumentRetrievalSearchStateCreator;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import de.julielab.semedico.core.services.DocumentRetrievalUserInterfaceCreator;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.ClientIdentificationService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.state.SemedicoSessionStateCreator;
import de.julielab.semedico.util.RequireSessionFilter;
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
import org.apache.tapestry5.services.*;

/**
 * This module is automatically included as part of the Tapestry IoC Registry,
 * it's a good place to configure and extend Tapestry, or to place your own
 * service definitions.
 */
// Without that, the core module would not be loaded.
@ImportModule({ SemedicoCoreModule.class })
public class SemedicoFrontendModule {

	public static void contributeSymbolSource(@Autobuild ConfigurationSymbolProvider symbolProvider,
			final OrderedConfiguration<SymbolProvider> configuration) {
		configuration.add("SemedicoConfigurationSymbols", symbolProvider, "before:ApplicationDefaults");
	}

	public static void bind(ServiceBinder binder) {
		binder.bind(ITokenInputService.class, TokenInputService.class);
	}

	// Deactivates Tapestry default style sheets (tapestry.css)
	// @Contribute(MarkupRenderer.class)
	// public static void
	// deactiveDefaultCSS(OrderedConfiguration<MarkupRendererFilter>
	// configuration)
	// {
	// configuration.override("InjectDefaultStylesheet", null);
	// }

	/**
	 * <p>
	 * The complete Hibernate configuration is done by this service
	 * configuration.
	 * </p>
	 * <p>
	 * This way, we can configure the database connection for Hibernate
	 * according to the central Semedico configuration file. Then, no Hibernate
	 * configuration file <tt>hibernate.cfg.xml</tt> is required, IF the
	 * appropriate symbol indicating the use of the default configuration is set
	 * to <tt>false</tt>. This is done in this modul's
	 * {@link #contributeApplicationDefaults(MappedConfiguration)}.
	 * </p>
	 * 
	 * @see #contributeApplicationDefaults(MappedConfiguration)
	 * @param config
	 * @param serverName
	 * @param portNumber
	 * @param databaseName
	 * @param user
	 * @param password
	 */
	// public static void
	// contributeHibernateSessionSource(OrderedConfiguration<HibernateConfigurer>
	// config,
	// @Symbol(SemedicoSymbolConstants.DATABASE_SERVER) final String serverName,
	// @Symbol(DATABASE_PORT) final int portNumber,
	// @Symbol(SemedicoSymbolConstants.DATABASE_NAME) final String databaseName,
	// @Symbol(DATABASE_USER) final String user, @Symbol(DATABASE_PASSWORD)
	// final String password) {
	// config.add("Custom", new HibernateConfigurer() {
	//
	// @Override
	// public void configure(Configuration configuration) {
	// configuration.setProperty("hibernate.connection.url",
	// String.format("jdbc:postgresql://%s:%d/%s", serverName, portNumber,
	// databaseName));
	// configuration.setProperty("hibernate.connection.username", user);
	// configuration.setProperty("hibernate.connection.password", password);
	//
	// configuration.setProperty("hibernate.connection.driver_class",
	// "org.postgresql.Driver");
	// configuration.setProperty("hibernate.dialect",
	// "org.hibernate.dialect.PostgreSQLDialect");
	// // The following property determines whether tables should be
	// // created automatically or not. The 'update' setting creates
	// // automatically and allows updates.
	// configuration.setProperty("hibernate.hbm2ddl.auto", "update");
	// configuration.setProperty("hibernate.show_sql", "true");
	// configuration.setProperty("hibernate.format_sql", "true");
	//
	// // The next configuration parameters do one thing: When the database is
	// down or not available, only wait
	// // 5 seconds for timeout and not minutes as the default is. For this to
	// work, the hibernate 3cp0
	// // dependency must be present which is done for the frontend POM.
	// configuration.setProperty("hibernate.c3p0.loginTimeout", "5");
	// configuration.setProperty("hibernate.c3p0.acquireRetryAttempts", "1");
	// configuration.setProperty("hibernate.c3p0.acquireRetryDelay", "1000");
	// }
	//
	// }, "after:Default");
	// }

	public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
		// The application version number is incorprated into URLs for some
		// assets. Web browsers will cache assets because of the far future
		// expires
		// header. If existing assets are changed, the version number should
		// also
		// change, to force the browser to download new versions.
		configuration.override(SymbolConstants.APPLICATION_VERSION, "2.3.0-SNAPSHOT");
		configuration.override(SymbolConstants.PRODUCTION_MODE, "false");
		// Deactivate the use of the default hibernate.cfg.xml configuration
		// file in favor of the direct Hibernate service contribution above
		// (contributeHibernateSessionSource).
		// configuration.override(HibernateSymbols.DEFAULT_CONFIGURATION,
		// "false");

	}

	public static void contributeApplicationDefaults(MappedConfiguration<String, Object> configuration) {
		// Contributions to ApplicationDefaults will override any contributions
		// to
		// FactoryDefaults (with the same key). Here we're restricting the
		// supported
		// locales to just "en" (English). As you add localised message catalogs
		// and other assets,
		// you can extend this list of locales (it's a comma separated series of
		// locale names;
		// the first locale name is the default when there's no reasonable
		// match).

		configuration.add(SymbolConstants.SUPPORTED_LOCALES, "en");

		configuration.add(SymbolConstants.HMAC_PASSPHRASE, "juliesemedicopassphrase");

		// Support for jQuery is new in Tapestry 5.4 and will become the only
		// supported
		// option in 5.5.
		configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
		configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "context:mybootstrap");
		configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
	}

	/**
	 * This is a service definition, the service will be named "TimingFilter".
	 * The interface, RequestFilter, is used within the RequestHandler service
	 * pipeline, which is built from the RequestHandler service configuration.
	 * Tapestry IoC is responsible for passing in an appropriate Logger
	 * instance. Requests for static resources are handled at a higher level, so
	 * this filter will only be invoked for Tapestry related requests.
	 * 
	 * <p>
	 * Service builder methods are useful when the implementation is inline as
	 * an inner class (as here) or require some other kind of special
	 * initialization. In most cases, use the static bind() method instead.
	 * 
	 * <p>
	 * If this method was named "build", then the service id would be taken from
	 * the service interface and would be "RequestFilter". Since Tapestry
	 * already defines a service named "RequestFilter" we use an explicit
	 * service id that we can reference inside the contribution method.
	 */
	// public RequestFilter buildTimingFilter(final Logger log) {
	// return new RequestFilter() {
	// public boolean service(Request request, Response response, RequestHandler
	// handler) throws IOException {
	// long startTime = System.currentTimeMillis();
	//
	// try {
	// // The responsibility of a filter is to invoke the
	// // corresponding method
	// // in the handler. When you chain multiple filters together,
	// // each filter
	// // received a handler that is a bridge to the next filter.
	//
	// return handler.service(request, response);
	// } finally {
	// long elapsed = System.currentTimeMillis() - startTime;
	//
	// log.info(String.format("Request time for %s: %d ms", request.getPath(),
	// elapsed));
	// }
	// }
	// };
	// }

	/**
	 * This is a contribution to the RequestHandler service configuration. This
	 * is how we extend Tapestry using the timing filter. A common use for this
	 * kind of filter is transaction management or security. The @Local
	 * annotation selects the desired service by type, but only from the same
	 * module. Without @Local, there would be an error due to the other
	 * service(s) that implement RequestFilter (defined in other modules).
	 */
	// public void contributeRequestHandler(OrderedConfiguration<RequestFilter>
	// configuration, @Local RequestFilter
	// filter) {
	// Each contribution to an ordered configuration has a name, When
	// necessary, you may
	// set constraints to precisely control the invocation order of the
	// contributed filter
	// within the pipeline.

	// configuration.add("Timing", filter);
	// }

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
