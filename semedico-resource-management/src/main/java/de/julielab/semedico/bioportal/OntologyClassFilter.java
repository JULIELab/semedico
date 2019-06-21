package de.julielab.semedico.bioportal;

import de.julielab.bioportal.ontologies.data.OntologyClass;

import java.util.*;

/**
 * This class contains some cleaning rules for classes downloaded from BioPortal (using the new API since BioPortal 4.0). Among the current rules are:
 * <ul>
 * <li>Classes with ID "http://www.w3.org/2002/07/owl#Thing" are removed.</li>
 * <li>Classes with ID "http://www.onto-med.de/ontologies/gfo.owl#Entity" are removed.</li>
 * <li>Classes that are obsolete are removed.</li>
 * </ul>
 * This class requires the passed <tt>Collection</tt> of ontology classes to make use of completely implemented iterators, especially the <tt>remove</tt> method is required.
 * 
 * @author faessler
 * 
 */
public class OntologyClassFilter {
	
	public final Set<String> classRemovalSet = createClassRemovalSet();
	public final Set<String> parentRemovalSet = createParentRemovalSet();
	
	/**
	 * @see OntologyClassFilter
	 * @param classes
	 */
	public void filter(Collection<OntologyClass> classes) {
		for (Iterator<OntologyClass> classIt = classes.iterator(); classIt.hasNext();) {
			OntologyClass cls = classIt.next();
			if (cls.obsolete != null && cls.obsolete) {
				classIt.remove();
				continue;
			}
			if (classRemovalSet.contains(cls.id)) {
				classIt.remove();
				continue;
			}
			if (cls.parents != null && cls.parents.parents != null) {
				for (Iterator<String> parentIt = cls.parents.parents.iterator(); parentIt.hasNext();) {
					String parent = parentIt.next();
					if (parentRemovalSet.contains(parent))
						parentIt.remove();
				}
			}
		}
	}

	private Set<String> createParentRemovalSet() {
		return Collections.emptySet();
	}

	private Set<String> createClassRemovalSet() {
		Set<String> set = new HashSet<>();
		set.add("http://www.w3.org/2002/07/owl#Thing");
		set.add("http://www.onto-med.de/ontologies/gfo.owl#Entity");
		return set;
	}
}
