package de.julielab.semedico.core.services.query;

import de.julielab.scicopia.core.parsing.ScicopiaBaseListener;
import de.julielab.scicopia.core.parsing.ScicopiaLexer;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayDeque;
import java.util.Deque;

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
    public void enterBinaryBoolean(ScicopiaParser.BinaryBooleanContext ctx) {
        if (ctx.parent != null)
            sb.append("(");
    }

    @Override
    public void exitBinaryBoolean(ScicopiaParser.BinaryBooleanContext ctx) {
        // Omit parenthesis around the root node.
        if (ctx.parent != null) {
            if (sb.length() > 0)
                sb.deleteCharAt(sb.length() - 1);
            sb.append(") ");
        } else {
            // This is the root, the traversal is finished. Remove the trailing whitespace from the output.
            sb.deleteCharAt(sb.length() - 1);
        }
    }


}