package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.QueryToken;

import java.util.List;

public interface ISecopiaParsingService {
    SecopiaParse parseQueryTokens(List<QueryToken> tokens);
}
