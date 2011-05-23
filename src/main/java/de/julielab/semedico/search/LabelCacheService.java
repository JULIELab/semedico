/** 
 * LabelCacheService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import java.util.Collection;
import java.util.Stack;

import de.julielab.semedico.core.Label;

public class LabelCacheService implements ILabelCacheService {

	private Stack<Label> labelCache;
	
	public LabelCacheService() {
		labelCache = new Stack<Label>(); 
	}
	
	@Override
	public Label getCachedLabel() {
		Label label = null;
		
		if( labelCache.size() > 0 )
			label = labelCache.pop();
		else
			label = new Label();
		
		return label;
	}

	@Override
	public void releaseLabels(Collection<Label> labels) {
		for( Label label: labels ){
			label.clear();
			labelCache.push(label);
		}
	}

}
