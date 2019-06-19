package de.julielab.semedico.core.search.query;

import de.julielab.semedico.core.entities.documents.SemedicoIndexField;

import java.util.List;

/**
 * A query interface that allows to set fields that should be searched and fields that should be returned from
 * the index. Thus, the index must be field-based like Lucene.
 */
public interface IFieldQuery<Q> extends ISemedicoQuery<Q> {
    void setSearchedFields(List<SemedicoIndexField> searchedFields);

    void setRequestedFields(List<String> requestedFields);

    List<SemedicoIndexField> getSearchedFields();

    List<String> getRequestedFields();

}
