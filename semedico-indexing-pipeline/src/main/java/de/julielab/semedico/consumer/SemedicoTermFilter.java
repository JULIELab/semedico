package de.julielab.semedico.consumer;

import de.julielab.jcore.consumer.es.filter.AbstractFilter;
import de.julielab.jcore.consumer.es.filter.Filter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SemedicoTermFilter extends AbstractFilter {
	private Matcher matcher;

	public SemedicoTermFilter() {
		super();
		Pattern termP = Pattern.compile("^(" + "tid" + "|" + "atid"
				+ ").+");
		matcher = termP.matcher("");
	}

	@Override
	public List<String> filter(String input) {
		newOutput();
		matcher.reset(input);
		if (matcher.matches())
			output.add(input);
		return output;
	}

	@Override
	public Filter copy() {
		return new SemedicoTermFilter();
	}

}
