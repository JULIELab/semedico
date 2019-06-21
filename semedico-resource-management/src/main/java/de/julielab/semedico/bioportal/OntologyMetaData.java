package de.julielab.semedico.bioportal;

import com.google.gson.annotations.SerializedName;
import de.julielab.neo4j.plugins.datarepresentation.JsonSerializer;

import java.util.ArrayList;
import java.util.List;

public class OntologyMetaData {
	class OntologySubmission {

	}

	@SerializedName("@id")
	public String id;
	public String acronym;
	public String name;
	public List<String> group;
	/**
	 * Not actually sent via the BioPortal API. Instead only {@link #group} is sent, an array of URIs pointing to groups
	 * this ontology belongs to. The OntologyGroup objects are then requested via these URIs and may be added here for
	 * storage. To add ontology groups, please refer to {@link #addOntologyGroup(OntologyGroup)}.
	 */
	public List<OntologyGroup> ontologyGroups;
	/**
	 * Not actually sent via the BioPortal API. Instead ontology metrics (number of classes, depth of ontology etc) have
	 * to be requested separately. Then, the correct metric may be added here.
	 */
	public OntologyMetric ontologyMetric;
//	static volatile Gson gson = new Gson();

	public OntologyMetaData(String name, String acronym) {
		this.name = name;
		this.acronym = acronym;
	}

	public void addOntologyGroup(OntologyGroup ontologyGroup) {
		if (null == ontologyGroups)
			ontologyGroups = new ArrayList<>();
		ontologyGroups.add(ontologyGroup);
	}


	@Override
	public String toString() {
//		return gson.toJson(this);
		return JsonSerializer.toJson(this);
	}

	public String apiUrl() {
		return id;
	}

	public String bioportalPurl() {
		return "http://purl.bioontology.org/ontology/" + acronym;
	}

}
