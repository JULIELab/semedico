package de.julielab.semedico.core.services.query;

import de.julielab.java.utilities.spanutils.OffsetMap;
import de.julielab.scicopia.core.parsing.ScicopiaLexer;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class SecopiaParsingService implements ISecopiaParsingService {
    private final Set<ITokenInputService.TokenType> preanalyzed;
    private final Logger log;

    public SecopiaParsingService(Logger log) {
        this.log = log;
        this.preanalyzed = new TreeSet<>();
//        preanalyzed.add(ITokenInputService.TokenType.AMBIGUOUS_CONCEPT);
//        preanalyzed.add(ITokenInputService.TokenType.CONCEPT);
//        preanalyzed.add(ITokenInputService.TokenType.KEYWORD);
    }

    @Override
    public SecopiaParse parseQueryTokens(List<QueryToken> tokens) {
        List<QueryToken> specialTokens = new ArrayList<>();
        int special = 0;
        StringBuilder builder = new StringBuilder();
        if (!tokens.isEmpty()) {
            for (QueryToken userToken : tokens) {
                if (preanalyzed.contains(userToken.getInputTokenType())) {
                    specialTokens.add(userToken);
                    builder.append("⌨" + special + "⌨ ");
                    special++;
                } else if (userToken.getType() == QueryToken.Category.KW_PHRASE){
                    builder.append("\"").append(userToken.getOriginalValue()).append("\"").append(" ");
                }
                else{
                    builder.append(userToken.getOriginalValue() + " ");
                }
            }
            builder.setLength(builder.length() - 1);
            String queryString = builder.toString();
            CodePointCharStream stream = CharStreams.fromString(queryString);
            ScicopiaLexer lexer = new ScicopiaLexer(stream);
            CommonTokenStream tokenstream = new CommonTokenStream(lexer);
            ScicopiaParser parser = new ScicopiaParser(tokenstream);
            ParseTree tree = parser.query();
            return new SecopiaParse(tree, new OffsetMap<>(tokens));
        } else {
            log.warn("An empty list of query tokens was passed to the parsing service.");
        }
        return null;
    }
}
