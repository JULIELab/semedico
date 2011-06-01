/** 
 * ILabelHitCounterService.java
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
 * Creation date: 03.04.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.search;

import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.response.FacetField;

import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;

public interface IFacetHitCollectorService {

	public void setFacetFieldList(List<FacetField> facetFields);
	
	public FacetHit collectFacetHits(Collection<FacetConfiguration> facetConfigurations);

}
