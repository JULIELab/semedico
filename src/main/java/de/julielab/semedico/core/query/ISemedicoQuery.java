package de.julielab.semedico.core.query;

import java.util.Set;

public interface ISemedicoQuery {
	<T> T getQuery();
	Set<String> getSearchFieldFilter();
}
