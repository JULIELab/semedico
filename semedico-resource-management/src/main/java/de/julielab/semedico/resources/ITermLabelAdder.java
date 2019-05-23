package de.julielab.semedico.resources;

public interface ITermLabelAdder {
	void addTermLabels(String termIdFile, String idProperty, String originalSource, String... labels);
}
