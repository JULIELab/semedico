package de.julielab.semedico.core.search.components.data;

import java.util.List;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.parsing.ParseTree;

/**
 * This class should be obsolete as soon as there is a single "SemedicoQuery" object,
 * namely the ParseTree. Then, each search node query would just be the correct
 * QueryTree (perhaps a better name anyways) - no hassle with search node
 * indexes etc.
 * 
 * @author faessler
 * 
 */
@Deprecated
public class SearchNodeSearchCommand {
//	public List<Multimap<String, IFacetTerm>> searchNodes;
	public List<ParseTree> searchNodes;
	public int nodeIndex = Integer.MIN_VALUE;
	public IConcept linkTerm;
}
