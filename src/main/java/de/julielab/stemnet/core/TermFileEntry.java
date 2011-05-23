package de.julielab.stemnet.core;

import java.util.ArrayList;
import java.util.List;

public class TermFileEntry {
	private String id;
	private String parentId;
	private TermFileEntry parent;
	private String canonic;
	private String type;
	private List<String> synonyms;
	private List<List<String>> variations;
	private List<TermFileEntry> children;
	private Facet facet;
	private String description;
	private String shortDescription;
	public TermFileEntry() {
		super();
		children = new ArrayList<TermFileEntry>();
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public TermFileEntry getParent() {
		return parent;
	}
	public void setParent(TermFileEntry parent) {
		this.parent = parent;
	}
	public String getCanonic() {
		return canonic;
	}
	public void setCanonic(String canonic) {
		this.canonic = canonic;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public List<String> getSynonyms() {
		return synonyms;
	}
	public void setSynonyms(List<String> synonyms) {
		this.synonyms = synonyms;
	}
	public List<List<String>> getVariations() {
		return variations;
	}
	public void setVariations(List<List<String>> variations) {
		this.variations = variations;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public List<TermFileEntry> getChildren() {
		return children;
	}
	public void setChildren(List<TermFileEntry> children) {
		this.children = children;
	}
	@Override
	public String toString() {
	
		return id;
	}

	public Facet getFacet() {
		return facet;
	}

	public void setFacet(Facet facet) {
		this.facet = facet;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String organism) {
		this.description = organism;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String description) {
		this.shortDescription = description;
	}
}
