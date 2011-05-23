/** 
 * LabelHitCounterService.java
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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;

import com.google.common.collect.SortedSetMultimap;
import com.google.common.collect.TreeMultimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;

public class FacetHitCollectorService implements IFacetHitCollectorService {
	// private static final int MAX_COUNTED_DOCS = 300000;
	// private FacetTerm[][][] termTable;
	// private static Logger logger = Logger
	// .getLogger(FacetHitCollectorService.class);
	// private Map<Facet, int[]> hitTables;
	// private Map<Facet, OpenBitSet> hitBitVectors;
	// private Map<Facet, OpenBitSet> subTermHitVectors;
	// private OpenBitSet facetHits;
	// private int[] totalFacetHits;
	private ILabelCacheService labelCacheService;
	private ITermService termService;
	private IFacetService facetService;

	private List<FacetField> facetFields;

	// private class InvertedIntegerComparator implements Comparator<Integer> {
	//
	// public int compare(Integer integer1, Integer integer2) {
	//
	// return integer2 - integer1;
	// }
	// }

	// public FacetHitCollectorService(ITermService termService, IFacetService
	// facetService) {
	// super();
	// this.termService = termService;
	// this.facetService = facetService;
	// try {
	// this.termTable = table.getTable();
	// hitTables = new HashMap<Facet, int[]>();
	// hitBitVectors = new HashMap<Facet, OpenBitSet>();
	// subTermHitVectors = new HashMap<Facet, OpenBitSet>();
	// this.termService = termService;
	// Collection<Facet> facets = termService.getFacetService()
	// .getFacets();
	// for (Facet facet : facets) {
	// int size = termService.getTermsForFacet(facet).size();
	// hitTables.put(facet, new int[size]);
	// hitBitVectors.put(facet, new OpenBitSet(size));
	// subTermHitVectors.put(facet, new OpenBitSet(size));
	// }
	//
	// facetHits = new OpenBitSet(facets.size());
	// totalFacetHits = new int[facets.size()];
	//
	// } catch (Exception e) {
	// throw new IllegalStateException(e);
	// }

	// }

	// protected void countHitsInDocument(
	// Map<Facet, FacetConfiguration> facetConfigurations, int docId) {
	// Set<Facet> hittenFacets = new HashSet<Facet>();
	//
	// for (Facet facet : facetConfigurations.keySet()) {
	// // Terms of this facet which occur in this document
	// FacetTerm[] facetTerms = termTable[docId][facet.getIndex()];
	//
	// for (FacetTerm term : facetTerms) {
	// FacetConfiguration facetConfiguration = facetConfigurations
	// .get(facet);
	// hittenFacets.add(facet);
	//
	// if (facetConfiguration.isHierarchicMode()) {
	// List<FacetTerm> path = facetConfiguration.getCurrentPath();
	// if (path.size() > 0) {
	// FacetTerm lastPathEntry = path.get(path.size() - 1);
	//
	// // As we only show counts for the children of the
	// // currently selected term, roots get never counted.
	// if (term.getParent() == null)
	// continue;
	//
	// if (lastPathEntry.getSubTerms().size() > 0) {
	//
	// subTermHitVectors.get(facet).set(
	// term.getParent().getFacetIndex());
	//
	// // Similar to not counting roots we also don't count
	// // the nodes on the current Term-path but only for
	// // the children of the currently selected Term.
	// if (!term.getParent().equals(lastPathEntry))
	// continue;
	// }
	// } else {
	// if (term.getParent() != null) {
	// if (term.getParent().getParent() == null)
	// subTermHitVectors.get(facet).set(
	// term.getParent().getFacetIndex());
	//
	// continue;
	// }
	// }
	// } else if (!term.getSubTerms().isEmpty())
	// continue;
	//
	// // Term hit counter.
	// hitTables.get(facet)[term.getFacetIndex()]++;
	// // Term hit indicator.
	// hitBitVectors.get(facet).set(term.getFacetIndex());
	// // Facet hit indicator.
	// facetHits.set(facet.getIndex());
	// }
	// }
	// // Facet hit counter.
	// for (Facet hittenFacet : hittenFacets)
	// totalFacetHits[hittenFacet.getIndex()]++;
	// }
	//
	// protected List<FacetHit> collectResults(Collection<Facet> facets) {
	// Map<Integer, Facet> facetsByIndex = new HashMap<Integer, Facet>();
	// List<FacetHit> resultFacetHits = new ArrayList<FacetHit>();
	// for (Facet facet : facets)
	// facetsByIndex.put(facet.getIndex(), facet);
	//
	// SortedSetMultimap<Integer, Integer> sortedHits = new
	// TreeMultimap<Integer, Integer>(
	// new InvertedIntegerComparator(), null);
	//
	// int nextFacetIndex = facetHits.nextSetBit(0);
	// // For each Facet which has document hits...
	// while (nextFacetIndex != -1) {
	// Facet facet = facetsByIndex.get(nextFacetIndex);
	// List<FacetTerm> terms = termService.getTermsForFacet(facet);
	// int[] hitTable = hitTables.get(facet);
	// OpenBitSet labelHits = hitBitVectors.get(facet);
	// int nextTermIndex = labelHits.nextSetBit(0);
	//
	// FacetHit facetHit = new FacetHit(facet);
	// resultFacetHits.add(facetHit);
	// facetHit.setTotalHits(totalFacetHits[facet.getIndex()]);
	//
	// // ...store for each Term its hit frequency and sort descending...
	// while (nextTermIndex != -1) {
	// sortedHits.put(hitTable[nextTermIndex], nextTermIndex);
	//
	// hitTable[nextTermIndex] = 0;
	// labelHits.clear(nextTermIndex);
	// nextTermIndex = labelHits.nextSetBit(nextTermIndex + 1);
	// }
	//
	// int labelCount = 0;
	// Iterator<Integer> iterator = sortedHits.keySet().iterator();
	// OpenBitSet subTermHits = subTermHitVectors.get(facet);
	//
	// // ...and add the 50 most frequent hit Term labels to the result.
	// while (labelCount < 50 && iterator.hasNext()) {
	// Integer count = iterator.next();
	// Collection<Integer> termIndexes = sortedHits.get(count);
	// for (Integer termIndex : termIndexes) {
	// FacetTerm term = terms.get(termIndex);
	// Label label = labelCacheService.getCachedLabel();
	// label.setTerm(term);
	// label.setHits(count);
	// label.setHasChildHits(subTermHits.get(term.getFacetIndex()));
	// facetHit.getLabels().add(label);
	// labelCount++;
	// if (labelCount == 50)
	// break;
	// }
	// }
	// // Clean up and proceed to the next facet.
	// subTermHits.clear(0, subTermHits.capacity());
	// sortedHits.clear();
	// facetHits.clear(nextFacetIndex);
	// totalFacetHits[nextFacetIndex] = 0;
	// nextFacetIndex = facetHits.nextSetBit(nextFacetIndex + 1);
	// }
	//
	// return resultFacetHits;
	// }
	//
	// public FacetTerm[][][] getTermTable() {
	// return termTable;
	// }
	//
	// public void setTermTable(FacetTerm[][][] termTable) {
	// this.termTable = termTable;
	// }
	//
	// protected int countHitsInDocuments(
	// Map<Facet, FacetConfiguration> facetConfigurations,
	// OpenBitSet documents) {
	// Integer docId = documents.nextSetBit(0);
	// Integer counter = 0;
	// long topDocsSize = documents.cardinality();
	// long time = System.currentTimeMillis();
	// for (int i = 0; i < MAX_COUNTED_DOCS - topDocsSize && docId != -1; i++) {
	// countHitsInDocument(facetConfigurations, docId);
	// counter++;
	// docId = documents.nextSetBit(docId + 1);
	// }
	//
	// time = System.currentTimeMillis() - time;
	// logger.info("counting takes " + time + " milliseconds");
	//
	// return counter;
	// }

	@Override
	public List<FacetHit> collectFacetHits(
			Collection<FacetConfiguration> facetConfigurations,
			List<FacetField> facetFields) {
		this.facetFields = facetFields;
		return collectFacetHits(facetConfigurations);
	}

	public List<FacetHit> collectFacetHits(
			Collection<FacetConfiguration> facetConfigurations) {
		List<FacetHit> facetHits = new ArrayList<FacetHit>(
				facetConfigurations.size());

		for (FacetConfiguration facetConf : facetConfigurations) {
			Facet facet = facetConf.getFacet();
			FacetHit facetHit = new FacetHit(facet);
			facetHit.setTotalHits(1);
			List<FacetTerm> terms = termService.getTermsForFacet(facet);
			for (FacetTerm child : terms) {
				Label label = labelCacheService.getCachedLabel();
				if (child.getParent() != null)
					continue;
				label.setTerm(child);
				label.setHits(1);
				label.setHasChildHits(child.getSubTerms().size() > 0);
				facetHit.getLabels().add(label);
			}

			// List<FacetTerm> termPath = facetConf.getCurrentPath();
			// if (!termPath.isEmpty()) {
			// FacetTerm lastPathTerm = termPath.get(termPath.size() - 1);
			//
			// for (FacetTerm child : lastPathTerm.getSubTerms()) {
			// Label label = labelCacheService.getCachedLabel();
			// label.setTerm(child);
			// label.setHits(1);
			// label.setHasChildHits(child.getSubTerms().size() > 0);
			// facetHit.getLabels().add(label);
			// }
			// }

			facetHits.add(facetHit);
		}

		return facetHits;
		// try {
		// List<FacetHit> facetHits = new ArrayList<FacetHit>(facetService
		// .getFacets().size());
		//
		// for (FacetField field : facetFields) {
		// String facetFieldName = field.getName();
		// // TODO "facet_" is kind of a "magic string"...
		// Facet facet =
		// facetService.getFacetWithName(StringUtils.substringAfter(facetFieldName,
		// "facet_"));
		//
		// FacetHit facetHit = new FacetHit(facet);
		//
		// // TODO double iteration of the top facet terms: one to mark subterm
		// // hits, one for the actual creation of Labels. Could this be
		// // avoided anyhow?
		// HashSet<FacetTerm> subTermHits = new HashSet<FacetTerm>();
		// for (Count termCount : field.getValues()) {
		// FacetTerm term = termService.getTermWithInternalIdentifier(
		// termCount.getName(), facet);
		// FacetTerm parentTerm = term.getParent();
		// if (parentTerm != null)
		// subTermHits.add(parentTerm);
		// }
		//
		// for (Count termCount : field.getValues()) {
		// FacetTerm term = termService.getTermWithInternalIdentifier(
		// termCount.getName(), facet);
		// Label label = labelCacheService.getCachedLabel();
		// label.setTerm(term);
		// label.setHits((int) termCount.getCount());
		// label.setHasChildHits(subTermHits.contains(term));
		// facetHit.getLabels().add(label);
		// }
		// facetHits.add(facetHit);
		// }
		//
		// return facetHits;
		// } catch (SQLException e) {
		// e.printStackTrace();
		// }
		// return null;
	}

	// @Override
	// public List<FacetHit> collectFacetHits(
	// Collection<FacetConfiguration> configurations, int docId) {
	//
	// OpenBitSet documents = new OpenBitSet();
	// documents.set(docId);
	// return collectFacetHits(configurations, documents);
	// }

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public ILabelCacheService getLabelCacheService() {
		return labelCacheService;
	}

	public void setLabelCacheService(ILabelCacheService labelCacheService) {
		this.labelCacheService = labelCacheService;
	}

	// TODO not needed
	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}
}
