package de.julielab.semedico.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

@Import(library = { "context:js/bootstrap/js/bootstrap.js" }, stylesheet = { "context:js/bootstrap/css/bootstrap.css" })
public class FormDialog implements ClientElement {

	@Parameter(value = "prop:componentResources.id", defaultPrefix = BindingConstants.LITERAL)
	private String clientId;

	@Parameter(required = true)
	@Property
	private Block content;

	@Property
	@Parameter(required = false, defaultPrefix = "literal")
	private String title;

	@Property
	private String test;

	@Inject
	private JavaScriptSupport js;

	@Override
	public String getClientId() {
		return clientId;
	}

	@AfterRender
	private void afterRender() {
		js.addScript("$j('#%s').modal()", getClientId());
	}

}