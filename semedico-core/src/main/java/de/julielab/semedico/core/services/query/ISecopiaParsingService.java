package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.QueryToken;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;

public interface ISecopiaParsingService {
    SecopiaParse parseQueryTokens(List<QueryToken> tokens);
}
