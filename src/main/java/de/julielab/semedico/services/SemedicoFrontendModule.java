package de.julielab.semedico.services;

import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_INIT_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_MAX_CONN;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PASSWORD;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_PORT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_SERVER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DATABASE_USER;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_FACET_COUNT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.DISPLAY_TERMS_MIN_HITS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.EVENT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.FACETS_LOAD_AT_START;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.FACET_ROOT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.GET_HOLLOW_FACETS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABELS_DEFAULT_NUMBER_DISPLAYED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.LABEL_HIERARCHY_INIT_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_DISPLAYED_FACETS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.MAX_NUMBER_SEARCH_NODES;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.NEO4J_REST_ENDPOINT;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.RELATION_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.ROOT_PATH_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SEARCH_MAX_NUMBER_DOC_HITS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.STOP_WORDS_FILE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_ACTIVATED;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_FILTER_INDEX_TERMS;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.SUGGESTIONS_INDEX_NAME;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERM_CACHE_SIZE;
import static de.julielab.semedico.core.services.SemedicoSymbolConstants.TERM_DICT_FILE;

import org.apache.tapestry5.SymbolConstants;
import org.apache.tapestry5.ioc.LoggerSource;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.OrderedConfiguration;
import org.apache.tapestry5.ioc.ServiceBinder;
import org.apache.tapestry5.ioc.annotations.Autobuild;
import org.apache.tapestry5.ioc.annotations.Contribute;
import org.apache.tapestry5.ioc.annotations.ImportModule;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.internal.services.ClasspathResourceSymbolProvider;
import org.apache.tapestry5.ioc.services.SymbolProvider;
import org.apache.tapestry5.services.ApplicationStateContribution;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.ComponentRequestFilter;
import org.apache.tapestry5.services.ComponentRequestHandler;
import org.apache.tapestry5.services.PersistentFieldStrategy;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import de.julielab.elastic.query.ElasticQuerySymbolConstants;
import de.julielab.semedico.base.TabPersistentField;
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

	public static void contributeSymbolSource(Logger logger, final OrderedConfiguration<SymbolProvider> configuration) {
		try {
			String configFileName = System.getProperty("semedico.configuration");
			if (null == configFileName) {
				String username = System.getProperty("user.name");
				configFileName = "configuration.properties." + username;
			}
			configuration.add("DevSymbols", new ClasspathResourceSymbolProvider(configFileName),
					"before:ApplicationDefaults");
		} catch (NullPointerException e) {
			logger.info(
					"No configuration file found in the classpath. Using default configuration in Application Module ({}).",
					SemedicoFrontendModule.class.getCanonicalName());
		}
	}

	public static void bind(ServiceBinder binder) {
		binder.bind(IStatefulSearchService.class, StatefulSearchService.class);
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
		configuration.override(SymbolConstants.APPLICATION_VERSION, "2.0.2");
		configuration.override(SymbolConstants.PRODUCTION_MODE, "true");
		// Deactivate the use of the default hibernate.cfg.xml configuration
		// file in favor of the direct Hibernate service contribution above
		// (contributeHibernateSessionSource).
		// configuration.override(HibernateSymbols.DEFAULT_CONFIGURATION,
		// "false");

		// Contributions to ApplicationDefaults will be used when the
		// corresponding symbol is not delivered by any SymbolProvider and
		// override
		// any contributions to
		// FactoryDefaults (with the same key).
		// In Semedico, the defaults are meant to reflect the productive
		// environment while for testing a separate configuration file can be
		// used via SemedicoSymbolProvider.
		// Postgres is currently still used by hibernate (BBatchAnalysis)
		configuration.add(DATABASE_NAME, "semedico_stag_poc");
		configuration.add(DATABASE_SERVER, "darwin");
		configuration.add(DATABASE_USER, "postgres");
		configuration.add(DATABASE_PASSWORD, "$postgr3s$$");
		configuration.add(DATABASE_PORT, "5432");
		configuration.add(DATABASE_MAX_CONN, "4");
		configuration.add(DATABASE_INIT_CONN, "1");

		// ------------ Neo4j ---------------
		configuration.add(NEO4J_REST_ENDPOINT, "http://dawkins:7474/");
		configuration.add("semedico.neo4j.username", "neo4j");
		configuration.add("semedico.neo4j.password", "julielab");

		// configuration.add(SOLR_URL, "http://192.168.1.15:8983/solr/");

		// ------------ ELASTIC SEARCH ---------------
		// We use the "Transport Client" for ElasticSearch, so we need host and
		// port.
		configuration.add(ElasticQuerySymbolConstants.ES_HOST, "dawkins");
		configuration.add(ElasticQuerySymbolConstants.ES_PORT, "9300");
		// We have to give the cluster name anyway because the ES client service
		// requires it.
		configuration.add(ElasticQuerySymbolConstants.ES_CLUSTER_NAME, "semedico-development");
		configuration.add(SUGGESTIONS_INDEX_NAME, "suggestions");
		configuration.add(SUGGESTIONS_ACTIVATED, "true");
		configuration.add(SUGGESTIONS_FILTER_INDEX_TERMS, "true");

		configuration.add(FACETS_LOAD_AT_START, "true");
		configuration.add(GET_HOLLOW_FACETS, "false");
		configuration.add(LABELS_DEFAULT_NUMBER_DISPLAYED, "5");
		configuration.add(LABEL_HIERARCHY_INIT_CACHE_SIZE, "5");
		configuration.add(MAX_NUMBER_SEARCH_NODES, "2");
		configuration.add(MAX_DISPLAYED_FACETS, "20");
		// configuration.add(TERMS_DO_NOT_BUILD_STRUCTURE, "false");
		configuration.add(DISPLAY_TERMS_MIN_HITS, "0");
		configuration.add(DISPLAY_MESSAGE_WHEN_NO_CHILDREN_HIT, "false");
		configuration.add(DISPLAY_MESSAGE_WHEN_NO_FACET_ROOTS_HIT, "false");
		configuration.add(DISPLAY_FACET_COUNT, "true");
		configuration.add(TERM_CACHE_SIZE, "5000000");
		configuration.add(EVENT_CACHE_SIZE, "100000");
		configuration.add(RELATION_CACHE_SIZE, "10000000");
		configuration.add(FACET_ROOT_CACHE_SIZE, "500");
		configuration.add(ROOT_PATH_CACHE_SIZE, "1000");
		// store into the DB?
		configuration.add(STOP_WORDS_FILE, "/root/development/stopwords.txt");
		// store into the DB?
		configuration.add(TERM_DICT_FILE, "/root/development/query.dic");
		configuration.add("semedico.core.search.maxFacettedDocuments", "300000");
		configuration.add(SEARCH_MAX_NUMBER_DOC_HITS, "10");
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
		
		// Support for jQuery is new in Tapestry 5.4 and will become the only supported
        // option in 5.5.
		configuration.add(SymbolConstants.JAVASCRIPT_INFRASTRUCTURE_PROVIDER, "jquery");
		configuration.add(SymbolConstants.BOOTSTRAP_ROOT, "context:mybootstrap");
		configuration.add(SymbolConstants.MINIFICATION_ENABLED, true);
	}

	// public static ObjectProvider buildHiveMind(final Logger log){
	//
	// return new HiveMindObjectProvider(log);
	// }

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
			@Autobuild DocumentRetrievalUserInterfaceCreator docRetrievalUiStateCreator

	// @Autobuild UserInterfaceStateCreator userInterfaceStateCreator,
	// @Autobuild BTermUserInterfaceStateCreator bTermUserInterfaceStateCreator,
	// @Autobuild SearchStateCreator searchStateCreator
	) {
		configuration.add(SemedicoSessionState.class,
				new ApplicationStateContribution("session",
						new SemedicoSessionStateCreator(loggerSource, docRetrievalSSCreator, docRetrievalUiStateCreator)));
		// configuration.add(UserInterfaceState.class, new
		// ApplicationStateContribution("session",
		// userInterfaceStateCreator));
		// configuration.add(BTermUserInterfaceState.class, new
		// ApplicationStateContribution("session",
		// bTermUserInterfaceStateCreator));
		// configuration.add(SearchState.class, new
		// ApplicationStateContribution("session", searchStateCreator));
		configuration.add(Client.class,
				new ApplicationStateContribution("session", new ClientIdentificationService(request)));
	}
}
