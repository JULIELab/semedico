package de.julielab.semedico.pages;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.SelectModelFactory;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.base.Search;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.state.SemedicoSessionState;
import de.julielab.semedico.util.ConceptValueEncoder;

/**
 * Start page of application semedico-frontend.
 */
@Import(
	stylesheet =
	{
		"context:js/jquery-ui/jquery-ui.min.css",
		"context:css/semedico-base.css",
		"context:css/semedico-search.css",
		"context:css/index.css",
	},
	library =
	{
		"context:js/jquery.min.js",
		"context:js/jquery-ui/jquery-ui.min.js",
		"index.js",
		"context:js/tutorial.js"
	}
)

public class Index extends Search {
	@Inject
	private Request request;

	@SessionState
	private SemedicoSessionState sessionState;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources resources;

	@Deprecated
	@Inject
	SelectModelFactory selectModelFactory;

	@Deprecated
	@Inject
	protected ITokenInputService tokenInputService;
	
	@Inject
	private Logger log;
	
	@Override
	public void setupRender() {
		super.setupRender();
	}

	public ConceptValueEncoder getConceptValueEncoder() {
		return new ConceptValueEncoder(termService);
	}

	@Override
	@AfterRender
	public Object afterRender() {
		super.afterRender();
		return null;
	}

	@Override
	protected Logger getLogger() {
		return log;
	}
	
}