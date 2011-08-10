package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.query.IQueryTranslationService;
import de.julielab.semedico.search.IFacettedSearchService;
import de.julielab.semedico.search.ILabelCacheService;

/**
 * For a particular Facet, holds information about the total hit count of Terms
 * in this facets and which Term in this Facet has been hit how often in a
 * document search. This information in stored in the <code>labels</code> field
 * which stores for each Term how often this Term has been found.
 * 
 * @author faessler
 * 
 */
public class FacetHit {

	// This is here to keep the facet counts of a particular search available.
	// It is a mapping from a term's ID to its label for display.
	private Map<String, Label> labels;

	private Map<String, Map<String, Label>> hLabels;
	private Map<String, List<Label>> fLabels;

	// Total document hits in this facet. Note that this number is not just the
	// number of Labels/Terms in the associated facet: One document has
	// typically numerous terms associated with it.
	private Map<Facet, Long> totalFacetCounts;

	private ILabelCacheService labelCacheService;

	private ITermService termService;

	private SolrServer solr;

	private IQueryTranslationService queryTranslationService;

	private Multimap<String, IFacetTerm> queryTerms;

	private IFacettedSearchService searchService;

	private SearchConfiguration searchConfiguration;

	public FacetHit(ILabelCacheService labelCacheService,
			ITermService termService, SolrServer solr,
			IQueryTranslationService queryTranslationService,
			Multimap<String, IFacetTerm> queryTerms) {
		this.labelCacheService = labelCacheService;
		this.termService = termService;
		this.solr = solr;
		this.queryTranslationService = queryTranslationService;
		this.queryTerms = queryTerms;
		this.labels = new HashMap<String, Label>();
		this.totalFacetCounts = new HashMap<Facet, Long>();
	}

	public FacetHit(Map<String, Label> labels,
			ILabelCacheService labelCacheService,
			IFacettedSearchService searchService) {
		this.labels = labels;
		this.labelCacheService = labelCacheService;
		this.searchService = searchService;
		this.totalFacetCounts = new HashMap<Facet, Long>();

	}

	public void addLabel(String termId, long frequency) {

		// First check, whether we already have added the label for termId. This
		// may happen when a label for a sub term of the term with ID termId is
		// added first.
		Label label = labels.get(termId);
		if (label == null) {
			label = labelCacheService.getCachedLabel(termId);
			labels.put(termId, label);
		}
		label.setHits(frequency);
		// Mark the parent term as having a sub term hit. If we
		// don't already have met the parent term, we just
		// create it now and set its hits later when the loop
		// comes to it.
		// We can do that because whenever a term has been hit, all his parents
		// must also have been hit (IS-A relation).
		for (IFacetTerm parentTerm : label.getTerm().getAllParents()) {
			Label parentLabel = labels.get(parentTerm.getId());
			if (parentLabel == null) {
				parentLabel = labelCacheService.getCachedLabel(parentTerm
						.getId());
				labels.put(parentTerm.getId(), parentLabel);
			}
			parentLabel.setHasChildHits();
		}
	}

	/**
	 * @param facet
	 * @return
	 */
	public List<Label> getHitFacetRoots(Facet facet) {
		Collection<IFacetTerm> roots = termService.getFacetRoots(facet);
		Iterator<IFacetTerm> rootIt = roots.iterator();
		List<Label> retLabels = new ArrayList<Label>();
		List<String> idList = new ArrayList<String>();
		while (rootIt.hasNext()) {
			IFacetTerm root = rootIt.next();
			idList.add(root.getId());
		}
		String strQ = queryTranslationService.createQueryFromTerms(queryTerms);
		SolrQuery q = new SolrQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);
		for (String id : idList) {
			q.add("facet.query", "facetTerms:" + id);
		}
		try {
			QueryResponse queryResponse = solr.query(q);
			System.out.println("Time elapsed: "
					+ queryResponse.getElapsedTime());
			for (String id : queryResponse.getFacetQuery().keySet()) {
				String termId = id.split(":")[1];
				Integer count = queryResponse.getFacetQuery().get(id);
				if (count == null)
					count = 0;
				Label label = labelCacheService.getCachedLabel(termId);
				label.setHits(new Long(count));
				retLabels.add(label);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Amount of labels: " + retLabels.size());
		return retLabels;
	}

	/**
	 * @param id
	 * @return
	 */
	public List<Label> getHitChildren(String childId) {
		IFacetTerm term = termService.getNode(childId);
		Iterator<IFacetTerm> childIt = term.childIterator();
		List<Label> retLabels = new ArrayList<Label>();
		List<String> idList = new ArrayList<String>();
		while (childIt.hasNext()) {
			IFacetTerm root = childIt.next();
			idList.add(root.getId());
		}
		String strQ = queryTranslationService.createQueryFromTerms(queryTerms);
		SolrQuery q = new SolrQuery("*:*");
		q.setFilterQueries(strQ);
		q.setFacet(true);
		q.setRows(0);
		for (String id : idList) {
			q.add("facet.query", "facetTerms:" + id);
		}
		try {
			QueryResponse queryResponse = solr.query(q);
			System.out.println("Time elapsed: "
					+ queryResponse.getElapsedTime());
			for (String id : queryResponse.getFacetQuery().keySet()) {
				String termId = id.split(":")[1];
				Integer count = queryResponse.getFacetQuery().get(id);
				if (count == null)
					count = 0;
				Label label = labelCacheService.getCachedLabel(termId);
				label.setHits(new Long(count));
				retLabels.add(label);
			}
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Amount of labels: " + retLabels.size());
		return retLabels;
	}

	public void setTotalFacetCount(Facet facet, long totalHits) {
		this.totalFacetCounts.put(facet, totalHits);
	}

	public long getTotalFacetCount(Facet facet) {
		Long count = totalFacetCounts.get(facet);
		return count == null ? 0 : count;
	}

	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		for (Facet facet : totalFacetCounts.keySet()) {
			b.append(String
					.format("Facet: %s. Total number of document hits for this facet: %d",
							facet.getName(), totalFacetCounts.get(facet)));
			b.append("\n");
		}
		return b.toString();
	}

	/**
	 * 
	 */
	public void clear() {
		labelCacheService.releaseHierarchy(labels.values());
		labels.clear();
	}

	/**
	 * @return the hitFacetTermLabels
	 */
	public Map<String, Label> getHitFacetTermLabels() {
		return labels;
	}

	/**
	 * @param allIds
	 * 
	 */
	public void updateLabels(List<String> allIds) {
		List<String> newIds = new ArrayList<String>();
		for (String id : allIds)
			if (!labels.containsKey(id))
				newIds.add(id);
		// TODO only if there are new ids
		labels.putAll(searchService.getFacetCountsForTermIds(newIds));
	}

	/**
	 * @param selectedFacetGroup
	 */
	public void getLabelsForFacetGroup(FacetGroup selectedFacetGroup) {
		// TODO Auto-generated method stub

	}
}
