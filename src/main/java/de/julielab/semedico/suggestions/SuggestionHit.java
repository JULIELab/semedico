package de.julielab.semedico.suggestions;


public class SuggestionHit {
	private String name;
	private String identifier;
	private String index;
	private String shortDescription;
	public SuggestionHit(String name) {
		super();
		this.name = name;
	}
	
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}


	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}


	public String getIndex() {
		return index;
	}


	public void setIndex(String indexes) {
		this.index = indexes;
	}


	public String getShortDescription() {
		return shortDescription;
	}


	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
}
