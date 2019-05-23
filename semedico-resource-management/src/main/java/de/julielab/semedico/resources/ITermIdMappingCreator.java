package de.julielab.semedico.resources;

public interface ITermIdMappingCreator {

	public void writeIdMapping(String outputFile, String idProperty, String... facetIds);
}
