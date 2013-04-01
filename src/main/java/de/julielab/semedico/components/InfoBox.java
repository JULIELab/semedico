package de.julielab.semedico.components;

import java.util.List;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/*
 * DESCRIPTION
 * 
 * @author rzymski
 */

public class InfoBox {
	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	@Path("infobox.js")
	protected Asset infoBoxJS;
		
	@Inject
	private ComponentResources resources;

	@Property
	@Parameter
	private String arrowDirection;

	@Property
	@Parameter
	private String infoBoxContent;

	private String clientId;

	void beginRender(MarkupWriter writer) {
		clientId = javaScriptSupport.allocateClientId(resources);
		String output = infoBoxContent;
	}

	void afterRender(MarkupWriter writer) {
		javaScriptSupport.importJavaScriptLibrary(infoBoxJS);
		writer.end();
	}

//	public String getClientId() {
//		return clientId;
//	}
}