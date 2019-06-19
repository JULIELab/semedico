package de.julielab.semedico.components;

import de.julielab.semedico.core.entities.state.SearchState;
import de.julielab.semedico.core.entities.state.UserInterfaceState;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.core.services.query.IParsingService;
import org.apache.tapestry5.ioc.annotations.Inject;

public class FacetBox extends AbstractFacetBox {

	@Inject
	private ISearchService searchService;

	@Inject
	private IParsingService parsingService;

	private UserInterfaceState uiState;

	public boolean setupRender() {
		this.uiState = sessionState.getDocumentRetrievalUiState();
		// Required in the super class for display purposes
		super.uiState = this.uiState;
		return super.initialize();
	}

	public void onTermSelect(String termIndexAndFacetId) {
		SearchState searchState = sessionState.getDocumentRetrievalSearchState();
		super.onTermSelect(termIndexAndFacetId);

		if (selectedConcept == null) {
			throw new IllegalStateException("The Concept object reflecting the newly selected term is null.");
		}
		log.debug("Name of filter concept: {} (ID: {})", selectedConcept.getPreferredName(), selectedConcept.getId());

		//searchState.addSelectedFacetConcept(selectedConcept);
	}

	// public void onTermSelect(String termIndexAndFacetId) {
	// SearchState searchState = sessionState.getDocumentRetrievalSearchState();
	// super.onTermSelect(termIndexAndFacetId);
	//
	// ParseTree parseTree = searchState.getSemedicoQuery();
	// List<Node> conceptNodes = parseTree.getConceptNodes();
	// Label selectedLabel = searchState.getSelectedTerm();
	// if (selectedLabel == null) {
	// throw new IllegalStateException("The Concept object reflecting the newly
	// selected term is null.");
	// }
	// log.debug("Name of newly selected label: {} (ID: {})",
	// selectedLabel.getName(), selectedLabel.getId());
	// // Get the FacetConfiguration associated with the selected term.
	// Facet selectedFacet = uiFacet;
	//
	// Concept selectedTerm;
	// boolean selectedTermIsAlreadyInQuery = false;
	// // //Multimap<String, IFacetTerm> newQueryTerms = HashMultimap.create();
	//
	// selectedTerm = ((TermLabel) selectedLabel).getTerm();
	// Event selectedEventTerm = null;
	// if (selectedTerm.getClass().equals(Event.class))
	// selectedEventTerm = (Event) selectedTerm;
	//
	// if (selectedFacet.isHierarchic()) {
	// log.debug("Searching for ancestors of {} in the query for refinement...",
	// selectedTerm.getPreferredName());
	// // We have to take caution when refining a term. Only the
	// // deepest term of each root-node-path in the hierarchy may be
	// // included in our queryTerms map.
	// // Reason 1: The root-node-path of _each_ term in queryTerms is
	// // computed automatically in the QueryPanel
	// // currently.
	// // Reason 2: We associate refined terms with the (user) query string
	// // of the original term. Multiple terms per string -> disambiguation
	// // triggers.
	// Collection<IPath> rootPathsOfSelectedTerm =
	// termService.getAllPathsFromRootsInFacet(selectedTerm, null);
	// Multimap<Node, Concept> nodesWithTermsToBeReplacedBySelectedTerm =
	// HashMultimap.create();
	// List<Node> nodesToBeReplacedWithNewTerm = new ArrayList<>();
	// // Build a new queryTerms map with all not-refined terms.
	// // The copying is done because in rare cases writing on the
	// // queryTokens map while iterating over it can lead to a
	// // ConcurrentModificationException.
	// // //for (Map.Entry<String, IFacetTerm> entry : queryTerms.entries()) {
	// for (Node node : conceptNodes) {
	// TextNode textNode;
	// if (node.getClass().equals(TextNode.class)) {
	// textNode = (TextNode) node;
	// List<? extends IConcept> terms = textNode.getTerms();
	// for (IConcept concept : terms) {
	// if (concept.isKeyword())
	// continue;
	// if (concept.isEventTrigger() &&
	// selectedTerm.getClass().equals(Event.class)) {
	// // Handels case: An event has been selected and the corresponding event
	// type is currently in
	// // the query (drill-down)
	// if (concept.equals(selectedEventTerm.getEventTerm())) {
	// log.debug(
	// "An event of type {} was selected - which currently is in the query -
	// drilling down to the concrete event.",
	// concept.getPreferredName());
	// // nodesWithTermsToBeReplacedBySelectedTerm.put(node, event);
	// nodesToBeReplacedWithNewTerm.add(node);
	// }
	// } else {
	// // We have an hierarchic facet; so we have a plain term, an aggregate or
	// an event
	// // For aggergates, we check if some element of the aggregate is
	// // TODO handle events...
	// List<Concept> conceptsOfNode;
	// if (concept.isAggregate()) {
	// conceptsOfNode = ((AggregateTerm) concept).getElements();
	// } else {
	// conceptsOfNode = Lists.newArrayList((Concept) concept);
	// }
	// for (Concept term : conceptsOfNode) {
	//
	// // TODO there should somewhere be the possibility to just add the term
	// with OR or AND
	// // (because
	// // this
	// // could actually alter the query!)
	// if (term.equals(selectedTerm)) {
	// selectedTermIsAlreadyInQuery = true;
	// break;
	// }
	//
	// boolean existingTermIsOnSelectedTermRootPath = false;
	// boolean selectedTermIsOnExistingRootPath = false;
	// for (IPath rootPath : rootPathsOfSelectedTerm) {
	// if (rootPath.containsNode(term)) {
	// existingTermIsOnSelectedTermRootPath = true;
	// break;
	// }
	// }
	// Collection<IPath> potentialAncestorRootPaths =
	// termService.getAllPathsFromRootsInFacet(term, null);
	// for (IPath rootPath : potentialAncestorRootPaths) {
	// if (rootPath.containsNode(selectedTerm)) {
	// selectedTermIsOnExistingRootPath = true;
	// break;
	// }
	// }
	//
	// if (selectedTermIsOnExistingRootPath ||
	// existingTermIsOnSelectedTermRootPath) {
	// // If there IS a term in queryTerms which lies on the root
	// // path, just memorize its key. Except its the exact term
	// // which
	// // has been selected. This can happen when a facet has been
	// // drilled up and the same term is selected again.
	// nodesWithTermsToBeReplacedBySelectedTerm.put(textNode, term);
	// log.debug("Found related term of {} in current search query: {}",
	// selectedTerm.getPreferredName(), term.getPreferredName());
	// }
	// }
	// }
	// }
	// } else if (node.getClass().equals(EventNode.class)) {
	// // Handels case: Event type selected, event node of this type is in query
	// (drill-up)
	// EventNode eventNode = (EventNode) node;
	// // For event nodes only two cases are interesting: an event type term
	// (regulation, localization,
	// // ...) has been selected or an actual Event instance
	// if (selectedTerm.isEventTrigger()) {
	// for (IConcept eventType : eventNode.getEventTypes()) {
	// if (selectedTerm.equals(eventType)) {
	// log.debug(
	// "Event type of event {} was selected, which is the general event type for
	// event {} that is included in the query, drilling up to event type term.",
	// selectedTerm.getPreferredName(), eventNode.toString());
	// nodesToBeReplacedWithNewTerm.add(node);
	// // TextNode newTextNode = new TextNode(selectedTerm.getPreferredName());
	// // List<Concept> newTerms = new ArrayList<>();
	// // newTerms.add(selectedTerm);
	// // newTextNode.setTerms(newTerms);
	// // newTextNode.setFacetMapping(selectedTerm, selectedFacet);
	// // parseTree.replaceNode(eventNode, newTextNode);
	// }
	// }
	// } else if (selectedTerm.getClass().equals(Event.class)) {
	// // Handels case: Complete event selected, an equal event structure is
	// already present in the
	// // query
	// boolean eventsAreDifferent =
	// isEventTermEqualToEventNode(selectedEventTerm, eventNode);
	// if (!eventsAreDifferent) {
	// log.debug("Event {} does already exist in the query.",
	// selectedEventTerm.getPreferredName());
	// selectedTermIsAlreadyInQuery = true;
	// }
	// }
	// }
	// }
	// if (!selectedTermIsAlreadyInQuery) {
	// if (!nodesWithTermsToBeReplacedBySelectedTerm.isEmpty()) {
	// log.debug("Ancestor found, refining the query.");
	// // For all parse tree nodes that have terms related to the selected term,
	// go through all node-terms
	// // are replace the related ones.
	// // Also, change the text of the respective nodes to the name of the
	// selected node.
	// for (Node node : nodesWithTermsToBeReplacedBySelectedTerm.keySet()) {
	// Set<Concept> termsToBeReplacedInNode =
	// new HashSet<>(nodesWithTermsToBeReplacedBySelectedTerm.get(node));
	// // TODO if necessary, also handle EventNodes
	// // For this to be necessary, we first have to implement a thorough
	// subsumption-algorithm of
	// // events, don't forget that their arguments can be boolean expressions!
	// if (node.getClass().equals(TextNode.class)) {
	// // If an event node was selected, this means that this node will be
	// completely replaced by
	// // the event
	// // TODO THIS IS THE WRONG MEANING OF THE MAP!! IT MIGHT WORK HERE BUT
	// THIS IS A COINCIDENCE
	// if (null != selectedEventTerm) {
	// EventNode eventNode = parsingService.createEventNode(selectedEventTerm);
	// parseTree.replaceNode(node, eventNode);
	// } else {
	// TextNode textNode = (TextNode) node;
	// Iterator<? extends IConcept> currentNodeTerms =
	// textNode.getTerms().iterator();
	// List<IConcept> newTerms = new ArrayList<>();
	// while (currentNodeTerms.hasNext()) {
	// IConcept term = currentNodeTerms.next();
	// if (!term.isAggregate()) {
	// if (termsToBeReplacedInNode.contains(term))
	// currentNodeTerms.remove();
	// else
	// newTerms.add(term);
	// } else {
	// AggregateTerm aggregate = (AggregateTerm) term;
	// boolean someElementIsRelatedToSelectedTerm = false;
	// for (Concept element : aggregate.getElements()) {
	// if (termsToBeReplacedInNode.contains(element))
	// someElementIsRelatedToSelectedTerm = true;
	// }
	// if (someElementIsRelatedToSelectedTerm)
	// currentNodeTerms.remove();
	// else
	// newTerms.add(term);
	// }
	// }
	// newTerms.add(selectedTerm);
	// textNode.setTerms(newTerms);
	// textNode.setText(selectedTerm.getPreferredName());
	// textNode.setFacetMapping(selectedTerm, selectedFacet);
	// }
	// }
	// }
	// } else if (!nodesToBeReplacedWithNewTerm.isEmpty()) {
	// for (Node node : nodesToBeReplacedWithNewTerm) {
	// if (null != selectedEventTerm) {
	// EventNode eventNode = parsingService.createEventNode(selectedEventTerm);
	// parseTree.replaceNode(node, eventNode);
	// } else {
	// TextNode textNode = createTextNodeFromSimpleConcept(selectedFacet,
	// selectedTerm);
	// parseTree.replaceNode(node, textNode);
	// }
	// }
	// } else {
	// // Otherwise, add a new mapping.
	// log.debug("No ancestor found, add the term into the current search
	// query.");
	// if (null != selectedEventTerm) {
	// log.debug("Selected term is an event query, creating equivalent event
	// structure in the parse tree.");
	// EventNode eventNode = parsingService.createEventNode(selectedEventTerm);
	//
	// try {
	// parseTree.add(parseTree.getRoot(), eventNode, NodeType.AND);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	//
	// } else {
	// log.debug("Selected term is a non-event concept term, adding a new text
	// node to the parse tree.");
	// // Associate the new term with its ID as query string.
	// TextNode newTextNode = createTextNodeFromSimpleConcept(selectedFacet,
	// selectedTerm);
	// try {
	// // TODO one should be able to choose between AND and OR. Someday ;-)
	// parseTree.add(parseTree.getRoot(), newTextNode, NodeType.AND);
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// // Append the new term to the raw query
	// }
	// }
	// }
	// // Facet is not hierarchic
	// else {
	// // selectedTerm = ((TermLabel) selectedLabel).getTerm();
	// if (selectedTerm.getClass().equals(Event.class)) {
	// for (Node node : parseTree.getEventNodes()) {
	// selectedTermIsAlreadyInQuery =
	// isEventTermEqualToEventNode(selectedEventTerm, (EventNode) node);
	// log.debug("Checking for flat facet whether event term is already in
	// query: {}",
	// selectedTermIsAlreadyInQuery);
	// }
	// if (!selectedTermIsAlreadyInQuery) {
	// log.debug("Selected term is an event query, creating equivalent event
	// structure in the parse tree.");
	// EventNode eventNode = parsingService.createEventNode(selectedEventTerm);
	//
	// try {
	// parseTree.add(parseTree.getRoot(), eventNode, NodeType.AND);
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	// } else {
	// if (parseTree.getTerms().contains(selectedTerm))
	// selectedTermIsAlreadyInQuery = true;
	// else {
	// // Not an event term
	// TextNode newTextNode = new TextNode(selectedTerm.getPreferredName());
	// ArrayList<Concept> newTerms = new ArrayList<>();
	// newTerms.add(selectedTerm);
	// newTextNode.setTerms(newTerms);
	// try {
	// parseTree.add(parseTree.getRoot(), newTextNode, NodeType.AND);
	// } catch (Exception e) {
	// log.error(
	// "Error when trying to the selected string term (i.e. non-hierarchic
	// facet) to the query: {}",
	// e);
	// e.printStackTrace();
	// }
	// }
	// }
	// }
	//
	// if (!selectedTermIsAlreadyInQuery) {
	// log.debug("New semedico query: {}", parseTree);
	// searchState.setDisambiguatedQuery(parseTree);
	// log.debug("Adding selected term {} for facet {} into current query.",
	// selectedTerm, selectedFacet);
	// // searchState.getQueryTermFacetMap().put(selectedTerm, selectedFacet);
	//
	// } else {
	// log.debug("Selected term is already contained in the query. No changes
	// made.");
	// }
	// }
	//
	// protected boolean isEventTermEqualToEventNode(Event selectedEventTerm,
	// EventNode eventNode) {
	// boolean eventsAreDifferent = false;
	// // first, check whether the event types match
	// IFacetTerm eventTermType = selectedEventTerm.getEventTerm();
	// eventsAreDifferent = !eventNode.getEventTypes().contains(eventTermType);
	// if (!eventsAreDifferent) {
	// // If the event types match, we continue to check the arguments.
	// EventBoolElement booleanStructure = eventNode.getBooleanStructure();
	// // Events selected from facets can only have one or two arguments. At all
	// other cases, the
	// // events don't match. There could still be a subsumption relation, but
	// this needs more thinking
	// // to recognize....
	// BoolElement arg1 = booleanStructure.getArg1();
	// BoolElement arg2 = booleanStructure.getArg2();
	// if (arg1.getClass().equals(LiteralBoolElement.class) && (arg2 == null ||
	// arg2.getClass().equals(
	// LiteralBoolElement.class))) {
	// LiteralBoolElement arg1Lit = (LiteralBoolElement) arg1;
	// LiteralBoolElement arg2Lit = (LiteralBoolElement) arg2;
	// // Currently, Event instances cannot be negated; we handle likelihood via
	// sorting, you
	// // cannot directly specify a search for that get an respective event term
	// from the index
	// // (although they exist in the index for the text and title field, not
	// however for the
	// // events facet field).
	// if (arg1Lit.isNegated()) {
	// eventsAreDifferent = true;
	// log.debug("Event has negated first argument, this cannot happen in the
	// index, selected event term and event node are different.");
	// }
	// if (null != arg2Lit && arg2Lit.isNegated()) {
	// eventsAreDifferent = true;
	// log.debug("Event has negated second argument, this cannot happen in the
	// index, selected event term and event node are different.");
	// }
	//
	// // Compare the number of arguments. If there is a difference, we don't
	// have to check the
	// // arguments themselves.
	// int parseEventNumArguments = arg2 == null ? 1 : 2;
	// if (parseEventNumArguments != selectedEventTerm.getArguments().size()) {
	// eventsAreDifferent = true;
	// log.debug("Selected event term has a different number of arguments than
	// event node, so the events are different.");
	// } else {
	// List<Concept> treeEventArgs = selectedEventTerm.getArguments();
	// Concept selectedEventArg1 = treeEventArgs.get(0);
	// Concept selectedEventArg2 = treeEventArgs.size() > 1 ?
	// treeEventArgs.get(1) : null;
	//
	// if (!selectedEventArg1.getId().equals(arg1Lit.getLiteral())) {
	// eventsAreDifferent = true;
	// log.debug("Argument 1 of event term does not equal this of the event
	// node, events are different.");
	// }
	// if (arg2Lit != null &&
	// !selectedEventArg2.getId().equals(arg2Lit.getLiteral())) {
	// eventsAreDifferent = true;
	// log.debug("Argument 2 of event term does not equal this of the event
	// node, events are different.");
	// }
	// }
	// }
	// }
	// return !eventsAreDifferent;
	// }
	//
	// protected TextNode createTextNodeFromSimpleConcept(Facet selectedFacet,
	// Concept selectedTerm) {
	// TextNode newTextNode = new TextNode(selectedTerm.getPreferredName());
	// List<Concept> newTerms = new ArrayList<>();
	// newTerms.add(selectedTerm);
	// newTextNode.setTerms(newTerms);
	// newTextNode.setFacetMapping(selectedTerm, selectedFacet);
	// return newTextNode;
	// }

	/**
	 * Updates the displayed labels in a facet, must be called e.g. after a
	 * drillUp.
	 */
	@Override
	protected void refreshFacetHit() {
		log.debug("Refreshing labels of facet {}.", uiFacet.getName());
		// TODO repair
//		searchService.doFacetNavigationSearch(uiFacet,
//				sessionState.getDocumentRetrievalSearchState().getSemedicoQuery());
		// First of all: Check whether new terms will show up for which we don't
		// have collected frequency counts yet. If so, get the counts.
		// uiState.createLabelsForFacet(facetConfiguration);
		// sortLabelsIntoDisplayGroup();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.julielab.semedico.components.AbstractFacetBox#getTermCSSClasses()
	 */
	@Override
	public String getTermCSSClasses() {
		return "";
	}
}
