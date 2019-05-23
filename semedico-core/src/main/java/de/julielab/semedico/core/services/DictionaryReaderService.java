package de.julielab.semedico.core.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;

import de.julielab.semedico.core.services.interfaces.IDictionaryReaderService;

public class DictionaryReaderService implements IDictionaryReaderService {
	
	private static Logger logger = LoggerFactory.getLogger(DictionaryReaderService.class);	
	private static final String SEPARATOR = "\t";

	public Multimap<String, String> readDictionary(String filePath) throws IOException {
		File dictFile = new File(filePath);
		if (!dictFile.isFile()) {
			logger.error("Dictionary {} does not exist.", filePath);
			throw new FileNotFoundException("Dictionary " + filePath + " does not exist.");
		}
		if (filePath.endsWith("gz")) {
			return readDictionary(filePath, (int) dictFile.length() / 11);
		} else {
			return readDictionary(filePath, (int) dictFile.length() / 37);
		}
	}
	
	public Multimap<String, String> readDictionary(String filePath, int dictSize) throws IOException {
		ListMultimap<String, String> dict = MultimapBuilder.hashKeys(dictSize).arrayListValues(5).build();
		try (InputStream is = (filePath.endsWith("gz"))
				? new GZIPInputStream(new FileInputStream(filePath))
				: new FileInputStream(filePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));) {

			String line = reader.readLine();
			while (line != null) {
				if (line.startsWith("#")) {
					continue;
				}
				String[] split = line.split(SEPARATOR);
				dict.put(split[0], split[1]);
				line = reader.readLine();
			}
		}
		return dict;
	}
	
}