package de.julielab.semedico.core.search.components.data;

import de.julielab.semedico.core.facets.Facet;

public class MessageLabel extends Label {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6168260208097247628L;
	private String message;
	private String longMessage;

	public MessageLabel(String message, String longMessage) {
		this.message = message;
		this.longMessage = longMessage;
	}
	
	@Override
	public String getName() {
		return message;
	}

	@Override
	public boolean hasChildHitsInFacet(Facet facet) {
		return false;
	}

	@Override
	public String getId() {
		return null;
	}

	@Override
	public boolean isTermLabel() {
		return false;
	}

	@Override
	public boolean isStringLabel() {
		return false;
	}

	@Override
	public boolean isMessageLabel() {
		return true;
	}
	
	public String getLongMessage() {
		return longMessage;
	}

}
