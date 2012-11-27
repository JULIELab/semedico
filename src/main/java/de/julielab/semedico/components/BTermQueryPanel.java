package de.julielab.semedico.components;

import java.util.Collection;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

public class BTermQueryPanel {
	
	@Parameter
	@Property
	private IFacetTerm selectedBTerm;
	
	@SessionState
	@Property
	private SearchState searchState;
	
	@Property
	private IFacetTerm termLoopItem;
	
	public String getBTermCSSClass() {
		if (selectedBTerm != null) {
			Facet facet = selectedBTerm.getFirstFacet();
			String cssId = facet.getCssId();
			String termClass = cssId + " querybterm primaryFacetStyle";
			return termClass;
		} else
			return null;
	}
	
	public String getSearchNodeTermCSSClass() {
		if (selectedBTerm != null) {
			Facet facet = termLoopItem.getFirstFacet();
			String cssId = facet.getCssId();
			String termClass = cssId + " filterBox primaryFacetStyle";
			return termClass;
		} else
			return null;
	}
	
	public Collection<IFacetTerm> getSearchNodeTerms(int index) {
		Multimap<String, IFacetTerm> searchNode = searchState.getSearchNode(index);
		return searchNode.values();
	}
}