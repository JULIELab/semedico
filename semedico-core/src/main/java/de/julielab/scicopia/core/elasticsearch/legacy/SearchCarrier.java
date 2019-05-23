/**
 * SearchCarrier.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 06.04.2013
 **/

/**
 * 
 */
package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerResponse;

/**
 * @author faessler
 * 
 */
public class SearchCarrier {
	
	public List<SearchServerCommand> serverCmds;
	public List<ISearchServerResponse> serverResponses;
	public StopWatch sw;
	public String chainName;
	private List<String> enteredComponents;

	public SearchCarrier(String chainName) {
		this.chainName = chainName;
		enteredComponents = new ArrayList<>();
		sw = new StopWatch();
		sw.start();
		serverCmds = new ArrayList<>();
		serverResponses = new ArrayList<>();
	}
	
	public List<String> getEnteredComponents() {
		return enteredComponents;
	}
	
	public void addSearchServerCommand(SearchServerCommand serverCmd) {
		serverCmds.add(serverCmd);
	}

	public void addSearchServerResponse(ISearchServerResponse serverRsp) {
		serverResponses.add(serverRsp);
	}
	
	public List<SearchServerCommand> getSearchServerCommands() {
		return serverCmds;
	}

	public SearchServerCommand getSingleSearchServerCommandOrCreate() {
		if (serverCmds.isEmpty())
			serverCmds.add(new SearchServerCommand());
		else if (serverCmds.size() > 1)
			throw new IllegalStateException("There are " + serverCmds.size()
					+ " search server commands instead of exactly one.");
		return serverCmds.get(0);
	}

	public SearchServerCommand getSingleSearchServerCommand() {
		if (serverCmds.size() > 1)
			throw new IllegalStateException("There are " + serverCmds.size()
					+ " search server commands instead of exactly one.");
		else if (!serverCmds.isEmpty()) {
			return serverCmds.get(0);
		}
		return null;
	}

	public ISearchServerResponse getSingleSearchServerResponse() {
		if (serverResponses.size() > 1)
			throw new IllegalStateException("There are " + serverResponses.size()
					+ " search server responses instead of exactly one.");
		else if (!serverResponses.isEmpty()) {
			return serverResponses.get(0);
		}
		return null;
	}

	public void setElapsedTime() {
		sw.stop();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("State of chain ").append(chainName).append(":\n");
		sb.append("Entered components: ").append(StringUtils.join(enteredComponents, ", "));
		return sb.toString();
	}
}
