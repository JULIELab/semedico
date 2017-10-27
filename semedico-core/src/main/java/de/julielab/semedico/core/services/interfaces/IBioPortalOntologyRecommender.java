package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.semedico.core.facets.UIFacet;

public interface IBioPortalOntologyRecommender {

	List<UIFacet> recommendOntologies(String keywords, List<UIFacet> facetsToSelectFrom);

}
