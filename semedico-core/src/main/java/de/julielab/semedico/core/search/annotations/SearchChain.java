package de.julielab.semedico.core.search.annotations;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;

/**
 * Contains the following search components:
 * <ol>
 * <li>QueryTranslation</li>
 * <li>RequestCreation</li>
 * <li>SearchOptionConfiguration</li>
 * <li>SemedicoConfigurationApplication</li>
 * <li>SearchServer</li>
 * <li>ShortCircuit</li>
 * </ol>
 */
@Retention(RUNTIME)
public @interface SearchChain {

}
