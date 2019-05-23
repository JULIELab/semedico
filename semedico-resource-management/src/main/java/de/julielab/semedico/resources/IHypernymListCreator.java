package de.julielab.semedico.resources;

public interface IHypernymListCreator {
	void writeHypernymList(String outputFilePath, String termLabel, String... facetLabels);
}
