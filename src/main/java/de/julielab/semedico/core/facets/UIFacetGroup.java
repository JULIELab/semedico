package de.julielab.semedico.core.facets;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UIFacetGroup extends FacetGroup<UIFacet> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7682763376379737081L;


	private List<UIFacetGroupSection> sections;
	private UIFacetGroupSection defaultSection;
	private boolean configurationDone;

	public UIFacetGroup(String name, int position) {
		super(name, position);
		sections = Collections.emptyList();
		configurationDone = true;
	}

	public void addSection(UIFacetGroupSection section) {
		if (sections.size() == 0)
			sections = new ArrayList<>();
		sections.add(section);
		section.setFacetGroup(this);
	}
	
	public void addDefaultSection() {
		if (null == defaultSection) {
			defaultSection = new UIFacetGroupSection(UIFacetGroupSection.DEFAULT_NAME, false);
			defaultSection.addAll(this);
			defaultSection.setShowName(false);
		}
		addSection(defaultSection);
	}

	public List<UIFacetGroupSection> getSections() {
		return sections;
	}

	public boolean isConfigurationDone() {
		return configurationDone;
	}

	public void setConfigurationDone(boolean configurationDone) {
		this.configurationDone = configurationDone;
	}

	public int numSections() {
		// To load the default section if necessary.
		getSections();
		return sections.size();
	}

	public UIFacetGroupSection getSection(int i) {
		return getSections().get(i);
	}

	public List<UIFacet> getFacetsInSections(int maxFacets) {
		List<UIFacet> facets = new ArrayList<>();
		for (UIFacetGroupSection section : getSections()) {
			for (int i = 0; i < section.size() && i < maxFacets; i++) {
				UIFacet uiFacet = section.get(i);
				facets.add(uiFacet);
			}
		}
		return facets;
	}

	public void reset() {
		for (UIFacetGroupSection section : sections)
			section.reset();
	}

}
