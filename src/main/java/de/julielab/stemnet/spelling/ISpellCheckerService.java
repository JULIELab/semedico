package de.julielab.stemnet.spelling;

import java.io.IOException;

public interface ISpellCheckerService {

	public String[] suggestSimilar(String word) throws IOException;
}
