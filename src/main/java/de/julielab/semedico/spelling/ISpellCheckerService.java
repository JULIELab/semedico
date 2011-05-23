package de.julielab.semedico.spelling;

import java.io.IOException;

public interface ISpellCheckerService {

	public String[] suggestSimilar(String word) throws IOException;
}
