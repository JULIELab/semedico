package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;

import de.julielab.semedico.core.entities.documents.DocumentDisplayContext;
import de.julielab.semedico.core.entities.documents.DocumentModel;
import de.julielab.semedico.core.util.DocumentModelAccessException;

public interface IDocumentModelService {
	/**
	 * Tries to load the document model matching the given document facetSource (document
	 * type) and display context (e.g. hit list). Document models are stored in YML
	 * format at <code>META-INF/documentmodel/source_context.yml</code> where
	 * <code>facetSource</code> and <code>context</tt> are the lower-cased string
	 * representations of the two parameters.
	 * 
	 * @param source
	 *            The facetSource of the document. This must be an identifier that
	 *            uniquely determines the document structure.
	 * @param context
	 *            The display context that determines which parts of the document to
	 *            show in what way.
	 * @return The document model for the given facetSource and context.
	 * @throws IOException
	 *             If the model file cannot be read.
	 */
	DocumentModel getModel(String source, DocumentDisplayContext context) throws DocumentModelAccessException;
}
