package de.julielab.semedico.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import de.julielab.semedico.core.query.QueryToken;

public class EventQueryToken extends QueryToken {
	public static final EventQueryToken EMPTY_TOKEN = new EventQueryToken();
	private List<QueryToken> arguments;
	private List<QueryToken> likelihoodIndicators;

	public EventQueryToken(int beginOffset, int endOffset) {
		super(beginOffset, endOffset);
		this.arguments = new ArrayList<>();
		this.likelihoodIndicators = new ArrayList<>();
	}

	private EventQueryToken() {
		this(0, 0);
	}

	public void addArgument(QueryToken argument) {
		arguments.add(argument);
	}

	public void setArguments(QueryToken[] arguments) {
		this.arguments = Arrays.asList(arguments);
	}

	public void setArguments(List<QueryToken> arguments) {
		this.arguments = arguments;
	}

	public int getNumArguments() {
		return arguments.size();
	}

	public List<QueryToken> getArguments() {
		return arguments;
	}

	public List<QueryToken> getLikelihoodIndicators() {
		return likelihoodIndicators;
	}

	public void setLikelihoodIndicators(List<QueryToken> likelihoodIndicators) {
		this.likelihoodIndicators = likelihoodIndicators;
	}

	public boolean isEmpty() {
		return getBeginOffset() - getEndOffset() == 0;
	}

	public int size() {
		return arguments.size() + 1;
	}

	public QueryToken getFirstArgument() {
		if (!arguments.isEmpty())
			return arguments.get(0);
		return null;
	}

	public QueryToken getSecondArgument() {
		if (!arguments.isEmpty() && arguments.size() > 1)
			return arguments.get(1);
		return null;
	}

	public QueryToken getLastArgument() {
		if (!arguments.isEmpty())
			return arguments.get(arguments.size() - 1);
		return null;
	}

	public Collection<? extends QueryToken> getQueryTokenStructure() {
		List<QueryToken> structure = new ArrayList<>();
		QueryToken firstArgument = getFirstArgument();
		QueryToken lastArgument = getLastArgument();
		if (null != firstArgument && firstArgument.getBeginOffset() < getBeginOffset())
			structure.add(firstArgument);
		structure.add(this);
		if (null != lastArgument && lastArgument.getBeginOffset() > getBeginOffset())
			structure.add(lastArgument);
		return structure;
	}

	public int getEventEndOffset() {
		QueryToken lastArgument = getLastArgument();
		if (lastArgument.getEndOffset() > getEndOffset())
			return lastArgument.getEndOffset();
		return getEndOffset();
	}

}
