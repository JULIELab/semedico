package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

public class BTermQueryPanel {
	
	@Parameter
	@Property
	private IFacetTerm selectedBTerm;
	
	public String getBTermCSSClass() {
		if (selectedBTerm != null) {
			Facet facet = selectedBTerm.getFirstFacet();
			String cssId = facet.getCssId();
			String termClass = cssId + " querybterm primaryFacetStyle";
			return termClass;
		} else
			return null;
	}
}