package de.julielab.semedico.core.services.query;

import de.julielab.scicopia.core.parsing.ScicopiaBaseListener;
import de.julielab.scicopia.core.parsing.ScicopiaLexer;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ToStringListener extends ScicopiaBaseListener {

    private StringBuilder sb = new StringBuilder();

    public StringBuilder getStringBuilder() {
        return sb;
    }

    @Override
    public void visitTerminal(TerminalNode node) {
        if (node.getSymbol().getType() == ScicopiaLexer.RPAR && sb.length() > 0)
            sb.deleteCharAt(sb.length() - 1);
        sb.append(node.getSymbol().getText()).append(" ");
    }

    @Override
    public void exitQuery(ScicopiaParser.QueryContext ctx) {
        // Clean up if this is the root: Removing the trailing whitespace and potential outer parenthesis.
        if (ctx.parent == null) {
            sb.deleteCharAt(sb.length() - 1);
            if (sb.charAt(0) == '(' && sb.charAt(sb.length()-1) == ')') {
                sb.deleteCharAt(0);
                sb.deleteCharAt(sb.length() - 1);
            }
        }
    }

    @Override
    public void enterBool(ScicopiaParser.BoolContext ctx) {
        if (ctx.getChildCount() > 1)
            sb.append("(");
    }

    @Override
    public void exitBool(ScicopiaParser.BoolContext ctx) {
        // Omit parenthesis around the root node.
        if (ctx.getChildCount() > 1) {
            if (ctx.children.size() > 1) {
                if (sb.length() > 0)
                    sb.deleteCharAt(sb.length() - 1);
                sb.append(") ");
            }
        }
    }


}