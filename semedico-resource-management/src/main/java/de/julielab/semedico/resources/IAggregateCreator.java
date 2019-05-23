package de.julielab.semedico.resources;

import java.util.Set;

public interface IAggregateCreator {
	void createAggregates(Set<String> allowedMappingTypes, String termLabel, String aggregatedTermsLabel);
}
