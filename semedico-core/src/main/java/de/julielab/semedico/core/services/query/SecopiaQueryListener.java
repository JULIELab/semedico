package de.julielab.semedico.core.services.query;

import de.julielab.scicopia.core.parsing.ScicopiaBaseListener;
import de.julielab.scicopia.core.parsing.ScicopiaParser;

import java.util.ArrayDeque;
import java.util.Deque;

public class SecopiaQueryListener extends ScicopiaBaseListener {

    private StringBuilder sb = new StringBuilder();
    private Deque<String> operators = new ArrayDeque<>();
    private Deque<String> child = new ArrayDeque<>();

    public StringBuilder getStringBuilder() {
        return sb;
    }

    @Override
    public void enterPart(ScicopiaParser.PartContext ctx) {
        final String childType = this.child.poll();
        if (childType != null && childType.equals("left")) {
            child.push("right");
            sb.append(ctx.getText());
        } else if (childType != null) {
            sb.append(" " + operators.pop() + " ");
            sb.append(ctx.getText());
        }
    }




}