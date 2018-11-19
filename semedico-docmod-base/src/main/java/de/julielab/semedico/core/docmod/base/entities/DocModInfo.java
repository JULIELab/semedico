package de.julielab.semedico.core.docmod.base.entities;

import de.julielab.semedico.core.docmod.base.services.DocModBaseModule;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class exhibits the information about a document module ("docmod").
 * This is the name of the document type or corpus (e.g. PubMed, Wikipedia). The idea is that the different
 * document types may have differing structures of their documents. Thus, they consist of different parts like
 * title, abstract, introduction, table captions, sections, diagnosis, ending remarks and more. Which exact
 * parts are found in the documents of a document module is exhibited by the {@link #documentParts} list of this class.
 * An instance of this class must be contributed to the {@link DocModBaseModule#buildDocModInformationService(List)}     method
 * for each document module. This information is used to display the available corpora / document types and their
 * searchable structures on the website.
 */
public class DocModInfo {
    private String documentTypeName;
    private Map<String, DocumentPart> documentParts;

    public DocModInfo(String documentTypeName, List<DocumentPart> documentParts) {
        this.documentTypeName = documentTypeName;
        this.documentParts = documentParts.stream().collect(Collectors.toMap(DocumentPart::getDocPartName, Function.identity()));
    }

    public String getDocumentTypeName() {
        return documentTypeName;
    }

    public void setDocumentTypeName(String documentTypeName) {
        this.documentTypeName = documentTypeName;
    }

    public Map<String, DocumentPart> getDocumentParts() {
        return documentParts;
    }

    public void setDocumentParts(Map<String, DocumentPart> documentParts) {
        this.documentParts = documentParts;
    }

    public DocumentPart getDocumentPart(String name) {
        return documentParts.get(name);
    }
}
