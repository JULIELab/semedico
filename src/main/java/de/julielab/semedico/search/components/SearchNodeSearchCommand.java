package de.julielab.semedico.search.components;

import java.util.List;

import com.google.common.collect.Multimap;

import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

/**
 * This class should be obsolete as soon as there is a single "SemedicoQuery" object,
 * namely the ParseTree. Then, each search node query would just be the correct
 * QueryTree (perhaps a better name anyways) - no hassle with search node
 * indexes etc.
 * 
 * @author faessler
 * 
 */
public class SearchNodeSearchCommand {
	public List<Multimap<String, IFacetTerm>> searchNodes;
	public int nodeIndex = Integer.MIN_VALUE;
	public IFacetTerm linkTerm;
}
