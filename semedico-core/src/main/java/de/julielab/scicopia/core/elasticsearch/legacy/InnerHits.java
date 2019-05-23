package de.julielab.scicopia.core.elasticsearch.legacy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.julielab.scicopia.core.elasticsearch.legacy.HighlightCommand;

public class InnerHits {

	public HighlightCommand highlight;
	public boolean fetchSource;
	public List<String> fields = Collections.emptyList();
	public boolean explain;
	public Integer size;

	public void addField(String field) {
		if (fields.isEmpty())
			fields = new ArrayList<>();
		fields.add(field);
	}
}
