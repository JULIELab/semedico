package de.julielab.semedico.search;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacettedSearchResult;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.SortCriterium;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;

public interface IFacettedSearchService {

	/**
	 * Returns a {@link FacettedSearchResult} which consists of the
	 * <code>documentHits</code>, <code>facetHits</code> (facet- and
	 * term-hit-counts), the ID-BitSet of retrieved documents, Lucene ScoreDocs
	 * (DocId and Score) and the total number of document hits.
	 */
	public FacettedSearchResult search(Multimap<String, IFacetTerm> query,
			String rawQuery, SortCriterium sortCriterium,
			boolean reviewsFiltered) throws IOException;

	public Collection<DocumentHit> constructDocumentPage(int start);

	/**
	 * @param displayedTermIds
	 * @param labels 
	 * @return
	 */
	public void getFacetCountsForHierarchicFacets(Multimap<Facet, String> displayedTermIds, FacetHit facetHit);

	/**
	 * @param facets
	 * @return
	 */
	public void getFacetCountsForFlatFacets(List<Facet> facets, FacetHit facetHit);

}
