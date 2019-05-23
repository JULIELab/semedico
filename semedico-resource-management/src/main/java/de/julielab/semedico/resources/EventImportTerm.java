package de.julielab.semedico.resources;

import java.util.List;

import com.google.gson.annotations.SerializedName;

import de.julielab.neo4j.plugins.constants.semedico.ConceptConstants;
import de.julielab.neo4j.plugins.datarepresentation.ConceptCoordinates;
import de.julielab.neo4j.plugins.datarepresentation.ImportConcept;

public class EventImportTerm extends ImportConcept {

	@SerializedName(ConceptConstants.PROP_EVENT_VALENCE)
	public List<Integer> eventValence;
	@SerializedName(ConceptConstants.PROP_SPECIFIC_EVENT_TYPE)
	public String specificEventType;

	public EventImportTerm(ConceptCoordinates coord) {
		super(coord);
	}

}
