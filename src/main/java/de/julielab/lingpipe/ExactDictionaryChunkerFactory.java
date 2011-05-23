/** 
 * ExactDictionaryChunkerFactory.java
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
 * Creation date: 29.07.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.lingpipe;

import java.io.IOException;

import org.apache.hivemind.ServiceImplementationFactory;
import org.apache.hivemind.ServiceImplementationFactoryParameters;
import org.apache.log4j.Logger;

import com.aliasi.chunk.Chunker;
import com.aliasi.dict.ExactDictionaryChunker;
import com.aliasi.dict.MapDictionary;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;

public class ExactDictionaryChunkerFactory implements ServiceImplementationFactory {

	private IDictionaryReaderService dictionaryReaderService;
	private static final Logger LOGGER = Logger.getLogger(ExactDictionaryChunkerFactory.class);
	public ExactDictionaryChunkerFactory() {}

	/* (non-Javadoc)
	 * @see de.julielab.lingpipe.IChunkerFactory#createChunker()
	 */
	public Chunker createChunker(boolean caseSensitive) throws IOException{
		MapDictionary<String> dictionary = dictionaryReaderService.getMapDictionary();
		LOGGER.info("creating new chunker ..");
		ExactDictionaryChunker chunker = new ExactDictionaryChunker(dictionary, IndoEuropeanTokenizerFactory.FACTORY, false, caseSensitive); 
		chunker.setReturnAllMatches(true);
		LOGGER.info(".. ready");
		return chunker;
	}
	
	public IDictionaryReaderService getDictionaryReaderService() {
		return dictionaryReaderService;
	}

	public void setDictionaryReaderService(IDictionaryReaderService dictionaryReaderService) {
		this.dictionaryReaderService = dictionaryReaderService;
	}


	@Override
	public Object createCoreServiceImplementation(ServiceImplementationFactoryParameters factoryParams) {
		Chunker chunker = null;
		Object caseSensitiveObject = factoryParams.getFirstParameter();
		boolean caseSensitive = false;
		if( caseSensitiveObject != null && caseSensitiveObject instanceof Boolean )
			caseSensitive = ((Boolean)caseSensitiveObject).booleanValue();

		try {
			chunker = createChunker(caseSensitive);
			
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}

		return chunker;
	}
}
