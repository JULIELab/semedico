package de.julielab.semedico.services;

import de.julielab.semedico.base.TabPersistentField;
import de.julielab.semedico.commons.services.ConfigurationSymbolProvider;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.docmod.base.services.DocModBaseModule;
import de.julielab.semedico.core.services.DocumentRetrievalSearchStateCreator;
import de.julielab.semedico.core.services.DocumentRetrievalUserInterfaceCreator;
import de.julielab.semedico.core.services.SemedicoCoreModule;
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
@ImportModule({SemedicoCoreModule.class, DocModBaseModule.class, DefaultDocumentModule.class})
public class SemedicoFrontendModule {

    public static void contributeSymbolSource(@Autobuild ConfigurationSymbolProvider symbolProvider,
                                              final OrderedConfiguration<SymbolProvider> configuration) {
        configuration.add("SemedicoConfigurationSymbols", symbolProvider, "before:ApplicationDefaults");
    }

    public static void bind(ServiceBinder binder) {
    }

    public static void contributeFactoryDefaults(MappedConfiguration<String, Object> configuration) {
        // The application version number is incorprated into URLs for some
        // assets. Web browsers will cache assets because of the far future
        // expires
        // header. If existing assets are changed, the version number should
        // also
        // change, to force the browser to download new versions.
        configuration.override(SymbolConstants.APPLICATION_VERSION, "3.1.0-SNAPSHOT");
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
