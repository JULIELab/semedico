package de.julielab.semedico.components;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONObject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

@Import(library = { "context:js/jquery-ui/jquery-ui.min.js", "tooltip.js" })
public class Tooltip {

	@Parameter
	private String title;

	@Parameter
	private String content;

	@Parameter
	private String element;

	@Parameter
	private boolean disabled;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources resources;
	
	private String clientId;

	boolean setupRender() {
		// returning false jumps to the "cleanupRender" phase, circumventing
		// rendering
		if (disabled)
			return false;
		return true;
	}

	void beginRender(MarkupWriter writer) {
		clientId = javaScriptSupport.allocateClientId(resources);
		String elementToRender = StringUtils.isBlank(element) ? "span" : element;
		writer.element(elementToRender, "id", clientId, "title", title);
	}

	void afterRender(MarkupWriter writer) {
		writer.end();
		JSONObject options = new JSONObject();
		if (!StringUtils.isBlank(content))
			options.put("content", content);
		javaScriptSupport.addScript("Semedico.initTooltip('%s', %s)", clientId, options);
	}

}
