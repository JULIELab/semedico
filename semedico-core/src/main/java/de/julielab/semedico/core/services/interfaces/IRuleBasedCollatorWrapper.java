/**
 * IRuleBasedCollatorWrapper.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 18.06.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.services.interfaces;

import com.ibm.icu.text.RuleBasedCollator;

/**
 * @author faessler
 * 
 */
public interface IRuleBasedCollatorWrapper {
	/**
	 * <p>
	 * Clones the wrapped collator and returns the clone. The returned clone is
	 * <i>not</i> frozen, regardless of the wrapped collator's state. This
	 * operation is thread-safe.
	 * </p>
	 * 
	 * @return A clone of the wrapped collator
	 * @see http://userguide.icu-project.org/collation/architecture
	 */
	public RuleBasedCollator getCollator();

	public int compare(String source, String target);
}
