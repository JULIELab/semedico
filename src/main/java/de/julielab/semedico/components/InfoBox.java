package de.julielab.semedico.components;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

/*
 * DESCRIPTION
 * 
 * @author rzymski
 */

// Just some personal notes, please ignore for now:
// infoBox components:
//  - infoBoxTooltip (based on pop-jquery-tooltip)
//  - infoBoxDialog (based on jQuery-UI-dialog)
//  - infoBoxSlide (based on ???, custom dialogue to 'fade in' additional
//    information

// The tree-view for facets is probably realised easiest in an individual,
// separate component.

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