package de.julielab.semedico.core.entities.documents;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Objects;

/**
 * Lucene/ElasticSearch fields are named objects that have properties like "is indexed", "is stored", "has term vectors stored" etc.
 * A Semedico field is connected to the Lucene fields via their name. However, for Semedico a different set of properties, unknown to Lucene, is important.
 * In Semedico, a field can contain concept identifiers. This is an information useful for implementations of {@link de.julielab.semedico.core.search.query.translation.IQueryTranslator}.
 */
public class SemedicoIndexField {
    private EnumSet<Type> types;
    private String name;

    public SemedicoIndexField(String name, EnumSet<Type> types) {
        this.name = name;
        this.types = types;
    }

    /**
     * Creates a field with the given name and the {@link Type#STRING_TERMS} type.
     * @param name The Lucene field name to query.
     */
    public SemedicoIndexField(String name) {
        this(name, EnumSet.of(Type.STRING_TERMS));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SemedicoIndexField that = (SemedicoIndexField) o;
        return Objects.equals(types, that.types) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(types, name);
    }

    public EnumSet<Type> getType() {
        return types;
    }

    public String getName() {
        return name;
    }

    /**
     * Checks if this field has only the one field type given by <tt>type</tt>.
     * @param type The checked field type.
     * @return True iff this field only has exactly one type and this type is the passed <tt>type</tt>.
     */
    public boolean isOnly(Type type) {
        return types.size() == 1 && types.contains(type);
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

    /**
     * Creates an index field of type {@link Type#STRING_TERMS}.
     * @param fieldname The name of the field.
     * @return A new field with the {@link Type#STRING_TERMS} type.
     */
    public static SemedicoIndexField termsField(String fieldname) {
        return new SemedicoIndexField(fieldname, EnumSet.of(Type.STRING_TERMS));
    }

    /**
     * Creates an index field of type {@link Type#CONCEPTS}.
     * @param fieldname The name of the field.
     * @return A new field with the {@link Type#CONCEPTS} type.
     */
    public static SemedicoIndexField conceptsOnlyField(String fieldname) {
        return new SemedicoIndexField(fieldname, EnumSet.of(Type.CONCEPTS));
    }

    /**
     * Creates a new field with the given name and types.
     * @param fieldname The field name.
     * @param types The types for the field.
     * @return The new field.
     */
    public static SemedicoIndexField field(String fieldname, Type... types) {
        EnumSet<Type> enumSet = EnumSet.noneOf(Type.class);
        if (types.length > 0) {
            Type first = types[0];
            if (types.length > 1) {
                final Type[] tail = Arrays.copyOfRange(types, 1, types.length);
                enumSet = EnumSet.of(first, tail);
            } else {
                enumSet = EnumSet.of(first);
            }
        }
        return new SemedicoIndexField(fieldname, enumSet);
    }
}
