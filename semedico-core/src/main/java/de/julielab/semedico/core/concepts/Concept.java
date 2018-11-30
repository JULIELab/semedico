package de.julielab.semedico.core.concepts;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.julielab.semedico.core.entities.ConceptRelationKey;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation.Type;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A <tt>Concept</tt> is a conceptual unit that can be set in relationships to
 * other <tt>Concepts</tt>. In this way this implementation exceeds its
 * interface, however it serves different kind of concepts as a common super
 * class.
 *
 * @author faessler
 */
// TODO this class effectively implements IHierarchicalConcept but doesn't to do
// so actually because of non-fitting subclasses. This is awkward and should be
// changed.
public abstract class Concept implements IConcept, DescribableConcept {

    @JsonIgnore
    protected List<Facet> facets;
    // The Facets this term belongs to for element-checks.
    protected Set<Facet> facetSet;

    // (Long) description of this term.
    protected List<String> descriptions;

    protected List<String> writingVariants;

    protected String id;

    protected String preferredName;
    // The type is stored as string because we can have quite arbitrary types,
    // e.g. we have one IS_BROADER_THAN type for
    // each facet. We can't put them all into an enumeration.
    protected Map<String, List<IConceptRelation>> incomingRelationships;
    // The type is stored as string because we can have quite arbitrary types,
    // e.g. we have one IS_BROADER_THAN type for
    // each facet. We can't put them all into an enumeration.
    protected Map<String, List<IConceptRelation>> outgoingRelationships;
    /**
     * The content of this field is read directly from the term database.
     * However, the database must be triggered to create the respective
     * information. This is done by using the 'updateChildrenInformation'
     * endpoint of the Neo4j ConceptManager plugin. This field is then used to
     * determine whether to render a 'opening' triangle on the frontend for the
     * respective facet term or not.
     */
    protected Set<String> childrenInFacets;
    protected List<String> synonyms;
    protected boolean isNonDatabaseTerm;
    protected boolean childrenHaveBeenLoaded;
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
    private boolean isEventTrigger;
    /**
     * Only used for deserialization via Jackson. We need to read the facet IDs
     * from the JSON input format to then get the respective facet objects.
     */
    private List<String> facetIds;
    /**
     * Only used for deserialization. Determines the number of arguments the
     * event, represented by this term, if any, can take. Depending on context
     * this number may vary, so multiple number are possible.
     *
     * @deprecated we don't care about event valence any more
     */
    @Deprecated
    private Set<Integer> eventValence;

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
        this.id = id;
    }

    public Concept(String id, ITermService termService) {
        this.id = id;
    }

    public Concept(ConceptDescription description) {
        initializeFromDescription(description);
    }

    @Override
    public void initializeFromDescription(ConceptDescription description) {
        this.id = description.getId();
        this.preferredName = description.getPreferredName();
        this.displayName = description.getDisplayName();
        this.synonyms = Arrays.asList(description.getSynonyms());
        this.writingVariants = Arrays.asList(description.getWritingVariants());
        this.descriptions = Arrays.asList(description.getDescriptions());
        this.childrenInFacets = new HashSet<>(Arrays.asList(description.getChildrenInFacets()));
        this.facetIds = Arrays.asList(description.getFacetIds());
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

    public Set<String> getChildrenInFacets() {
        return childrenInFacets;
    }

    public void addFacet(Facet facet) {
        if (null == this.facets)
            this.facets = new ArrayList<>();
        if (null == this.facetSet)
            this.facetSet = new HashSet<>();
        this.facets.add(facet);
        this.facetSet.add(facet);
    }

    @Override
    public boolean isEventTrigger() {
        return isEventTrigger || (null != eventValence && eventValence.size() > 0);
    }

    @Override
    public void setIsEventTrigger(boolean isEventTrigger) {
        this.isEventTrigger = isEventTrigger;

    }

    /**
     * Returns an iterator over ALL terms to which this term has a
     * IS_BROADER_THAN relationship, ignoring facet borders.
     *
     * @return
     */
    public Iterator<IConcept> childIterator() {
        return getAllChildren().iterator();
    }

    public Iterator<IConcept> childIteratorInFacet(String facetId) {
        return getAllChildrenInFacet(facetId).iterator();
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
        List<IConceptRelation> outgoingRelationships = outgoingRelationships().get(Type.IS_BROADER_THAN.name());
        List<IConcept> children = new ArrayList<>(outgoingRelationships.size());
        for (IConceptRelation or : outgoingRelationships) {
            children.add(or.getEndNode());
        }
        return children;
    }

    public Collection<IConcept> getAllChildrenInFacet(String facetId) {
        List<IConceptRelation> outgoingRelationships = outgoingRelationships()
                .get(Type.IS_BROADER_THAN.name() + "_" + facetId);
        if (null == outgoingRelationships)
            return Collections.emptyList();
        List<IConcept> children = new ArrayList<>(outgoingRelationships.size());
        for (IConceptRelation or : outgoingRelationships) {
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
        for (IConceptRelation ir : incomingRelationships.get(IConceptRelation.Type.IS_BROADER_THAN.name())) {
            parents.add(ir.getStartNode());
        }
        return parents;
    }

    public Concept getChild(int i) {
        try {
            return outgoingRelationships().get(Type.IS_BROADER_THAN.name()).get(i).getEndNode();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "Concept " + preferredNameAndIdString() + " does not have a child term on position " + i + ".");
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

    public void setDescription(List<String> descriptions) {
        this.descriptions = descriptions;
    }

    public List<Facet> getFacets() {
        return facets;
    }

    public void setFacets(List<Facet> facets) {
        this.facets = facets;
        this.facetSet = new HashSet<>(facets);
    }

    public IConcept getFirstChild() {
        try {
            return outgoingRelationships().get(Type.IS_BROADER_THAN.name()).get(0).getEndNode();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "Concept " + preferredNameAndIdString() + " does not have any child terms.");
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
            return incomingRelationships.get(IConceptRelation.Type.IS_BROADER_THAN.name()).get(0).getStartNode();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "Concept " + preferredNameAndIdString() + " does not have any parent terms.");
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        if (!StringUtils.isBlank(this.id) && !this.id.equals(id))
            throw new IllegalAccessError("The term with ID " + this.id + " cannot be set the new ID " + id
                    + ". Once set, a term's ID cannot be changed.");
        this.id = id;

    }

    @Deprecated
    public Collection<String> getIndexNames() {
        return null;
    }

    public int getNumberOfChildren() {
        List<IConceptRelation> list = outgoingRelationships().get(IConceptRelation.Type.IS_BROADER_THAN.name());
        return null != list ? list.size() : 0;
    }

    public int getNumberOfParents() {
        int numParents = 0;
        for (String relType : incomingRelationships.keySet())
            numParents += incomingRelationships.get(relType).size();
        return numParents;
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
            return incomingRelationships.get(IConceptRelation.Type.IS_BROADER_THAN.name()).get(i).getStartNode();
        } catch (NullPointerException e) {
            throw new IllegalArgumentException(
                    "Concept " + preferredNameAndIdString() + " does not have a parent term on position " + i + ".");
        }
    }

    public String getPreferredName() {
        return preferredName;
    }

    public void setPreferredName(String preferredName) {
        this.preferredName = preferredName;
    }

    public IConceptRelation getRelationShipWithKey(ConceptRelationKey key) {
        for (IConceptRelation relationship : incomingRelationships
                .get(IConceptRelation.Type.IS_BROADER_THAN.name()))
            if (relationship.getKey().equals(key))
                return relationship;
        return null;
    }

    public List<String> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        if (null != synonyms)
            this.synonyms = synonyms;
    }

    public List<String> getWritingVariants() {
        return writingVariants;
    }

    public void setWritingVariants(List<String> writingVariants) {
        if (null != writingVariants)
            this.writingVariants = writingVariants;
    }

    public boolean hasChild(IConcept node) {
        boolean childFound = false;
        List<IConceptRelation> broaderThanSuccessors = outgoingRelationships().get(Type.IS_BROADER_THAN.name());
        if (null != broaderThanSuccessors) {
            for (IConceptRelation or : broaderThanSuccessors) {
                if (node.getId().equals(or.getEndTermId()))
                    childFound = true;
            }
        }
        return childFound;
    }

    public boolean hasChildInFacet(IConcept node, String facetId) {
        boolean childFound = false;
        List<IConceptRelation> broaderThanSuccessors = outgoingRelationships()
                .get(Type.IS_BROADER_THAN.name() + "_" + facetId);
        for (IConceptRelation or : broaderThanSuccessors) {
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
        for (IConceptRelation or : incomingRelationships.get(IConceptRelation.Type.IS_BROADER_THAN.name())) {
            if (node.equals(or.getStartNode()))
                parentFound = true;
        }
        return parentFound;
    }

    public boolean hasParent(IConcept node, String facetId) {
        boolean parentFound = false;
        for (IConceptRelation or : incomingRelationships
                .get(IConceptRelation.Type.IS_BROADER_THAN.name() + "_" + facetId)) {
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

    public void setIncomingRelationships(Map<String, List<IConceptRelation>> incomingRelationships) {
        this.incomingRelationships = incomingRelationships;
    }

    public void addIncomingRelationship(IConceptRelation incomingRelationship) {
        if (incomingRelationships.isEmpty())
            incomingRelationships = new HashMap<>();
        List<IConceptRelation> relationshipList = this.incomingRelationships.get(incomingRelationship.getType());
        if (null == relationshipList) {
            relationshipList = new ArrayList<>();
            this.incomingRelationships.put(incomingRelationship.getType(), relationshipList);
        }
        relationshipList.add(incomingRelationship);
    }

    public void setOutgoingRelationships(Map<String, List<IConceptRelation>> outgoingRelationships) {
        this.outgoingRelationships = outgoingRelationships;

    }

    public void addOutgoingRelationship(IConceptRelation outgoingRelationship) {
        List<IConceptRelation> relationshipList = this.outgoingRelationships.get(outgoingRelationship.getType());
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

    @Override
    public String toString() {
        return "IConcept [id=" + id + ", preferredName=" + preferredName + "]";
    }

    public Collection<String> getAllChildIds() {
        List<IConceptRelation> outgoingRelationships = outgoingRelationships().get(Type.IS_BROADER_THAN.name());
        Set<String> children = new HashSet<>(outgoingRelationships.size());
        for (IConceptRelation or : outgoingRelationships) {
            children.add(or.getKey().getEndId());
        }
        return children;
    }

    public Collection<String> getAllChildIdsInFacet(String facetId, boolean filterNonDbTerms) {
        List<IConceptRelation> outgoingRelationships = outgoingRelationships()
                .get(Type.IS_BROADER_THAN.name() + "_" + facetId);
        if (null == outgoingRelationships)
            return Collections.emptyList();

        Stream<Concept> children = outgoingRelationships.stream().map(IConceptRelation::getEndNode);
        if (filterNonDbTerms)
            children = children.filter(child -> !child.isNonDatabaseTerm);
        return children.map(IConcept::getId).collect(Collectors.toList());
    }

    public boolean hasChildrenInFacet(String facetId) {
        return childrenInFacets.contains(facetId);
    }

    public Iterator<Concept> childIterator(final String facetId) {
        List<IConceptRelation> children = outgoingRelationships().get(Type.IS_BROADER_THAN.name() + "_" + facetId);
        if (null == children)
            return Collections.emptyIterator();
        final Iterator<IConceptRelation> relIt = children.iterator();
        return new Iterator<Concept>() {

            private IConceptRelation nextRel = null;

            @Override
            public boolean hasNext() {
                while (nextRel == null && relIt.hasNext()) {
                    IConceptRelation relation = relIt.next();
                    nextRel = relation;
                    return true;
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

    protected Map<String, List<IConceptRelation>> outgoingRelationships() {
        return outgoingRelationships;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public void addChildrenFacet(String facetId) {
        // We should be able to use this field without causing damage because
        // the database contents have already been
        // read when we are able to manipulate it.
        if (childrenInFacets.isEmpty())
            childrenInFacets = new HashSet<>();
        childrenInFacets.add(facetId);
    }

    public List<String> getFacetIds() {
        return facetIds;
    }

    public void setFacetIds(List<String> facetIds) {
        this.facetIds = facetIds;
    }

    /**
     * Only used for deserialization. Determines the number of arguments the
     * event, represented by this term, if any, can take. Depending on context
     * this number may vary, so multiple number are possible. * @deprecated we
     * don't care about event valence any more
     */
    @Deprecated
    public Set<Integer> getEventValence() {
        return eventValence;
    }

    /**
     * Only used for deserialization. Sets the number of arguments the event,
     * represented by this term, if any, can take. Depending on context this
     * number may vary, so multiple number are possible.
     *
     * @deprecated we don't care about event valence any more
     */
    @Deprecated
    public void setEventValence(Set<Integer> eventValence) {
        this.eventValence = eventValence;
    }

    @Override
    public boolean isNonDatabaseConcept() {
        return isNonDatabaseTerm;
    }

    @Override
    public void setNonDatabaseConcept(boolean isNonDatabaseTerm) {
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
