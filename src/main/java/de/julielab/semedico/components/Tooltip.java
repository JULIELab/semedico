package de.julielab.semedico.components;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

@Import(library = { "context:js/jquery-ui/js/jquery-ui-1.10.2.custom.min.js",
		"tooltip.js" }, stylesheet = { "context:js/jquery-ui/css/smoothness/jquery-ui.min.css" })
public class Tooltip {
	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources resources;

	private String clientId;

	@Property
	@Parameter
	private boolean useQuestionMark;

	@Property
	@Parameter
	private String title;

	@Property
	@Parameter
	private String firstParagraph;

	@Property
	@Parameter
	private String secondParagraph;

	void beginRender(MarkupWriter writer) {
		clientId = javaScriptSupport.allocateClientId(resources);
		// clientId = renderSupport.allocateClientId(resources);
		String output = "";
		if (useQuestionMark) {
			output = "<img src=\"\" class=\"ui-icon ui-icon-help\" style=\"position: absolute; left: 0px\"/>&nbsp;";
		}
		if (title != null) {
			output += "<b>" + title + "</b><br/>";
		}
		if (firstParagraph != null) {
			output += firstParagraph + "<br/>";
		}
		if (secondParagraph != null) {
			output += secondParagraph + "<br/>";
		}

		writer.element("span", "id", clientId, "title", "" + output + "");
	}

	void afterRender(MarkupWriter writer) {
		writer.end();
	}

	public String getClientId() {
		return clientId;
	}

}
