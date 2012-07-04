/**
 * RuleBasedCollatorWrapper.java
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
package de.julielab.semedico.core.services;

import com.ibm.icu.text.RuleBasedCollator;

import de.julielab.semedico.core.services.interfaces.IRuleBasedCollatorWrapper;

/**
 * @author faessler
 *
 */
public class RuleBasedCollatorWrapper implements IRuleBasedCollatorWrapper {
	private final RuleBasedCollator collator;

	public RuleBasedCollatorWrapper(RuleBasedCollator collator) {
		this.collator = collator;
	}
	
	/* (non-Javadoc)
	 * @see de.julielab.semedico.core.services.IRuleBasedCollatorWrapper#getCollator()
	 */
	@Override
	public RuleBasedCollator getCollator() {
		return collator;
	}
	
	
}

