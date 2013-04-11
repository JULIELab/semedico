/**
 * HighlightCommand.java
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

import java.util.ArrayList;
import java.util.List;

public class HighlightCommand {
	public List<String> fields = new ArrayList<String>();
	public String pre;
	public String post;
	public int snippets = Integer.MIN_VALUE;
	public int fragsize = Integer.MIN_VALUE;
}
