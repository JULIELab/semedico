/** 
 * FacetRecommenderService.java
 * 
 * Copyright (c) 2014, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 *
 * Author: matthies
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: Sep 30, 2014 
 * 
 * The FacetRecommenderService is a Semedico service that returns a sorted List of Facets (fid)
 * depending on Input Terms (tid). That is, it weights whether the Term-Id has been derived from
 * a "synonym" or a "preferred name".
 * The input should be provided in the form tid_source (e.g. tid40_n, tid326_s).
 * 
 **/

package de.julielab.semedico.core.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IFacetRecommenderService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class FacetRecommenderService implements IFacetRecommenderService {
	static final String PREFERRED_NAME = "n";
	static final String SYNONYM = "s";
	static final String PARENTAL_LVL_BEGINNING = "p";
	
	private ITermService termService;

	/**
	 * A map for constant weights depending on the "facetSource" of the term.
	 * 
	 * 	@param <em>n</em> for being derived from a preferred name.
	 * 	@param <em>s</em> for being derived from a synonym.
	 */
	private static final HashMap<String,Integer> WEIGHTS = new HashMap<String,Integer>()
								{private static final long serialVersionUID = 1L;
									{
									put(PREFERRED_NAME,10);
									put(SYNONYM,8);
									}
								};
	
	/* --- Constructors --- */
	public FacetRecommenderService(ITermService its) {
		this.termService = its;
	}
	
	/* --- Methods required by Interface --- */
	@Override
	public List<String> getSortedFacets(List<String> tids) {
		FacetRecommender recommender = new FacetRecommender(tids);
		List<String> flist = new ArrayList<String>();
		
		for (Entry<String,Double> e : recommender.sortFacets()){
			flist.add((String) e.getKey());
		}
		return flist;
	}

	@Override
	public List<String> getSortedFacetsByRange(List<String> tids,
			int start, int end) {
		return getSortedFacets(tids).subList(start, end);
	}

	@Override
	public List<String> getSortedFacetsByQuantity(List<String> tids,
			int quantity) {
		return getSortedFacetsByRange(tids, 0, quantity);
	}
	
	/* --- Recommender Class for easier access --- */
	private class FacetRecommender {
		/**
		 * Map keeps track of the respective weight for each facet.
		 */
		private Map<String, Double> facetWeights = new HashMap<String, Double>();
		
		/* --- Constructors --- */
		public FacetRecommender(List<String> tids) {
			storeTermsAndFacets(tids);
		}

		/* --- Methods --- */
		private void storeTermsAndFacets(List<String> tids) {
			for (String tstring : tids) {
				String tid = tstring.split("_")[0];
				String tsource = tstring.split("_")[1];
				
				IConcept term = termService.getTerm(tid);
				
				setRelatedFacets(term.getFacets(),tsource);
			}
		}
		
		private void setRelatedFacets(List<Facet> facets, String tsource) {
			for (Facet facet : facets) {
				String fid = facet.getId();
				Double weight = calcWeights(tsource);
				
				if (!facetWeights.containsKey(fid)) {
					facetWeights.put(fid, weight);
				} else {
					weight += facetWeights.get(fid);
					facetWeights.put(fid, weight);
				}
			}
		}
		
		private Double calcWeights(String tsource) {
			Double weight = 0.0;
			if (!tsource.startsWith(PARENTAL_LVL_BEGINNING)) {
				weight = WEIGHTS.get(tsource).doubleValue();
			}
			else if (tsource.startsWith(PARENTAL_LVL_BEGINNING)) {
				int plevel = Integer.parseInt(tsource.substring(1));
				weight = 1+(10*Math.exp(-0.2*plevel)); // 1+10e^(-0.2*plevel)
			}
			return weight;
		}
		
		//TODO need additional sorting for equally weighted facets (e.g. name of the facet or id or size)
		@SuppressWarnings({ "unchecked" })
		public List<Entry<String,Double>> sortFacets() {
			List<Entry<String,Double>> flist =
					new ArrayList<Entry<String,Double>>(facetWeights.entrySet());
			Collections.sort(flist , Collections.reverseOrder(new Comparator<Object>() {
				public int compare (Object f1, Object f2)
				{
					Entry<String,Double> e1 = (Entry<String,Double>) f1;
					Entry<String,Double> e2 = (Entry<String,Double>) f2;
					Double first_weight = (Double) e1.getValue();
					Double second_weight = (Double) e2.getValue();
					
					Integer cw = Double.compare(first_weight,second_weight);
					if (cw == 0) {
						String fid_1 = (String) e1.getKey();
						String fid_2 = (String) e2.getKey();
						// String first_name = facetNames.get(fid_1);
						// String second_name = facetNames.get(fid_2);
						return fid_1.compareTo(fid_2);
					}
					return cw;
				}
			}));
			return flist;
		}
	}
}
