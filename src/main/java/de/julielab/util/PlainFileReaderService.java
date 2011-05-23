/** 
 * PlainFileReaderService.java
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
 * Creation date: 30.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.hivemind.ServiceImplementationFactory;
import org.apache.hivemind.ServiceImplementationFactoryParameters;

public class PlainFileReaderService implements IPlainFileReaderService, ServiceImplementationFactory{

	@Override
	public Set<String> readFile(String filePath) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(filePath));
		Set<String> lineList = new HashSet<String>();

		String stopword = reader.readLine();
		while (stopword != null) {
			lineList.add(stopword.trim());
			stopword = reader.readLine();
			if( stopword != null )
				stopword.trim();
		}

		reader.close();
		return lineList;	
	}

	@Override
	public Object createCoreServiceImplementation(ServiceImplementationFactoryParameters factoryParams) {
		Object filePathObject = factoryParams.getFirstParameter();
		String filePath = null;
		if( filePathObject != null && filePathObject instanceof String )
			filePath = filePathObject.toString();
		
		try {
			return readFile(filePath);
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}
}
