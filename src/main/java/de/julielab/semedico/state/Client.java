/** 
 * Client.java
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

public class Client {

	public final static Client IEXPLORER6 = new Client(IClientIdentificationService.IEXPLORER, 6);
	public final static Client IEXPLORER7 = new Client(IClientIdentificationService.IEXPLORER, 7);
	public final static Client FIREFOX2 = new Client(IClientIdentificationService.FIREFOX, 2);
	public final static Client FIREFOX3 = new Client(IClientIdentificationService.FIREFOX, 3);
	public final static Client UNKNOWN = new Client(IClientIdentificationService.UNKNOWN, -1);

	private String name;
	private Integer version;

	public Client(String name, Integer version) {
		super();
		this.name = name;
		this.version = version;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getVersion() {
		return version;
	}
	
	public void setVersion(Integer version) {
		this.version = version;
	}
	
}
