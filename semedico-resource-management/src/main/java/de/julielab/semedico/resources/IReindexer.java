package de.julielab.semedico.resources;

public interface IReindexer {
	void reindex(String sourceIndex, String targetIndex) throws Exception;
}
