package de.julielab.semedico.core.services.interfaces;

import com.google.common.collect.Multimap;

import java.io.IOException;

public interface IDictionaryReaderService {

	Multimap<String, String> readDictionary(String filePath) throws IOException;
}