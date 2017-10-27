package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.core.facets.IFacetDeterminer;

public interface IFacetDeterminerManager {
	public IFacetDeterminer getFacetDeterminer(Class<?> marker);
}
