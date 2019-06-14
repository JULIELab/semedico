package de.julielab.semedico.components;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.pages.Index;
import org.apache.tapestry5.PersistenceConstants;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.internal.services.StringValueEncoder;
import org.apache.tapestry5.ioc.Messages;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.slf4j.Logger;

import javax.naming.OperationNotSupportedException;
import java.util.*;

@Import(stylesheet = { "context:css/semedico-queryPanel.css", "context:css/semedico-filterPanel.css" })
public class QueryPanel {

	@InjectPage
	private Index index;

	// We take the parts of the session state as parameters. That's easier then to get them out of the session state all
	// the time.
	@Parameter(required = true)
	@Property
	private SearchState searchState;

	@Parameter(required = true)
	private UserInterfaceState uiState;

	@Property
	@Parameter
	private Multimap<String, IHierarchicalConcept> spellingCorrectedQueryTerms;

	@Property
	@Parameter
	private Multimap<String, String> spellingCorrections;

	@Property
	// Used to iterate over all mapped terms
	private Long conceptNodeId;

	@Property
	private int queryTermIndex;

	@Property
	@Persist(PersistenceConstants.FLASH)
	private Long nodeToDisambiguate;

	@Property
	@Deprecated
	private Concept pathItem;

	@Property
	@Deprecated
	private int pathItemIndex;

	@Property
	private String correctedTerm;

	@Property
	private int correctedTermIndex;

	@Property
	@Deprecated
	private boolean hasFilter = false;

	// @Property
	// @Persist
	// private boolean showBTermPanel;

	@Inject
	private Logger logger;

	@Inject
	private IConceptService termService;

	@Persist
	ParseTree parseTree;

	public void setupRender() {
		if (searchState.isNewSearch())
			nodeToDisambiguate = null;
		parseTree = searchState.getSemedicoQuery();
	}

	public boolean isTermCorrected() {
		// if (nodeId == null || spellingCorrections == null)
		// return false;
		//
		// return spellingCorrections.containsKey(nodeId);
		return false;
	}

	public Collection getCorrectedTerms() {
		// if (nodeId == null || spellingCorrections == null)
		// return null;
		//
		// Collection correctedTerms = spellingCorrections.get(nodeId);
		// return correctedTerms;
		return null;
	}

	public boolean isMultipleCorrectedTerms() {
		// if (nodeId == null || spellingCorrections == null)
		// return false;
		//
		// return getCorrectedTerms().size() > 1;
		return false;
	}

	public boolean isTermAmbigue() {
		if (conceptNodeId == null)
			return false;

		Node node = getParseNode();
		if (null != node && node.getClass().equals(TextNode.class)) {
			TextNode textNode = (TextNode) node;
			return textNode.isAmbiguous();
		}
		// TODO handle EventNodes
		// comment on that: event nodes will most probably get some kind of 'internal disambiguation' on their
		// arguments. If this is done and working, delete this TODO
		return false;
	}

	public boolean isTermSelectedForDisambiguation() {
		return conceptNodeId != null && nodeToDisambiguate != null && conceptNodeId.equals(nodeToDisambiguate);
	}

	// @Inject
	// private Request request;
	// @InjectComponent
	// private Zone disambiguationZone;

	// public Object onRefine(Long nodeId) {
	// nodeToDisambiguate = nodeId;
	// conceptNodeId = nodeId;
	// return request.isXHR() ? disambiguationZone.getBody() : null;
	// }

	// public void doQueryChanged(String queryTerm) throws Exception {
	// if (queryTerm == null)
	// return;
	// searchState.removeTerm(queryTerm);
	// }

	public Node getParseNode() {
		Node node = parseTree.getNode(conceptNodeId);
		return node;
	}

	public String getParseNodeText() {
		return getParseNode().getText();
	}

	@Inject
	private IFacetService facetService;

	/**
	 * Returns the first mapped term of the current query string which is iterated over in the template.
	 * 
	 * @return The mapped term of the current query string.
	 */
	public IConcept getFirstTermOfParseNode() {
		// TODO seems a bit arbitrary. Is it possible that there are multiple
		// FacetTerms for queryTerm? Should this be so? Is it an adequate
		// solution to just take the first?
		Node node = getParseNode();
		if (null == node)
			return null;
		logger.debug("Current parse tree node: " + node.toString(Serialization.NODE_TEXT));
		TextNode textNode = node.asTextNode();
		if (null != textNode) {
			List<? extends IConcept> terms = textNode.getConcepts();
			return terms.get(0);
		}
		// else if (node.getClass().equals(EventNode.class)) {
		// EventNode eventNode = (EventNode) node;
		// // TODO think of a better way, perhaps a "DisplayFacetTerm" or something
		// FacetTerm eventTerm =
		// new FacetTerm(eventNode.getEventTypes().get(0).getId(), eventNode.toString(SERIALIZATION.TEXT));
		// eventTerm.setFacets(eventNode.getEventTypes().get(0).getFacets());
		// // searchState.getQueryTermFacetMap().put(eventTerm, eventTerm.getFirstFacet());
		// return eventTerm;
		// }
		return null;
	}

	@Deprecated
	public String getMappedTermClass() {
		IConcept mappedTerm = getFirstTermOfParseNode();
		if (mappedTerm != null) {
			Facet facet = getMappedTermFacet();
			String cssId = facet.getCssId();
			String termClass = cssId + " filterBox primaryFacetStyle";
			return termClass;
		}
//		else {
//			EventNode eventNode = getCurrentEventNode();
//			if (null != eventNode) {
//				IConcept eventType = eventNode.getEventTypes().get(0);
//				Facet eventFacet = facetService.getInducedFacet(eventType.getId(), FacetLabels.General.EVENTS);
//				String cssId = eventFacet.getCssId();
//				String termClass = cssId + " filterBox primaryFacetStyle";
//				return termClass;
//			}
//		}
		return null;
	}

	public Facet getMappedTermFacet() {
		IConcept mappedTerm = getFirstTermOfParseNode();
		Node parseNode = getParseNode();
		if (null == parseNode)
			return null;
		if (parseNode.getClass().equals(TextNode.class))
			return ((TextNode) parseNode).getMappedFacet(mappedTerm);
//		if (parseNode.getClass().equals(EventNode.class)) {
//			EventNode eventNode = (EventNode) parseNode;
//			IConcept eventType = eventNode.getEventTypes().get(0);
//			return facetService.getInducedFacet(eventType.getId(), FacetLabels.General.EVENTS);
//		}
		// Map<IConcept, Facet> queryTermFacetMap = searchState.getQueryTermFacetMap();
		// Facet facet = queryTermFacetMap.get(mappedTerm);
		logger.warn("Got no facet mapping for term {}, returning first facet.", mappedTerm);
		return mappedTerm.getFirstFacet();
	}

	// private Map<String, IFacetTerm> getUnambigousQueryTerms() {
	// Map<String, IFacetTerm> unambigousTerms = new HashMap<String, IFacetTerm>();
	//
	// for (String queryTerm : queryTerms.keySet()) {
	// Collection<IFacetTerm> terms = queryTerms.get(queryTerm);
	// if (terms.size() == 1)
	// unambigousTerms.put(queryTerm, terms.iterator().next());
	// }
	//
	// return unambigousTerms;
	// }

	public void onDrillUp(Integer nodeIdToDrillUp, int pathItemIndex) throws Exception {

		if (nodeIdToDrillUp == null)
			return;

		Map<Facet, UIFacet> facetConfigurations = uiState.getUIFacets();
		// We boldly do a cast here; but this assumes that we will never try to drillUp keywords. Which should be true,
		// so lets try it this way.
		TextNode textNode = parseTree.getNode(nodeIdToDrillUp).asTextNode();
		Concept searchTerm = (Concept) textNode.getConcepts().get(0);

		if (searchTerm == null)
			return;

		IPath pathFromRoot = termService.getShortestPathFromAnyRoot(searchTerm);

		if (pathItemIndex < 0 || pathItemIndex > pathFromRoot.length() - 1)
			return;

		Concept parent = pathFromRoot.getNodeAt(pathItemIndex);

		// UIFacet uiFacet = facetConfigurations.get(searchTerm.getFirstFacet());
		UIFacet uiFacet = facetConfigurations.get(textNode.getMappedFacet(searchTerm));
		boolean termIsOnPath = uiFacet.containsCurrentPathNode(searchTerm);
		if (uiFacet.isHierarchic() && uiFacet.getCurrentPathLength() > 0 && termIsOnPath) {
			while (uiFacet.removeLastNodeOfCurrentPath() != searchTerm)
				// That's all. We trust that selectedTerm IS on the path.
				;
		}

		// Map<String, IFacetTerm> unambigousTerms = getUnambigousQueryTerms();

		for (TextNode unambigousQueryTerm : parseTree.getUnambigousTextNodes()) {
			IConcept term = unambigousQueryTerm.getConcepts().get(0);

			if (termService.isAncestorOf(parent, term) && term != searchTerm) {
				// queryTerms.removeAll(unambigousQueryTerm);
				parseTree.remove(unambigousQueryTerm.getId());
				return;
			}
		}

		List<IConcept> parentCollection = new ArrayList<>();
		parentCollection.add(parent);
		textNode.getQueryToken().setConceptList(parentCollection);
		//textNode.setConcepts(parentCollection);
		// queryTerms.replaceValues(queryTerm, parentCollection);
		// searchState.getQueryTermFacetMap().put(parent, uiFacet);
		textNode.setFacetMapping(parent, uiFacet);
	}

	@Deprecated
	public boolean showPathForTerm() {
		Map<Facet, UIFacet> facetConfigurations = uiState.getUIFacets();
		IConcept mappedTerm = getFirstTermOfParseNode();
		Facet facet = mappedTerm.getFirstFacet();
		UIFacet facetConfiguration = facetConfigurations.get(facet);
		if (facet != null && facetConfiguration != null
				&& termService.getShortestPathFromAnyRoot(mappedTerm).length() > 1) {
			return facetConfiguration.isHierarchic();
		} else {
			return false;
		}
	}

	@Deprecated
	public boolean isFilterTerm() {
		// IFacetTerm mappedTerm = getMappedTerm();
		// Facet facet = searchState.getQueryTermFacetMap().get(mappedTerm);
		// if (facet.hasGeneralLabel(FacetLabels.General.FILTER)) {
		// this.hasFilter = true;
		// return true;
		// }
		return false;
	}

	public List<IConcept> getNodeConcepts() {
		if (conceptNodeId == null)
			return Collections.emptyList();

		// List<List<IMultiHierarchyNode>> = mappedTerm.getFacet().getId()

		TextNode node = parseTree.getNode(conceptNodeId).asTextNode();
		if (null != node) {
			List<IConcept> mappedQueryTerms = new ArrayList<>(node.getConcepts());

			return mappedQueryTerms;
		}
		return Collections.emptyList();
	}

	// public Multimap<String, IConcept> getSortedTerms() {
	//
	// Collection<IConcept> mappedQueryTerms = getMappedTerms();
	//
	// Multimap<String, IConcept> sortedQueryTerms = HashMultimap.create();
	//
	// for (IConcept currentTerm : mappedQueryTerms) {
	// sortedQueryTerms.put(currentTerm.getFirstFacet().getId(), currentTerm);
	// }
	//
	// return sortedQueryTerms;
	// }

	@Deprecated
	public Object[] getDrillUpContext() {
		return new Object[] { conceptNodeId, pathItemIndex };
	}

	public String[] getSpellingCorrection() {
		return new String[] { String.valueOf(conceptNodeId), correctedTerm };
	}

	@Log
	public void onConfirmSpellingCorrection(String queryTerm, String correctedTerm) throws Exception {
		// if (queryTerm == null || correctedTerm == null)
		// return;
		//
		// queryTerms.removeAll(queryTerm);
		// // logger.debug(spellingCorrection);
		// Collection<IFacetTerm> correctedTerms = spellingCorrectedQueryTerms.get(correctedTerm);
		// queryTerms.putAll(correctedTerm, correctedTerms);
		throw new OperationNotSupportedException();
	}

	public Index onRemoveTerm(Long nodeIdToRemove) throws Exception {
		if (nodeIdToRemove == null)
			return null;

		logger.trace("Removing ParseTree node with ID {}", nodeIdToRemove);
		parseTree.remove(nodeIdToRemove);
		logger.trace("New semedico query: {}", parseTree);

		if (parseTree.isEmpty()) {
			logger.debug("The last query concept was removed, redirecting to the index page.");
			return index;
		}

		return null;
	}

	public boolean isSimpleTerm() {
		Node node = getParseNode();
		if (null == node)
			return true;
		return node.getClass().equals(TextNode.class);
	}

	// all the term rendering and event stuff should probably go into a component of its own, as
//	@Deprecated
//	@Property
//	private Node eventArgItem;
//
//	@Deprecated
//	public boolean isEventTerm() {
//		Node node = getParseNode();
//		return node.getClass().equals(EventNode.class);
//	}
//
//	@Deprecated
//	public EventNode getCurrentEventNode() {
//		Node parseNode = getParseNode();
//		if (null == parseNode || !parseNode.getClass().equals(EventNode.class))
//			return null;
//		return (EventNode) parseNode;
//	}
//
//	@Deprecated
//	public IConcept getEventType() {
//		Node parseNode = getParseNode();
//		if (!parseNode.getClass().equals(EventNode.class))
//			return null;
//		EventNode eventNode = (EventNode) parseNode;
//		// TODO could be ambigue in theory
//		return eventNode.getEventTypes().get(0);
//	}
//
//	@Deprecated
//	public boolean eventHasSecondArgument() {
//		Node parseNode = getParseNode();
//		if (!parseNode.getClass().equals(EventNode.class))
//			return false;
//		EventNode eventNode = (EventNode) parseNode;
//		return eventNode.getChildren().size() > 1;
//	}

//	@Deprecated
//	public IConcept getFirstTermOfEventArgument() {
//		if (eventArgItem.isConceptNode()) {
//			// We currently do not support nested events, so if this node is a concept node it has to be a TextNode.
//			TextNode textNode = (TextNode) eventArgItem;
//			return textNode.getConcepts().get(0);
//		}
//		return null;
//	}

	// ---------- end event specific
	//
	// public void onEnableReviewFilter() {
	// searchState.setReviewsFiltered(true);
	// }
	//
	// public void onDisableReviewFilter() {
	// searchState.setReviewsFiltered(false);
	// }

	// @Validate("required")
	// public SortCriterium getSortCriterium() {
	// return searchState.getSortCriterium();
	// }
	//
	// public void setSortCriterium(SortCriterium sortCriterium) {
	// searchState.setSortCriterium(sortCriterium);
	// }

	/**
	 * Used by the template to get the path from a facet root to a particular query term. The elements are supplied with
	 * a link which causes a drill-up event. Thus, the last element of the path, the query term itself, is not returned.
	 * 
	 * @return The facet root path of the current term in exclusion of the term itself.
	 */
	@Deprecated
	public IPath getRootPath() {
		// Get the term mapped to the currently referenced query string in the
		// iteration over all query terms.
		IConcept mappedTerm = getFirstTermOfParseNode();
		IPath rootPath = termService.getShortestPathFromAnyRoot(mappedTerm);
		// Don't return the very last element as all elements returned here get
		// a drillUp-ActionLink. The the name of the term itself is rendered
		// separately.
		return rootPath.subPath(0, rootPath.length() - 1);
	}

	public String onStartNewSearchNode() {
		logger.debug("New search node started. Current serach state:\n{}", searchState.toString());
		searchState.createNewSearchNode();
		return "Index";
	}

	@Inject
	private Messages messages;

	@Property
	@Persist(PersistenceConstants.FLASH)
	private String searchNodeSubsumedMsg;


	// TODO check this, can probably go away
	public String onClearSearchNodes() {
		logger.debug("Clearing search nodes.");
		searchState.clear();
		return "Index";
	}

	// public boolean queryHasEvents() {
	// ParseTree semedicoQuery = searchState.getSemedicoQuery();
	// List<Node> conceptNodes = semedicoQuery.getConceptNodes();
	// for (Node node : conceptNodes) {
	// if (node.getClass().equals(EventNode.class))
	// return true;
	// }
	// return false;
	// }

	@Property
	private final StringValueEncoder stringValueEncoder = new StringValueEncoder();

	@Property
	@Persist
	private List<String> selectedLikelihoods;

	private List<String> likelihoodSelectionItems = Lists.newArrayList("high", "moderate", "low", "negation");

	public List<String> getLikelihoodModel() {
		return likelihoodSelectionItems;
	}

	public boolean isConceptNode() {
		Node parseNode = getParseNode();
		switch (parseNode.getNodeType()) {
		case EVENT:
		case CONCEPT:
			return true;
		default:
			return false;
		}
	}

	@Property
	private long lastNodeHeight = -1;
	@Property
	private int muh;

	public boolean isBooleanStructureBegan() {
		boolean ret = false;
		Node parseNode = getParseNode();
		switch (parseNode.getNodeType()) {
		case AND:
		case OR:
			ret = true;
			lastNodeHeight = parseNode.getHeight();
			break;
		default:
			break;
		}
		return ret;
	}

	public boolean isBooleanStructureEnded() {
		// boolean ret = false;
		// int parseNodeHeight = getParseNode().getHeight();
		// if (lastNodeHeight > parseNodeHeight) {
		// ret = true;
		// }
		// return ret;
		boolean ret = false;
		Node parseNode = getParseNode();
		switch (parseNode.getNodeType()) {
		case AND:
		case OR:
			ret = true;
			int parseNodeHeight = getParseNode().getHeight();
			ret = lastNodeHeight > parseNodeHeight;
			break;
		default:
			break;
		}
		return ret;
	}


	public long getHeightDiff() {
		return lastNodeHeight - getParseNode().getHeight();
	}

	public boolean isBooleanStructure() {
		return searchState.getSemedicoQuery().getNumberConceptNodes() > 1;
	}

	public boolean isResetNodeHeight() {
		lastNodeHeight = -1;
		return true;
	}
}
