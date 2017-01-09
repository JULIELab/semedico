/** 
 * ExternalLink.java
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
 * Creation date: 21.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core;

public class ExternalLink
{
	public String url;
	public String iconUrl;
	
	public ExternalLink(String url, String iconUrl)
	{
		super();
		this.url = url;
		this.iconUrl = iconUrl;
	}
	public String getUrl()
	{
		return url;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public String getIconUrl()
	{
		return iconUrl;
	}
	public void setIconUrl(String iconUrl)
	{
		this.iconUrl = iconUrl;
	}
}
