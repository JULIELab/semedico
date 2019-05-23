package de.julielab.semedico.resources;

/**
 * Just a convenience service to call the 'updateChildrenInformation' endpoint in the Neo4j TermManager plugin. This
 * call creates a new property in all nodes storing all facet IDs a term node has children in. This information is used
 * in Semedico to be able to tell whether there are children of a term or not.
 * 
 * @author faessler
 * 
 */
public interface ITermChildrenUpdater {
	void updateChildrenInformation();
}
