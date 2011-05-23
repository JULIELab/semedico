package de.julielab.stemnet.spelling;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.StringDistance;
import org.apache.lucene.store.RAMDirectory;


public class SpellCheckerService implements ISpellCheckerService{
	
	private Logger LOGGER = Logger.getLogger(SpellCheckerService.class);
	private static float ACCURACY = 0.8f;
	private SpellChecker spellChecker;
	private int numberOfSuggestions;
	
	public SpellCheckerService(Dictionary dictionary, StringDistance stringDistance, int numberOfSuggestions) throws IOException {
		long time = System.currentTimeMillis();
		LOGGER.info("starting indexing");
		this.spellChecker = new SpellChecker(new RAMDirectory(), stringDistance);
		this.spellChecker.indexDictionary(dictionary);
		this.spellChecker.setAccuracy(ACCURACY);
		this.numberOfSuggestions = numberOfSuggestions;
		LOGGER.info("... takes " + (System.currentTimeMillis() - time));
	}
	
	@Override
	public String[] suggestSimilar(String word) throws IOException {
		
		return spellChecker.suggestSimilar(word, numberOfSuggestions);
	}

}
