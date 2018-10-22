package de.julielab.semedico.core.docmod.base.entities;

/**
 * Simple class to describe the part of a document type / corpus. To be used in {@link DocModInfo}.
 *
 * @see DocModInfo
 */
public class DocumentPart {
    private String name;

    public DocumentPart(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
