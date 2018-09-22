package de.julielab.semedico.core.search.query;

import de.julielab.semedico.core.search.ServerType;

/**
 * This is the most abstract interface for queries in Semedico. It only defines one method, {@link #getQuery()}, that
 * returns the query object. This will most commonly be a {@link de.julielab.semedico.core.parsing.ParseTree} but is
 * not restricted to one. Multiple interfaces extend on this one to provide query types that are more specific to the
 * capabilities of the index technology that can be used with a particular query.
 */
public interface ISemedicoQuery {
	<T> T getQuery();

    ServerType getServerType();
}
