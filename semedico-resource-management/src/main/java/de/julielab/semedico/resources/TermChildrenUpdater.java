package de.julielab.semedico.resources;

import de.julielab.neo4j.plugins.ConceptManager;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.INeo4jHttpClientService;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.slf4j.Logger;

import java.io.IOException;

public class TermChildrenUpdater implements ITermChildrenUpdater {

	private Logger log;
	private IHttpClientService httpClientService;
	private String neo4jEndpoint;

	public TermChildrenUpdater(Logger log, INeo4jHttpClientService httpClientService,
			@Symbol(SemedicoSymbolConstants.NEO4J_REST_ENDPOINT) String neo4jEndpoint) {
		this.log = log;
		this.httpClientService = httpClientService;
		this.neo4jEndpoint = neo4jEndpoint;

	}

	@Override
	public void updateChildrenInformation() {
		log.info("Triggering the Neo4j plugin to update the term children information...");
		HttpEntity response = httpClientService.sendPostRequest(neo4jEndpoint + ConceptManager.TERM_MANAGER_ENDPOINT
				+ ConceptManager.UPDATE_CHILDREN_INFORMATION);
		try {
			log.info("The server responded: {}", EntityUtils.toString(response));
		} catch (ParseException e) {
			log.error("ParseException: ", e);
		} catch (IOException e) {
			log.error("IOException: ", e);
		}
	}

}
