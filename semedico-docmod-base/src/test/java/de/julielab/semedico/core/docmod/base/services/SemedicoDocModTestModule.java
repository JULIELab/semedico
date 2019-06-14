package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.docmod.base.search.DocumentModuleSearchTest;
import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.tapestry5.ioc.MappedConfiguration;
import org.apache.tapestry5.ioc.annotations.ImportModule;

@ImportModule({DocModBaseModule.class, SemedicoCoreTestModule.class, DefaultDocumentModule.class})
public class SemedicoDocModTestModule {

    public void contributeApplicationDefaults(MappedConfiguration<String, String> configuration) {
        // These settings are required when using the document module services
        configuration.add(DefaultDocumentModule.DEFAULT_DOCMOD_NAME, "Default Document Module");
        configuration.add(DefaultDocumentModule.DEFAULT_DOCMOD_ALLTEXT_INDEX, DocumentModuleSearchTest.TEST_INDEX);
    }
}
