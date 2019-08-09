package de.julielab.semedico.core.parsing;

import de.julielab.java.utilities.spanutils.OffsetMap;
import de.julielab.java.utilities.spanutils.OffsetSet;
import de.julielab.semedico.core.search.query.QueryToken;
import org.antlr.v4.runtime.tree.ParseTree;

public class SecopiaParse {
    private ParseTree parseTree;
    private OffsetMap<QueryToken> queryTokens;

    public SecopiaParse(ParseTree parseTree, OffsetMap<QueryToken> queryTokens) {

        this.parseTree = parseTree;
        this.queryTokens = queryTokens;
    }

    public ParseTree getParseTree() {
        return parseTree;
    }

    public OffsetMap<QueryToken> getQueryTokens() {
        return queryTokens;
    }
}
