package de.julielab.semedico.core.entities.documents;

import de.julielab.elastic.query.components.data.ISearchServerDocument;

/**
 * This class represents documents at a very abstract level. This means that
 * this class does not have variables for specific strings or data but just
 * consists of a {@link DocumentModel} and the actual data given by a
 * {@link ISearchServerDocument}. It is important to realize that the model and
 * the data must match in order to produce a coherent, usable document. If they
 * do, the document can answer queries about which value (from the search server
 * document) it has for which of its elements, e.g. authors (from its model).
 * The frontend can iterate through the elements of the model, get the
 * respective field names and other information and retrieve the value for each
 * element from the search server document under the received field name.
 * 
 * @author faessler
 *
 */
public class Document {
	private DocumentModel model;
	private ISearchServerDocument searchServerDocument;
}
