package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.ObjectLocator;
import org.slf4j.Logger;

import de.julielab.semedico.core.facets.IFacetDeterminer;
import de.julielab.semedico.core.services.interfaces.IFacetDeterminerManager;

public class FacetDeterminerManager implements IFacetDeterminerManager {

	private Logger log;
	private ObjectLocator objectLocator;

	public FacetDeterminerManager(Logger log, ObjectLocator objectLocator) {
		this.log = log;
		this.objectLocator = objectLocator;
	}

	@Override
	public IFacetDeterminer getFacetDeterminer(Class<?> determinerClass) {
		// There is currently no implemented facet determiner. Used to be the BioPortal recommander determiner but we didn't follow that road further.
		return null;
	}

}
