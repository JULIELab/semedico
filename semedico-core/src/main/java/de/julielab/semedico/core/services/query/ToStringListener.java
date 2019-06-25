package de.julielab.semedico.core.services.query;

import de.julielab.scicopia.core.parsing.ScicopiaBaseListener;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayDeque;
import java.util.Deque;

public class ToStringListener extends ScicopiaBaseListener {

    private StringBuilder sb = new StringBuilder();
    private Deque<String> operators = new ArrayDeque<>();

    public StringBuilder getStringBuilder() {
        return sb;
    }

    @Override
    public void enterPart(ScicopiaParser.PartContext ctx) {
        // part is just the only child of query, thus we need to ask query if it was the first child of its parent
        boolean isFirstChild = ctx.getParent() == ctx.getParent().getParent().children.stream().filter(c -> !(c instanceof TerminalNode)).findFirst().get();
        // The first child does not output the operator because it doesn't know if there is a second child.
        if (isFirstChild) {
            // If the parent is a unary operator, we process it here because there won't be a second child
            if (ctx.getParent().getParent().getChildCount() == 2) sb.append(operators.pop() + " ");
            sb.append(ctx.getText());
        } else {
            sb.append(" " + operators.pop() + " ");
            sb.append(ctx.getText());
        }
    }


    @Override
    public void enterQuery(ScicopiaParser.QueryContext ctx) {
        // query can just be derived to part in which case there is no logical operator (only case) and only one child
        if (ctx.getChildCount() > 1) {
            final ParseTree firstNonTerminalChild = ctx.getParent() != null ? ctx.getParent().children.stream().filter(c -> !(c instanceof TerminalNode)).findFirst().get() : null;
            TerminalNode terminalNode = ctx.children.stream().filter(c -> c instanceof TerminalNode).map(TerminalNode.class::cast).findFirst().get();

            boolean isFirstChild = firstNonTerminalChild != null ? ctx == firstNonTerminalChild : true;
            if (!isFirstChild) {
                sb.append(" " + operators.pop() + " ");
            }
            // Open the new subtree. But omit parenthesis around the root node.
            if (ctx.parent != null)
                sb.append("(");
            operators.push(terminalNode.getText());
        }
    }


    @Override
    public void exitQuery(ScicopiaParser.QueryContext ctx) {
        // query can just be derived to part in which case there is no logical operator (only case) and only one child
        if (ctx.getChildCount() > 1) {
            // Omit parenthesis around the root node.
            if (ctx.parent != null)
                sb.append(")");
        }
    }


}