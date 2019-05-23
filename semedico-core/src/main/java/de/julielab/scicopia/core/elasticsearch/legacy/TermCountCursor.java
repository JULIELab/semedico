package de.julielab.scicopia.core.elasticsearch.legacy;

import de.julielab.scicopia.core.elasticsearch.legacy.IFacetField.FacetType;

public interface TermCountCursor {
	public boolean forwardCursor();
	public String getName();
	public Number getFacetCount(FacetType type);
	public long size();
	public boolean isValid();
	public void reset();
}
