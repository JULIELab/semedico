package de.julielab.semedico.core.search.query;

import java.util.Collection;
import java.util.Set;

/**
 * A query interface that allows to set fields that should be searched and fields that should be returned from
 * the index. Thus, the index must be field-based like Lucene.
 */
public interface IFieldQuery extends ISemedicoQuery {
    Set<String> getSearchedFields();
    Collection<String> getRequestedFields();

}
