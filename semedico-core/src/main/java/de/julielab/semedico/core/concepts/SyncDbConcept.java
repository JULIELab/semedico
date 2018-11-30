package de.julielab.semedico.core.concepts;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.julielab.semedico.core.entities.ConceptRelationKey;
import de.julielab.semedico.core.concepts.interfaces.IConceptRelation;
import de.julielab.semedico.core.concepts.interfaces.LatchSynchronized;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.LatchSynchronizer;

public class SyncDbConcept extends DatabaseConcept implements LatchSynchronized {

	@Override
	public String toString() {
		return "SyncDbConcept [getPreferredName=" + getPreferredName() + ", getId=" + getId() + "]";
	}

	private LatchSynchronizer synchronizer;

	public SyncDbConcept() {
		super();
		this.synchronizer = new LatchSynchronizer();
	}

	public SyncDbConcept(String id, ITermService termService) {
		this(id, termService, true);
	}

	public SyncDbConcept(String id, ITermService termService, boolean doSynchronize) {
		this.id = id;
		this.conceptService = termService;
		if (doSynchronize)
			this.synchronizer = new LatchSynchronizer();
	}

	@Override
	public void addFacet(Facet facet) {
		synchronize();
		super.addFacet(facet);
	}

	private void synchronize() {
		if (null != synchronizer)
			synchronizer.synchronize();
	}

	@Override
	public Collection<IConcept> getAllChildren() {
		synchronize();
		return super.getAllChildren();
	}

	@Override
	public Collection<Concept> getAllParents() {
		synchronize();
		return super.getAllParents();
	}

	@Override
	public Concept getChild(int i) {
		synchronize();
		return super.getChild(i);
	}

	@Override
	public List<String> getDescriptions() {
		synchronize();
		return super.getDescriptions();
	}

	@Override
	public String getDescription() {
		synchronize();
		return super.getDescription();
	}

	@Override
	public List<Facet> getFacets() {
		synchronize();
		return super.getFacets();
	}

	@Override
	public IConcept getFirstChild() {
		synchronize();
		return super.getFirstChild();
	}

	@Override
	public Facet getFirstFacet() {
		synchronize();
		return super.getFirstFacet();
	}

	@Override
	public IConcept getFirstParent() {
		synchronize();
		return super.getFirstParent();
	}

	@Override
	public int getNumberOfChildren() {
		synchronize();
		return super.getNumberOfChildren();
	}

	@Override
	public int getNumberOfParents() {
		synchronize();
		return super.getNumberOfParents();
	}

	@Override
	public String getOriginalId() {
		synchronize();
		return super.getOriginalId();
	}

	@Override
	public IConcept getParent(int i) {
		synchronize();
		return super.getParent(i);
	}

	@Override
	public String getPreferredName() {
		synchronize();
		return super.getPreferredName();
	}

	@Override
	public IConceptRelation getRelationShipWithKey(ConceptRelationKey key) {
		synchronize();
		return super.getRelationShipWithKey(key);
	}

	@Override
	public List<String> getSourceIds() {
		synchronize();
		return super.getSourceIds();
	}

	@Override
	public List<String> getSynonyms() {
		synchronize();
		return super.getSynonyms();
	}

	@Override
	public List<String> getWritingVariants() {
		synchronize();
		return super.getWritingVariants();
	}

	@Override
	public boolean hasChild(IConcept node) {
		synchronize();
		return super.hasChild(node);
	}

	@Override
	public boolean hasChildren() {
		synchronize();
		return super.hasChildren();
	}

	@Override
	public boolean hasParent() {
		synchronize();
		return super.hasParent();
	}

	@Override
	public boolean hasParent(IConcept node) {
		synchronize();
		return super.hasParent(node);
	}

	@Override
	public boolean isContainedInFacet(Facet otherFacet) {
		synchronize();
		return super.isContainedInFacet(otherFacet);
	}

	@Override
	public boolean hasChildrenInFacet(String facetId) {
		synchronize();
		return super.hasChildrenInFacet(facetId);
	}

	@Override
	public Iterator<Concept> childIterator(String facetId) {
		synchronize();
		return super.childIterator(facetId);
	}

	@Override
	public void setSynchronizeLatch(CountDownLatch latch) {
		synchronizer.setSynchronizeLatch(latch);
	}

	@Override
	public boolean isEventTrigger() {
		synchronize();
		return super.isEventTrigger();
	}

	@Override
	public boolean isKeyword() {
		synchronize();
		return super.isKeyword();
	}

}
