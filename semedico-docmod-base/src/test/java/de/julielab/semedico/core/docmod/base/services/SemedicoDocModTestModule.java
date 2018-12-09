package de.julielab.semedico.core.docmod.base.services;

import de.julielab.semedico.core.services.SemedicoCoreTestModule;
import org.apache.tapestry5.ioc.annotations.ImportModule;

@ImportModule({DocModBaseModule.class, SemedicoCoreTestModule.class})
public class SemedicoDocModTestModule {
}
