package de.julielab.semedico.core.services.interfaces;

import java.io.IOException;

import com.google.common.collect.Multimap;

public interface IDictionaryReaderService {

	public Multimap<String, String> readDictionary(String filePath) throws IOException;
}