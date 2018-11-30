package de.julielab.semedico.core.entities.documents;

/**
 * Lucene/ElasticSearch fields are named objects that have properties like "is indexed", "is stored", "has term vectors stored" etc.
 * A Semedico field is connected to the Lucene fields via their name. However, for Semedico a different set of properties, unknown to Lucene, is important.
 * In Semedico, a field can contain concept identifiers. This is an information useful for implementations of {@link de.julielab.semedico.core.search.query.translation.IQueryTranslator}.
 */
public class SemedicoIndexField {
    private Type type;
    private String name;

    public SemedicoIndexField(Type type, String name) {
        this.type = type;
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public enum Type {
        /**
         * Fields with this type have index values that may contain concept identifiers.
         */
        CONCEPTS,
        /**
         * Fields with this type have index values that are simple strings derived from the original field value.
         */
        STRING_TERMS
    }
}
