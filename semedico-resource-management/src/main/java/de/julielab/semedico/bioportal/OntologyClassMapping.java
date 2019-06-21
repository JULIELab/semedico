package de.julielab.semedico.bioportal;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OntologyClassMapping {
	public class Process {
		public String comment;
		public String creator;
		public String date;
		public String id;
		public String name;
		public String relation;
		public String source;
		public String source_contact_info;
		public String source_name;
	}

	public Process process;
	public String source;
	public List<MappedClass> classes;
	@SerializedName("@id")
	public String id;

	private static volatile Gson gson = new Gson();

	@Override
	public String toString() {
		return gson.toJson(this);
	}

}
