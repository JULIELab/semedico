package de.julielab.semedico.pages;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.util.TextStreamResponse;
import org.slf4j.Logger;

import de.julielab.semedico.search.IRdfSearchService;

@Import(library = { "context:js/NetworkViewer/min/json2.min.js", // JSON support
																	// for IE
		"context:js/NetworkViewer/min/AC_OETags.min.js", // Flash embedding
															// utility
		"context:js/NetworkViewer/min/cytoscapeweb.min.js", // Cytoscape Web JS
															// API
		"context:js/NetworkViewer/NetworkViewer.js" // Controls for the
													// visualization
})
/**
 * A webpage showing ppi-networks with the cytoscpae web plugin
 */
public class NetworkViewer {

	@Inject
	private IRdfSearchService rdfQueryService;
	@Inject
	private Logger log;


	@Property
	private String nodes = "no results	";

	/**
	 * Called from NetworkViewer.js (in ctxt), loads new network
	 * 
	 * @param node
	 *            Center of the new network
	 * @return New network, will be parsed by the users flash plugin
	 */
	public StreamResponse onLoadNetwork(String node) {
		System.out.println(node);
		try {
			return new TextStreamResponse("text", "UTF-8",
					rdfQueryService.getSubgraph(node));
		} catch (Exception e) {
			log.error(e.getStackTrace()[0].toString());
			e.printStackTrace();
			return new TextStreamResponse("text", "UTF-8", "Error!");
		}
	}

	public void onActivate(String nodes) {
		List<String> toQuery = new ArrayList<String>();
		//code to split stuff like P1_X-regulates-P2_Y
		String[] parts = nodes.split("--");
		for(String part : parts)
			for(String protein : part.split("-.*-"))
				toQuery.add(protein);
		this.nodes = StringUtils.join(toQuery, "--");
	}

	public String onPassivate() {
		return nodes;
	}
}
