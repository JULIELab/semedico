package de.julielab.semedico.core.parsing;

/**
 * An interface to mark {@link Node} classes to carry conceptual meaning like terms or events.
 * 
 * @author faessler
 * 
 */
public interface ConceptNode {
	/**
	 * Whether this parse node is ambiguous or not. Terms themselves cannot be ambiguous because they represent atomic
	 * semantic parts of the Semedico system. A query consisting of words, however, may be interpreted in multiple ways
	 * and thus may contain ambiguous parts or units.
	 * 
	 * @return
	 */
	boolean isAmbiguous();
}
