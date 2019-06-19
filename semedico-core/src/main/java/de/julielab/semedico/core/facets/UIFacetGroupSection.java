package de.julielab.semedico.core.facets;

import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.entities.state.SearchState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UIFacetGroupSection extends ArrayList<UIFacet> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8632480401135457336L;
	public static final String DEFAULT_NAME = "defaultSection";
	private String name;
	private boolean showName;
	private IFacetDeterminer facetDeterminer;
	private String description;
	private boolean determineFacetsAfterReset;
	private boolean facetsHaveBeenDetermined;
	/**
	 * The facet group this section belongs to.
	 */
	private UIFacetGroup facetGroup;

	public UIFacetGroupSection(String name, boolean determineFacetsAfterReset) {
		this.name = name;
		this.determineFacetsAfterReset = determineFacetsAfterReset;
		this.setShowName(true);
		this.facetsHaveBeenDetermined = false;
	}

	public String getName() {
		return name;
	}

	public boolean showName() {
		return showName;
	}

	public String getDescription() {
		return description;
	}

	public void setShowName(boolean showName) {
		this.showName = showName;
	}

	public boolean determineFacetsToDisplay(SearchState searchState, AbstractUserInterfaceState uiState) {
		if (null == facetDeterminer || facetsHaveBeenDetermined) {
			return false;
		}
		List<UIFacet> determinedFacets = facetDeterminer.determineFacets(searchState, uiState);
		this.clear();
		this.addAll(determinedFacets);
		facetsHaveBeenDetermined = true;
		return true;
	}
	
	public void setDescription(String description) {
		this.description = description;

	}

	public IFacetDeterminer getFacetDeterminer() {
		return facetDeterminer;
	}

	public void setFacetDeterminer(IFacetDeterminer facetDeterminer) {
		this.facetDeterminer = facetDeterminer;
	}

	public void moveFacet(int fromIndex, int toIndex) {
		if (fromIndex < 0 || toIndex < 0)
			return;
		UIFacet movedFacet = get(fromIndex);
		if (fromIndex > toIndex) {
			// Dragging up
			for (int i = fromIndex - 1; i >= toIndex; i--) {
				UIFacet shiftedFacet = get(i);
				set(i + 1, shiftedFacet);
			}
		} else {
			// Dragging down
			for (int i = fromIndex + 1; i <= toIndex; i++) {
				UIFacet shiftedFacet = get(i);
				set(i - 1, shiftedFacet);
			}
		}
		set(toIndex, movedFacet);

	}

	
	
	@Override
	public boolean add(UIFacet e) {
		e.setFacetGroupSection(this);
		return super.add(e);
	}

	@Override
	public void add(int index, UIFacet element) {
		element.setFacetGroupSection(this);
		super.add(index, element);
	}

	@Override
	public boolean addAll(Collection<? extends UIFacet> c) {
		for (UIFacet f : c)
			f.setFacetGroupSection(this);
		return super.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends UIFacet> c) {
		for (UIFacet f : c)
			f.setFacetGroupSection(this);
		return super.addAll(index, c);
	}

	public void reset() {
		for (UIFacet facet : this)
			facet.reset();
		if (determineFacetsAfterReset) {
			clear();
			facetsHaveBeenDetermined = false;
		}
	}

	public void setFacetGroup(UIFacetGroup facetGroup) {
		this.facetGroup = facetGroup;
	}

	/**
	 * 
	 * @return The facet group this facet group section belongs to.
	 */
	public UIFacetGroup getFacetGroup() {
		return facetGroup;
	}

}