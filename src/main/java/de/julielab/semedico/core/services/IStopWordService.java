/**
 * IStopWordService.java
 *
 * Copyright (c) 2011, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 07.06.2011
 **/

package de.julielab.semedico.core.services;

import java.util.List;
import java.util.Set;

/**
 * Service for delivering stop words.
 * @author faessler
 */
public interface IStopWordService {
	public Set<String> getAsSet();
	public List<String> getAsList();
	public String[] getAsArray();
}
