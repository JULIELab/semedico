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
	public RuleBasedCollator getCollator();
}

