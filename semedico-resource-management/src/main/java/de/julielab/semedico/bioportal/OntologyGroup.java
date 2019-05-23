package de.julielab.semedico.bioportal;

import java.util.List;

public class OntologyGroup {
	public static final OntologyGroup UNKNOWN_GROUP = new OntologyGroup("Unknown", "Unknown", null, null, null);

	public OntologyGroup(String acronym, String name, String description, String created, List<String> ontologies) {
		this.name = name;
		this.acronym = acronym;
		this.description = description;
		this.created = created;
		this.ontologies = ontologies;
	}

	public String acronym;
	public String name;
	public String description;
	public String created;
	public List<String> ontologies;
}
