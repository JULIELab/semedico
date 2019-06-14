package de.julielab.semedico.core.docmod.center.services;

import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.services.SemedicoCoreModule;
import org.apache.tapestry5.ioc.annotations.ImportModule;

@ImportModule({SemedicoCoreModule.class, DefaultDocumentModule.class})
public class DocModCenterModule {
}
