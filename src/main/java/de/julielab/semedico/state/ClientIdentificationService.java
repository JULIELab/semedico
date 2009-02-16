/** 
 * ClientIdentificationService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 08.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.state;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tapestry5.services.ApplicationStateCreator;
import org.apache.tapestry5.services.Request;

public class ClientIdentificationService implements
		IClientIdentificationService, ApplicationStateCreator<Client> {
	public final static String USER_AGENT_HEADER = "User-Agent";

	private Request request;

	public ClientIdentificationService(Request request) {
		super();
		this.request = request;
	}

	@Override
	public Client identifyClient() {

		String userAgent = request.getHeader(USER_AGENT_HEADER);

		Pattern pattern = Pattern.compile(".+" + IEXPLORER + " (\\d).+");
		Matcher matcher = pattern.matcher(userAgent);

		// test IE
		if (matcher.matches()) {
			String version = matcher.group(1);
			if (version != null) {
				if (version.equals("6"))
					return Client.IEXPLORER6;
				else if (version.equals("7"))
					return Client.IEXPLORER7;
			}
			return new Client(IEXPLORER, new Integer(version));
		}

		pattern = Pattern.compile((".+" + FIREFOX + "/(\\d).+"));
		matcher = pattern.matcher(userAgent);

		// test Firefox
		if (matcher.matches()) {
			String version = matcher.group(1);
			if( version.equals("2"))
				return Client.FIREFOX2;
			else if( version.equals("3") )
				return Client.FIREFOX3;
		}

		return Client.UNKNOWN;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	
	public Client create() {

		return identifyClient();
	}

}
