package de.julielab.semedico.elasticsearch.index.setup.filter;

import de.julielab.semedico.elasticsearch.index.setup.Filter;

public class PatternReplace extends Filter {
	public String pattern;
	public String replacement;
	public String replace;

	public PatternReplace(String pattern, String replacement, String replace) {
		super("pattern_replace");
		this.pattern = pattern;
		this.replacement = replacement;
		this.replace = replace;
	}

}
