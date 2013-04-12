package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;

import de.julielab.semedico.bterms.TermSetStatistics;
import de.julielab.semedico.bterms.TermStatistics;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.exceptions.EmptySearchComplementException;
import de.julielab.semedico.search.interfaces.ILabelCacheService;
import de.julielab.util.TripleStream;
import de.julielab.util.math.HarmonicMean;

public class IndirectLinksDeterminationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IndirectLinksDetermination {
	}

	private final ILabelCacheService labelCacheService;
	private final Logger log;

	public IndirectLinksDeterminationComponent(Logger log,
			ILabelCacheService labelCacheService) {
		this.log = log;
		this.labelCacheService = labelCacheService;

	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		if (null == searchCarrier.searchResult
				|| null == searchCarrier.searchResult.searchNodeTermCounts)
			throw new IllegalArgumentException(
					"An instance of "
							+ SemedicoSearchResult.class.getName()
							+ " with non-null search node term counts were expected but not passed.");
		List<TripleStream<String, Integer, Integer>> termLists = searchCarrier.searchResult.searchNodeTermCounts;

		long totalNumDocs = searchCarrier.searchResult.totalNumDocs;
		List<Label> indirectLinkLabels = calculateIntersection(termLists,
				totalNumDocs);

		Collections.sort(indirectLinkLabels);

		log.debug("Retrieved {} intersecting terms as indirect links.",
				indirectLinkLabels.size());

		searchCarrier.searchResult.indirectLinkLabels = indirectLinkLabels;

		return false;
	}

	/**
	 * @param termLists
	 * @param totalNumDocs
	 * @return
	 * @throws EmptySearchComplementException
	 */
	private List<Label> calculateIntersection(
			List<TripleStream<String, Integer, Integer>> termLists,
			long totalNumDocs) throws EmptySearchComplementException {
		List<Label> ret = new ArrayList<Label>();

		for (int i = 0; i < termLists.size(); i++) {
			TripleStream<String, Integer, Integer> termList = termLists.get(i);
			// Do a first increment to set the streams to their first element.
			if (!termList.incrementTuple()) {
				throw new EmptySearchComplementException(
						"A search node is empty after removing the intersection with all other search nodes.");
			}
		}

		// Now, the actual Intersection is computed.
		String lastIntersectionTerm = null;
		boolean reachedEndOfAList = false;
		HarmonicMean hm = new HarmonicMean();
		TermSetStatistics termSetStats = new TermSetStatistics();
		termSetStats.setNumDocs(totalNumDocs);
		while (!reachedEndOfAList) {
			String potentialBTerm = termLists.get(0).getLeft();
			boolean notEqual = false;
			int leastTermListIndex = 0;
			// Check for two things here. First: Are all current elements equal?
			// Then we have an element of the intersection. Second: Determine
			// the index of the stream with the least element. This stream will
			// be incremented if not all elements were equal.
			for (int i = 1; i < termLists.size(); i++) {
				String term = termLists.get(i).getLeft();
				String leastTerm = termLists.get(leastTermListIndex).getLeft();
				if (!term.equals(potentialBTerm))
					notEqual = true;
				if (term.compareTo(leastTerm) < 0)
					leastTermListIndex = i;
			}
			// No intersection element. Increment the stream with the least
			// element and continue to check again, whether we have now an
			// element for the intersection.
			if (notEqual) {
				if (!termLists.get(leastTermListIndex).incrementTuple())
					reachedEndOfAList = true;
				continue;
			} else if (null == lastIntersectionTerm
					|| !potentialBTerm.equals(lastIntersectionTerm)) {
				// ...else: We found an intersection element. Combine the
				// statistics
				// of the single elements since in the intersection, there will
				// be
				// only one element.
				Label label = labelCacheService.getCachedLabel(potentialBTerm);
				label.setRankScoreStatistic(Label.RankMeasureStatistic.BAYESIAN_TCIDF_AVG);
				TermStatistics stats = label.getStatistics();
				stats.setTermSetStats(termSetStats);
				for (int i = 0; i < termLists.size(); i++) {
					// facet count
					double fc = termLists.get(i).getMiddle();
					hm.add(fc);
				}
				stats.setFc(hm.value());
				// The document frequency should be the same for all streams in
				// their current position.
				stats.setDf(termLists.get(0).getRight());
				termSetStats.add(stats);
				hm.reset();
				label.setStatistics(stats);
				ret.add(label);

				lastIntersectionTerm = potentialBTerm;
			}

			// Set the cursors of all lists to the next element as currently all
			// elements are equal anyway (or we have a double element, i.e.
			// lastIntersectionTerm and potentialBTerm are equal).
			for (int i = 0; i < termLists.size(); i++) {
				if (!termLists.get(i).incrementTuple())
					reachedEndOfAList = true;
			}
		}
		termSetStats.normalizeBaTcIdfStatistic();
		return ret;
	}

}
