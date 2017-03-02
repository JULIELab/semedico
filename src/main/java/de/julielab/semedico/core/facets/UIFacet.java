package de.julielab.semedico.core.facets;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.ImmutablePathWrapper;
import de.julielab.semedico.core.concepts.Path;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.util.DisplayGroup;
import de.julielab.semedico.core.util.LabelFilter;
import de.julielab.semedico.core.util.exceptions.IncompatibleStructureException;

public class UIFacet extends Facet {

	public enum FacetViewMode {
		HIERARCHIC, FLAT
	}

	private Facet originalFacet;

	public Facet getOriginalFacet() {
		return originalFacet;
	}

	// Whether this facetConfiguratino shall not be displayed to the user in
	// form of a FacetBox component.
	private boolean hidden;
	// Whether the FacetBox belonging to this facetConfiguration is collapsed,
	// i.e. only the Facet name but no terms are displayed to the user.
	private boolean collapsed;
	// Whether the FacetBox belonging to this facetConfiguration is supposed to
	// show more terms than is determined by the default value.
	private boolean expanded;
	/**
	 * Indicates if this facet is forced to flat, frequency ordered term counts.
	 * 
	 * @see UIFacet#isForcedToFlatFacetCounts()
	 */
	private boolean forcedToFlatFacetCounts;
	private FacetViewMode viewMode;

	/**
	 * The list of Terms on the path from the root (inclusive) to the currently selected term (inclusive) of this facet.
	 * E.g. Lipids -> Fatty Acids -> Fatty Acids, Unsaturated". In the front end, the children of
	 * "Fatty Acids, Unsaturated" will be displayed with their frequency count. The Terms on the path do not show a
	 * count.
	 */
	private IPath currentPath;
	private Logger logger;
	private DisplayGroup<Label> displayGroup;
	private long totalFacetCount;
	private UIFacetGroupSection facetGroupSection;

	/**
	 * 
	 * @return The facet group section this facet is sorted into.
	 */
	public UIFacetGroupSection getFacetGroupSection() {
		return facetGroupSection;
	}

	/**
	 * Called from the UserInterfaceStateCreator in the front end.
	 * 
	 * @param logger
	 * @param id
	 * @param name
	 * @param searchFieldNames
	 * @param filterFieldName
	 * @param uniqueLabels
	 * @param position
	 * @param cssId
	 * @param source
	 * @param termService
	 */
	public UIFacet(Logger logger, Facet originalFacet) {
		super(originalFacet);
		this.originalFacet = originalFacet;
		this.logger = logger;
		this.viewMode = source != null && source.isHierarchic() ? FacetViewMode.HIERARCHIC : FacetViewMode.FLAT;
		this.forcedToFlatFacetCounts = false;
		this.currentPath = new Path();
		this.hidden = false;
		this.displayGroup = new DisplayGroup<Label>(new LabelFilter(), 10);
	}

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public void switchViewMode() {
		// Only when the facet source is genuinely hierarchical, the mode can be
		// switched. It doesn't make sense for terms without any structural
		// information to be organized hierarchically.
		if (source.isFlat()) {
			logger.warn("Facet \"" + name
					+ "\" with genuinely flat structure was triggered to change to hierarchical display of terms which is not possible.");
			return;
		}
		viewMode = viewMode == FacetViewMode.HIERARCHIC ? FacetViewMode.FLAT : FacetViewMode.HIERARCHIC;
	}

	public void setToHierarchicViewMode() {
		viewMode = FacetViewMode.HIERARCHIC;
	}

	/**
	 * aka flat, perhaps 'list' is a better term here
	 */
	public void setToListViewMode() {
		viewMode = FacetViewMode.FLAT;
	}

	public boolean isInFlatViewMode() {
		return viewMode == FacetViewMode.FLAT;
	}

	public boolean isInHierarchicViewMode() {
		return viewMode == FacetViewMode.HIERARCHIC;
	}

	public FacetViewMode getViewMode() {
		return viewMode;
	}

	/**
	 * <p>
	 * Returns the terms which have been chosen by the user to display in this facet.
	 * </p>
	 * <p>
	 * These terms are determined as follows:
	 * <ul>
	 * <li>If the facet is not drilled down, i.e. the user did not select any term of this facet and did not enter a
	 * search term associated with the facet, the facet root IDs are returned.
	 * <li>If the facet is drilled down, i.e. the user has been viewing successors of a root term, the children of the
	 * last clicked-on term - i.e. the root of the currently viewed subtree - are returned.</code>.
	 * </ul>
	 * </p>
	 * 
	 * @return The children of the currently selected subtree root or <code>null</code> if the facet associated with
	 *         this <code>facetConfiguration</code> is flat.
	 * @throws IncompatibleStructureException
	 *             When this method is invoked on a facet instance which has a flat structure.
	 */
	public Collection<Concept> getRootTermsForCurrentlySelectedSubTree() {
		if (source.isFlat())
			throw new IncompatibleStructureException(
					"This facet if of flat structure and thus no tree roots to return.");
		Collection<Concept> returnTerms = null;

		if (isDrilledDown()) {
			Set<Concept> termSet = new HashSet<>();
			Concept lastPathTerm = this.currentPath.getLastNode();
			for (IConcept child : lastPathTerm.getAllChildrenInFacet(getId())) {
				termSet.add((Concept) child);
			}
			returnTerms = termSet;
		} else {
			returnTerms = getFacetRoots();
		}
		return returnTerms;
	}

	public Collection<String> getRootTermIdsForCurrentlySelectedSubTree(boolean filterNonDbTerms) {
		if (source.isFlat())
			throw new IncompatibleStructureException(
					"This facet if of flat structure and thus no tree roots to return.");
		Collection<String> returnTerms = null;

		if (isDrilledDown()) {
			Concept lastPathTerm = this.currentPath.getLastNode();
			returnTerms = lastPathTerm.getAllChildIdsInFacet(getId(), filterNonDbTerms);
		} else {
			returnTerms = getFacetRootIds();
		}
		return returnTerms;
	}

	/**
	 * This method exists solely for Tapestry to loop over the path elements. Returns a wrapper object for the current
	 * path which is immutable.
	 * 
	 * @return An immutable wrapper for the current path. If there is currently open path, the returned path is just
	 *         empty, but never <tt>null</tt>.
	 */
	public IPath getCurrentPath() {
		return new ImmutablePathWrapper(currentPath);
	}

	public int getCurrentPathLength() {
		return currentPath.length();
	}

	public Concept removeLastNodeOfCurrentPath() {
		forcedToFlatFacetCounts = false;
		return currentPath.removeLastNode();
	}

	/**
	 * Appends <code>term</code> to the current path from a root to the currently selected term for this configuration's
	 * facet.
	 * 
	 * @param term
	 *            The term to append to the current path.
	 */
	public void appendNodeToCurrentPath(Concept term) {
		forcedToFlatFacetCounts = false;
		currentPath.appendNode(term);
	}

	public void clearCurrentPath() {
		forcedToFlatFacetCounts = false;
		try {
			currentPath.clear();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public Concept getNodeOnCurrentPathAt(int i) {
		return currentPath.getNodeAt(i);
	}

	public boolean containsCurrentPathNode(Concept term) {
		return currentPath.containsNode(term);
	}

	/**
	 * Returns the last - i.e. the deepest - element of the current root-to-term path of this
	 * <code>FacetConfiguration</code>.
	 * 
	 * @return The last element on the current root-to-term path or <code>null</code> if the path is empty.
	 */
	public Concept getLastPathElement() {
		if (currentPath.length() > 0)
			return currentPath.getLastNode();
		return null;
	}

	public void setCurrentPath(IPath currentPath) {
		this.currentPath = currentPath;
	}

	public boolean isDrilledDown() {
		return currentPath.length() > 0;
	}

	/**
	 * Completely resets this UIFacet. It will be
	 * <ul>
	 * <li>set to not-collapsed</li>
	 * <li>not-expanded</li>
	 * <li>drilled-to-top (view on roots)</li>
	 * <li>the set of shown terms will be cleared</li>
	 * <li>the view mode will be reset to the natural structure of the underlying facet's source (hierarchical vs.
	 * flat).</li>
	 * </ul>
	 */
	public void reset() {
		hidden = false;
		collapsed = false;
		expanded = false;
		this.viewMode = source.isHierarchic() ? FacetViewMode.HIERARCHIC : FacetViewMode.FLAT;
		clearCurrentPath();
		displayGroup.reset();
	}

	/**
	 * Does nothing.
	 */
	public void refresh() {
		// nothing
	}

	/**
	 * <p>
	 * Returns <code>true</code> if this <code>FacetConfiguration</code> is forced to retrieve facet counts from the top
	 * N counts in its field rather than querying the selected subtree roots.
	 * </p>
	 * <p>
	 * This may happen when there are too many terms to query, e.g. for the roots of the 'Proteins/Genes' facet which
	 * has approx. 25k roots.
	 * </p>
	 * 
	 * @return the forcedToFlatFacetCounts
	 */
	public boolean isForcedToFlatFacetCounts() {
		return forcedToFlatFacetCounts;
	}

	/**
	 * @param forcedToFlatFacetCounts
	 *            the forcedToFlatFacetCounts to set
	 */
	public void setForcedToFlatFacetCounts(boolean forcedToFlatFacetCounts) {
		this.forcedToFlatFacetCounts = forcedToFlatFacetCounts;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "UIFacet '" + name + "' (ID: " + getId() + ")";
	}

	/**
	 * 
	 * @return
	 */
	public DisplayGroup<Label> getLabelDisplayGroup() {
		return displayGroup;
	}

	/**
	 * @return the totalFacetCount
	 */
	public long getTotalFacetCount() {
		return totalFacetCount;
	}

	/**
	 * @param totalFacetCount
	 *            the totalFacetCount to set
	 */
	public void setTotalFacetCount(long totalFacetCount) {
		this.totalFacetCount = totalFacetCount;
	}

	public void incrementTotalFacetCount(long amount) {
		this.totalFacetCount += amount;
	}

	public void setFacetGroupSection(UIFacetGroupSection facetGroupSection) {
		this.facetGroupSection = facetGroupSection;

	}

}
