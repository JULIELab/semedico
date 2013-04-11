/**
 * SemedicoSearchResult.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 08.04.2013
 **/

/**
 * 
 */
package de.julielab.semedico.search.components;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.util.LazyDisplayGroup;
import de.julielab.util.SolrTfDfTripleStream;

/**
 * @author faessler
 *
 */
public class SemedicoSearchResult {
	public LazyDisplayGroup<DocumentHit> documentHits;
	public long elapsedTime;
	public SolrTfDfTripleStream searchNodeTermCounts;
}

