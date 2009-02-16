/** 
 * IClientIdentificationService.java
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


public interface IClientIdentificationService {
	public final static String IEXPLORER = "MSIE";
	public final static String FIREFOX = "Firefox";
	public final static String UNKNOWN = "Unknown";
	public Client identifyClient();
}
