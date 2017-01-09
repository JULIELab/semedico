package de.julielab.semedico.core.facetterms;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.Concept.EventType;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class KeywordTerm implements IConcept {

	@Override
	public String toString() {
		return "KeywordTerm [id=" + id + ", preferredName=" + preferredName + "]";
	}

	private String id;
	private String preferredName;

	public KeywordTerm() {}
	
	public KeywordTerm(String id, String preferredName) {
		this.id = id;
		this.preferredName = preferredName;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getPreferredName() {
		return preferredName;
	}

	// @Override
	// public IConcept getFirstParent() {
	// return null;
	// }
	//
	// @Override
	// public IConcept getParent(int i) {
	// return null;
	// }
	//
	// @Override
	// public int getNumberOfParents() {
	// return 0;
	// }
	//
	// @Override
	// public boolean hasParent() {
	// return false;
	// }
	//
	// @Override
	// public boolean hasParent(IConcept node) {
	// return false;
	// }
	//
	// @Override
	// public IConcept getFirstChild() {
	// return null;
	// }
	//
	// @Override
	// public Collection<IConcept> getAllChildren() {
	// return null;
	// }
	//
	// @Override
	// public IConcept getChild(int i) {
	// return null;
	// }
	//
	// @Override
	// public boolean hasChild(IConcept node) {
	// return false;
	// }
	//
	// @Override
	// public int getNumberOfChildren() {
	// return 0;
	// }
	//
	// @Override
	// public boolean hasChildrenInFacet(String facetId) {
	// return false;
	// }

	@Override
	public void addFacet(Facet facet) {
		throw new IllegalAccessError("Keyword terms cannot be added a facet.");
	}

	@Override
	public void setDescription(List<String> description) {
		throw new IllegalAccessError("Keyword terms cannot be set a description.");
	}

	@Override
	public Facet getFirstFacet() {
		return Facet.KEYWORD_FACET;
	}

	@Override
	public Collection<String> getIndexNames() {
		return Lists.newArrayList(IIndexInformationService.MEDLINE_SEARCH_FIELDS);
	}

	@Override
	public List<String> getSynonyms() {
		return null;
	}

	@Override
	public List<String> getDescriptions() {
		return null;
	}

	@Override
	public String getDescription() {
		return null;
	}

	// @Override
	// public void setFacetIndex(int size) {
	// // TODO what does this anyway?
	// }
	//
	// @Override
	// public boolean hasChildren() {
	// return false;
	// }

	@Override
	public List<Facet> getFacets() {
		return Lists.newArrayList(Facet.KEYWORD_FACET);
	}

	@Override
	public boolean isContainedInFacet(Facet facet) {
		return facet.equals(Facet.KEYWORD_FACET);
	}

	// @Override
	// public void setWritingVariants(List<String> writingVariants) {
	// throw new IllegalAccessError("Keyword terms cannot be set writing variants.");
	// }
	//
	// @Override
	// public List<String> getWritingVariants() {
	// return null;
	// }

	@Override
	public List<String> getOccurrences() {
		return null;
	}

	// @Override
	// public void setSourceIds(List<String> sourceIds) {
	// throw new IllegalAccessError("Keyword terms cannot be set a source IDs.");
	// }
	//
	// @Override
	// public void setOriginalId(String originalId) {
	// throw new IllegalAccessError("Keyword terms cannot be set an original ID.");
	// }

	@Override
	public void setPreferredName(String preferredName) {
		this.preferredName = preferredName;
	}

	// @Override
	// public void setSynonyms(List<String> synonyms) {
	// throw new IllegalAccessError("Keyword terms cannot be set a synonyms.");
	// }

	@Override
	public void setFacets(List<Facet> facets) {
		throw new IllegalAccessError("Keyword terms cannot be set facets.");
	}

	// @Override
	// public void setIncomingRelationships(List<IFacetTermRelation> incomingRelationships) {
	// throw new IllegalAccessError("Keyword terms cannot be set relationships.");
	// }
	//
	// @Override
	// public void setOutgoingRelationships(List<IFacetTermRelation> outgoingRelationships) {
	// throw new IllegalAccessError("Keyword terms cannot be set a relationships.");
	// }
	//
	// @Override
	// public IFacetTermRelation getRelationShipWithKey(TermRelationKey key) {
	// return null;
	// }
	//
	// @Override
	// public List<String> getSourceIds() {
	// return null;
	// }
	//
	// @Override
	// public String getOriginalId() {
	// return null;
	// }
	//
	// @Override
	// public Collection<String> getAllChildIds() {
	// return null;
	// }
	//
	// @Override
	// public void setTermService(ITermService termService) {
	// throw new IllegalAccessError("Keyword terms cannot be set a term service.");
	// }

	@Override
	public void setId(String id) {
		this.id = id;
	}

	@Override
	public boolean isEventTrigger() {
		return false;
	}

	@Override
	public void setIsEventTrigger(boolean isEventTrigger) {
		throw new IllegalAccessError("Keyword terms cannot be set to be an event trigger.");
	}

	@Override
	public EventType getEventType() {
		return EventType.NONE;
	}

	/**
	 * @deprecated Use {@link #getConceptType()}.
	 */
	@Deprecated
	@Override
	public boolean isKeyword() {
		return true;
	}

	/**
	 * @deprecated Use {@link #getConceptType()}.
	 */
	@Deprecated
	@Override
	public boolean isAggregate() {
		return false;
	}

	@Override
	public boolean isNonDatabaseTerm() {
		return true;
	}

	@Override
	public void setNonDatabaseTerm(boolean isNonDatabaseTerm) {
		throw new IllegalAccessError("Keyword terms cannot be set to be a database term.");
	}

	@Override
	public String getDisplayName() {
		return preferredName;
	}

	@Override
	public String[] getQualifiers() {
		return null;
	}

	@Override
	public ConceptType getConceptType() {
		return ConceptType.KEYWORD;
	}

	@Override
	public boolean isCoreTerm() {
		return false;
	}

	/**
	 * Keywords do not have a special function in regards to events.
	 */
	@Override
	public boolean isEventFunctional() {
		return false;
	}
}
