package de.julielab.stemnet.search;

import de.julielab.stemnet.core.FacetTerm;

public class TermTable implements ITermTable {

	private FacetTerm[][][] termTable;

	public TermTable(FacetTerm[][][] termTable) {
		super();
		this.termTable = termTable;
	}
	@Override
	public FacetTerm[][][] getTable() {
		return termTable;
	}

}
