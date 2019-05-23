/**
 * ISearchComponent.java
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
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.scicopia.core.elasticsearch.legacy;

/**
 * @author faessler
 * 
 */
public interface ISearchComponent {
	public boolean process(SearchCarrier searchCarrier);

}
