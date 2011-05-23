package de.julielab.semedico.search;

import de.julielab.semedico.core.FacetTerm;

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
