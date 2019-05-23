package de.julielab.semedico.components;

import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.state.SemedicoSessionState;

@Import(stylesheet = { "context:css/semedico-filterPanel.css"})

public class FilterPanel {
	@SessionState
	@Property
	private SemedicoSessionState sessionState;
	
	@Persist
	ParseTree parseTree;
	
	public void onActionFromSortSelection() {
		// bubble up to ResultList and handle the event there
	}

}
