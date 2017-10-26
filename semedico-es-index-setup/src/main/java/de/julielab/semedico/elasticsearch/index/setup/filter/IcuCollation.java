package de.julielab.semedico.elasticsearch.index.setup.filter;

import de.julielab.semedico.elasticsearch.index.setup.Filter;

public class IcuCollation extends Filter {
	public String rules;
	public String strength;

	public IcuCollation(String rules, String strength) {
		super("icu_collation");
		this.rules = rules;
		this.strength = strength;
	}
}
