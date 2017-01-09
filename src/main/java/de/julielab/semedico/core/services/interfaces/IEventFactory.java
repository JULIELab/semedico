package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.core.facetterms.Event;
import de.julielab.semedico.core.parsing.EventNode;

public interface IEventFactory {
	Event createEvent(String indexEventString);

	Event createEvent(EventNode eventNode);
}
