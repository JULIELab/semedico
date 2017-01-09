package de.julielab.semedico.components;

import org.apache.tapestry5.BindingConstants;
import org.apache.tapestry5.Block;
import org.apache.tapestry5.ClientElement;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

// I'll keep the non-minified versions as long as this component is in development.
@Import(library = { "fsdialog.js" })
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
		// TODO: No regular expression selector for jQuery, use clientId (if
		// clientId equals the element ID).
		js.addScript(
				"$j('#%s').dialog({modal: true, resizable: false, draggable: false, height: 600, width: 820, open: function(event, ui){/* this does exactly one thing: hide the close 'x' of the dialog because hitting it would prohibit us from cleaning the DOM appropriatly.*/$j(this).parent().children().children('.ui-dialog-titlebar-close').hide(); },closeOnEscape: false,buttons: [{ id: 'submitFacets', text: 'Submit facet selection', click: function() {checkBoxHelper(); $j(\"[id*=submitFormDialog]\").click();}}, {id: 'cancel', text: 'Cancel', click: function() {$j(this).dialog('close').dialog('destroy').remove();}}]})",
				getClientId());
		js.addScript("facetSelectionDialog()");
		js.addScript("styleFacets()");
	}
}