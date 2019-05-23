package de.julielab.semedico.core.concepts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation.Type;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facetterms.FacetTerm;
import de.julielab.semedico.core.services.interfaces.ITermService;

/**
 * A <tt>Concept</tt> is a conceptual unit that can be set in relationships to
 * other <tt>Concepts</tt>. In this way this implementation exceeds its
 * interface, however it serves different kind of concepts as a common super
 * class.
 * 
 * @author faessler
 * 
 */
public abstract class Concept implements IConcept {

	@JsonIgnore
	protected List<Facet> facets;
	// The Facets this term belongs to for element-checks.
	protected Set<Facet> facetSet;

	// (Long) description of this term.
	protected List<String> descriptions;

	/**
	 * This Term's position in the list of Terms associated with this Term's
	 * facet.
	 */
	protected int facetIndex;

	protected List<String> writingVariants;

	protected String id;

	protected String preferredName;

	/**
	 * For display in facets, for disambiguation (e.g. same gene name but
	 * different species)
	 */
	private String displayName;

	/**
	 * A string that serves as qualification to the term to distinguish it from
	 * other terms that are very similar on first sight. This is currently used
	 * for genes with the same name. The qualifier is the species of the gene.
	 */
	private String[] qualifiers;

	// The type is stored as string because we can have quite arbitrary types,
	// e.g. we have one IS_BROADER_THAN type for
	// each facet. We can't put them all into an enumeration.
	protected Map<String, List<IFacetTermRelation>> incomingRelationships;

	// The type is stored as string because we can have quite arbitrary types,
	// e.g. we have one IS_BROADER_THAN type for
	// each facet. We can't put them all into an enumeration.
	protected Map<String, List<IFacetTermRelation>> outgoingRelationships;

	/**
	 * The content of this field is read directly from the term database.
	 * However, the database must be triggered to create the respective
	 * information. This is done by using the 'updateChildrenInformation'
	 * endpoint of the Neo4j TermManager plugin. This field is then used to
	 * determine whether to render a 'opening' triangle on the frontend for the
	 * respective facet term or not.
	 */
	protected Set<String> childrenInFacets;
	
	protected List<String> synonyms;

	/**
	 * Only used for deserialization via Jackson. We need to read the facet IDs
	 * from the JSON input format to then get the respective facet objects.
	 */
	private List<String> facetIds;

	protected ITermService termService;

	protected boolean isNonDatabaseTerm;
	protected boolean childrenHaveBeenLoaded;

	/**
	 * This constructor is only meant for generic instantiation because there is
	 * a no-args constructor required there.
	 */
	public Concept() {
		this.synonyms = Collections.emptyList();
		this.descriptions = Collections.emptyList();
		this.writingVariants = Collections.emptyList();
		this.incomingRelationships = Collections.emptyMap();
		this.outgoingRelationships = Collections.emptyMap();
		this.childrenInFacets = Collections.emptySet();
		this.childrenHaveBeenLoaded = false;
	}

	public Concept(String id) {
		this();
		this.id = id;
	}

	// Only for deserialization via Gson.
	public Concept(ITermService termService) {
		this();
		this.termService = termService;

	}

	public Concept(String id, ITermService termService) {
		this();
		this.id = id;
		this.termService = termService;
	}

	/**
	 * Constructor for flat terms without a database connection.
	 * 
	 * @param id
	 * @param preferredName
	 */
	public Concept(String id, String preferredName) {
		this(id, (ITermService) null);
		this.preferredName = preferredName;
	}

	public void addFacet(Facet facet) {
		if (null == this.facets)
			this.facets = new ArrayList<>();
		if (null == this.facetSet)
			this.facetSet = new HashSet<>();
		this.facets.add(facet);
		this.facetSet.add(facet);
	}

	protected void loadChildren() {
		if (!childrenInFacets.isEmpty() && outgoingRelationships.isEmpty()
				&& !childrenHaveBeenLoaded) {
			childrenHaveBeenLoaded = true;
			termService.loadChildrenOfTerm(this);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object otherObject) {
		if (!(otherObject instanceof Concept))
			return false;
		Concept otherTerm = (Concept) otherObject;
		return this.id.equals(otherTerm.id);
	}

	public Collection<IConcept> getAllChildren() {
		List<IFacetTermRelation> outgoingRelationships = outgoingRelationships().get(Type.IS_BROADER_THAN.name());
		List<IConcept> children = new ArrayList<>(outgoingRelationships.size());
		for (IFacetTermRelation or : outgoingRelationships) {
			children.add(or.getEndNode());
		}
		return children;
	}

	public Collection<IConcept> getAllChildrenInFacet(String facetId) {
		List<IFacetTermRelation> outgoingRelationships = outgoingRelationships()
				.get(Type.IS_BROADER_THAN.name() + "_" + facetId);
		if (null == outgoingRelationships)
			return Collections.emptyList();
		List<IConcept> children = new ArrayList<>(outgoingRelationships.size());
		for (IFacetTermRelation or : outgoingRelationships) {
			children.add(or.getEndNode());
		}
		return children;
	}

	/**
	 * Returns all parents of this node as a <code>Collection</code>.
	 * 
	 * @return The parents of this node.
	 */
	public Collection<Concept> getAllParents() {
		List<Concept> parents = new ArrayList<>(incomingRelationships.size());
		for (IFacetTermRelation ir : incomingRelationships.get(IFacetTermRelation.Type.IS_BROADER_THAN.name())) {
			parents.add(ir.getStartNode());
		}
		return parents;
	}

	public Concept getChild(int i) {
		try {
			loadChildren();
			return outgoingRelationships().get(Type.IS_BROADER_THAN.name()).get(i).getEndNode();
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"Term " + preferredNameAndIdString() + " does not have a child term on position " + i + ".");
		}
	}

	/**
	 * Returns all available descriptions for this term as a list.
	 */
	public List<String> getDescriptions() {
		return descriptions;
	}

	/**
	 * Returns all available descriptions of this term, concatenated to a single
	 * string.
	 */
	public String getDescription() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < descriptions.size(); i++) {
			sb.append((i + 1) + ") " + descriptions.get(i));
			if (i < descriptions.size() - 1)
				sb.append(" ");
		}
		return sb.toString();
	}

	public int getFacetIndex() {
		return facetIndex;
	}

	public List<Facet> getFacets() {
		return facets;
	}

	public IConcept getFirstChild() {
		try {
			return outgoingRelationships().get(Type.IS_BROADER_THAN.name()).get(0).getEndNode();
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"Term " + preferredNameAndIdString() + " does not have any child terms.");
		}
	}

	public Facet getFirstFacet() {
		for (Facet facet : facets)
			if (facet != Facet.KEYWORD_FACET)
				return facet;
		if (facets == null || facets.isEmpty())
			throw new IllegalStateException("Concept " + this + " does not have any facets.");
		return facets.get(0);
	}

	public IConcept getFirstParent() {
		try {
			return incomingRelationships.get(IFacetTermRelation.Type.IS_BROADER_THAN.name()).get(0).getStartNode();
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"Term " + preferredNameAndIdString() + " does not have any parent terms.");
		}
	}

	public String getId() {
		return id;
	}

	public List<String> getOccurrences() {
		List<String> occurrences = new ArrayList<>(1 + synonyms.size() + writingVariants.size());
		occurrences.add(preferredName);
		occurrences.addAll(synonyms);
		occurrences.addAll(writingVariants);
		return occurrences;
	}

	public IConcept getParent(int i) {
		try {
			return incomingRelationships.get(IFacetTermRelation.Type.IS_BROADER_THAN.name()).get(i).getStartNode();
		} catch (NullPointerException e) {
			throw new IllegalArgumentException(
					"Term " + preferredNameAndIdString() + " does not have a parent term on position " + i + ".");
		}
	}

	public String getPreferredName() {
		return preferredName;
	}

	public IFacetTermRelation getRelationShipWithKey(TermRelationKey key) {
		for (IFacetTermRelation relationship : incomingRelationships
				.get(IFacetTermRelation.Type.IS_BROADER_THAN.name()))
			if (relationship.getKey().equals(key))
				return relationship;
		return null;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public List<String> getWritingVariants() {
		return writingVariants;
	}

	public boolean hasChild(IConcept node) {
		boolean childFound = false;
		List<IFacetTermRelation> broaderThanSuccessors = outgoingRelationships().get(Type.IS_BROADER_THAN.name());
		if (null != broaderThanSuccessors) {
			for (IFacetTermRelation or : broaderThanSuccessors) {
				if (node.getId().equals(or.getEndTermId()))
					childFound = true;
			}
		}
		return childFound;
	}

	public boolean hasChildInFacet(IConcept node, String facetId) {
		boolean childFound = false;
		List<IFacetTermRelation> broaderThanSuccessors = outgoingRelationships()
				.get(Type.IS_BROADER_THAN.name() + "_" + facetId);
		for (IFacetTermRelation or : broaderThanSuccessors) {
			if (node.getId().equals(or.getEndTermId()))
				childFound = true;
		}
		return childFound;
	}

	public boolean hasChildren() {
		return childrenInFacets.size() > 0;
	}

	public boolean hasParent() {
		return incomingRelationships.size() > 0;
	}

	public boolean hasParent(IConcept node) {
		boolean parentFound = false;
		for (IFacetTermRelation or : incomingRelationships.get(IFacetTermRelation.Type.IS_BROADER_THAN.name())) {
			if (node.equals(or.getStartNode()))
				parentFound = true;
		}
		return parentFound;
	}

	public boolean hasParent(IConcept node, String facetId) {
		boolean parentFound = false;
		for (IFacetTermRelation or : incomingRelationships
				.get(IFacetTermRelation.Type.IS_BROADER_THAN.name() + "_" + facetId)) {
			if (node.equals(or.getStartNode()))
				parentFound = true;
		}
		return parentFound;
	}

	public boolean isContainedInFacet(Facet otherFacet) {
		if (null == facetSet)
			facetSet = new HashSet<>(facets);
		return facetSet.contains(otherFacet);
	}

	private String preferredNameAndIdString() {
		return "\"" + preferredName + "\" (ID: \"" + id + "\"";
	}

	public void setDescription(List<String> descriptions) {
		this.descriptions = descriptions;
	}

	public void setFacets(List<Facet> facets) {
		this.facets = facets;
		this.facetSet = new HashSet<>(facets);
	}

	public void addOutgoingRelationship(IFacetTermRelation outgoingRelationship) {
		List<IFacetTermRelation> relationshipList = this.outgoingRelationships.get(outgoingRelationship.getType());
		if (null == relationshipList) {
			if (this.outgoingRelationships.isEmpty())
				// Create a new map because the empty map is most likely the
				// Collections.emptyMap() constant
				this.outgoingRelationships = new HashMap<>();
			relationshipList = new ArrayList<>();
			this.outgoingRelationships.put(outgoingRelationship.getType(), relationshipList);
		}
		relationshipList.add(outgoingRelationship);
	}

	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	public void setSynonyms(List<String> synonyms) {
		if (null != synonyms)
			this.synonyms = synonyms;
	}

	public void setWritingVariants(List<String> writingVariants) {
		if (null != writingVariants)
			this.writingVariants = writingVariants;
	}

	@Override
	public String toString() {
		return "IConcept [id=" + id + ", preferredName=" + preferredName + "]";
	}

	public Collection<String> getAllChildIds() {
		List<IFacetTermRelation> outgoingRelationships = outgoingRelationships().get(Type.IS_BROADER_THAN.name());
		Set<String> children = new HashSet<>(outgoingRelationships.size());
		for (IFacetTermRelation or : outgoingRelationships) {
			children.add(or.getKey().getEndId());
		}
		return children;
	}

	public Collection<String> getAllChildIdsInFacet(String facetId, boolean filterNonDbTerms) {
		List<IFacetTermRelation> outgoingRelationships = outgoingRelationships()
				.get(Type.IS_BROADER_THAN.name() + "_" + facetId);
		if (null == outgoingRelationships)
			return Collections.emptyList();
		Set<String> children = new HashSet<>(outgoingRelationships.size());
		for (IFacetTermRelation or : outgoingRelationships) {
			String childId = or.getKey().getEndId();
			// The idea is: If were are here, outgoingRelationShips() has
			// already been called and DB-children have been
			// loaded. Thus, if the child is currently not loaded, it can't be a
			// database term (or isn't a child there,
			// but this would mean inconsistency of loaded data). If the term
			// HAS been loaded, we just ask it whether it
			// came from the database or not. No extra-loading of terms is
			// necessary.
			if (filterNonDbTerms) {
				Concept childTerm = (Concept) termService.getTermIfCached(childId);
				if (null != childTerm && childTerm.isNonDatabaseTerm)
					continue;
			}
			children.add(childId);
		}
		return children;
	}

	public boolean hasChildrenInFacet(String facetId) {
		// if (null == childrenInFacets)
		// return false;
		return childrenInFacets.contains(facetId);
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public void setId(String id) {
		if (!StringUtils.isBlank(this.id) && !this.id.equals(id))
			throw new IllegalAccessError("The term with ID " + this.id + " cannot be set the new ID " + id
					+ ". Once set, a term's ID cannot be changed.");
		this.id = id;

	}

	public Iterator<Concept> childIterator(final String facetId) {
		List<IFacetTermRelation> children = outgoingRelationships().get(Type.IS_BROADER_THAN.name() + "_" + facetId);
		if (null == children)
			return Collections.emptyIterator();
		final Iterator<IFacetTermRelation> relIt = children.iterator();
		return new Iterator<Concept>() {

			private IFacetTermRelation nextRel = null;

			@Override
			public boolean hasNext() {
				while (nextRel == null && relIt.hasNext()) {
					IFacetTermRelation relation = relIt.next();
					// String reltype = relation.getType();
					// if (reltype.endsWith(facetId)) {
					nextRel = relation;
					return true;
					// }
				}
				return false;
			}

			@Override
			public Concept next() {
				Concept nextChild = null;
				if (null == nextRel)
					hasNext();
				if (null != nextRel)
					nextChild = nextRel.getEndNode();
				nextRel = null;
				return nextChild;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}

		};
	}

	private Map<String, List<IFacetTermRelation>> outgoingRelationships() {
		loadChildren();
		return outgoingRelationships;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	/**
	 * Used mainly for deserialization from the term database. Returns the facet
	 * IDs as they are stored in the term database.
	 * 
	 * @return
	 */
	@JsonProperty("facets")
	public List<String> getFacetIds() {
		return facetIds;
	}

	/**
	 * This method should not be used manually. The setter is used by the
	 * deserialization library to create terms from JSON strings, received from
	 * the term database.
	 * 
	 * @param facetIds
	 */
	public void setFacetIds(List<String> facetIds) {
		this.facetIds = facetIds;
	}

	@Override
	public boolean isNonDatabaseTerm() {
		return isNonDatabaseTerm;
	}

	@Override
	public void setNonDatabaseTerm(boolean isNonDatabaseTerm) {
		this.isNonDatabaseTerm = isNonDatabaseTerm;
	}

	public String getDisplayName() {
		return StringUtils.isBlank(displayName) ? preferredName : displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String[] getQualifiers() {
		return qualifiers;
	}

	public void setQualifiers(String[] qualifiers) {
		this.qualifiers = qualifiers;
	}

}
