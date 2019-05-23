package de.julielab.semedico.resources;

public interface ILingpipeTermDictionaryCreator {
	void writeLingpipeDictionary(String outputFilePath, String label, String[] excludeLabels, String[] properties);

	void writeLingpipeDictionary(String outputFilePath, String label);
}
