package de.julielab.semedico.core.facetterms;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import de.julielab.semedico.core.TermRelationKey;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IFacetTermRelation;
import de.julielab.semedico.core.concepts.interfaces.LatchSynchronized;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.util.LatchSynchronizer;

public class SyncFacetTerm extends FacetTerm implements LatchSynchronized {

	@Override
	public String toString() {
		return "SyncFacetTerm [getPreferredName=" + getPreferredName() + ", getId=" + getId() + "]";
	}

	private LatchSynchronizer synchronizer;

	public SyncFacetTerm() {
		super();
		this.synchronizer = new LatchSynchronizer();
	}

	public SyncFacetTerm(String id, ITermService termService) {
		this(id, termService, true);
	}

	public SyncFacetTerm(String id, ITermService termService, boolean doSynchronize) {
		this.id = id;
		this.termService = termService;
		if (doSynchronize) {
			this.synchronizer = new LatchSynchronizer();
		}
	}

	@Override
	public void addFacet(Facet facet) {
		synchronize();
		super.addFacet(facet);
	}

	private void synchronize() {
		if (null != synchronizer) {
			synchronizer.synchronize();
		}
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
	public int getFacetIndex() {
		synchronize();
		return super.getFacetIndex();
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
	public IFacetTermRelation getRelationShipWithKey(TermRelationKey key) {
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
	public void setSynchronizeLatch(CountDownLatch latch) {
		synchronizer.setSynchronizeLatch(latch);
	}

}
