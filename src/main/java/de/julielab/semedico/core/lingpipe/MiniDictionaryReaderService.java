package de.julielab.semedico.core.lingpipe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.aliasi.dict.DictionaryEntry;
import com.aliasi.dict.MapDictionary;
import com.aliasi.dict.TrieDictionary;

import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * This service directly reads all terms from the term service and adds the
 * preferred name, the synonyms and the writing variants to the dictionary.
 * Thus, the term database has to be small for this approach to succeed. The
 * MiniTermService or perhaps some test database is expected.
 * 
 * @author faessler
 * 
 */
public class MiniDictionaryReaderService implements IDictionaryReaderService {

	private static final double CHUNK_SCORE = 1.0;

	private ITermService termService;

	public MiniDictionaryReaderService(ITermService termService) {
		this.termService = termService;
	}

	public List<DictionaryEntry<String>> getDictionaryEntries() {
		List<DictionaryEntry<String>> entries = new ArrayList<>();
		Iterator<IConcept> terms = termService.getTerms();
		while (terms.hasNext()) {
			Concept term = (Concept) terms.next();
			String prefName = term.getPreferredName();
			List<String> synonyms = term.getSynonyms();
			List<String> writingVariants = term.getWritingVariants();
			String category = term.getId();
			DictionaryEntry<String> de = new DictionaryEntry<>(prefName, category, CHUNK_SCORE);
			entries.add(de);
			for (String synonym : synonyms)
				entries.add(new DictionaryEntry<String>(synonym, category, CHUNK_SCORE));
			for (String variant : writingVariants)
				entries.add(new DictionaryEntry<String>(variant, category, CHUNK_SCORE));
		}
		return entries;
	}

	@Override
	public MapDictionary<String> getMapDictionary(String dictionaryFilePath) throws IOException {
		MapDictionary<String> dictionary = new MapDictionary<String>();
		for (DictionaryEntry<String> de : getDictionaryEntries())
			dictionary.addEntry(de);
		return dictionary;
	}

	@Override
	public TrieDictionary<String> getTrieDictionary(String dictionaryFilePath) {
		TrieDictionary<String> dictionary = new TrieDictionary<String>();
		for (DictionaryEntry<String> de : getDictionaryEntries())
			dictionary.addEntry(de);
		return dictionary;
	}

}
