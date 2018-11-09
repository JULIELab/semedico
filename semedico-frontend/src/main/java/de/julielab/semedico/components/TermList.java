package de.julielab.semedico.components;

import de.julielab.semedico.core.SearchState;
import de.julielab.semedico.core.UserInterfaceState;
import de.julielab.semedico.core.concepts.Concept;
import de.julielab.semedico.core.concepts.ConceptType;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.interfaces.IPath;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.parsing.Node;
import de.julielab.semedico.core.parsing.Node.NodeType;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.ParseTree.Serialization;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.search.components.data.LabelStore;
import de.julielab.semedico.core.search.components.data.TermLabel;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import de.julielab.semedico.state.SemedicoSessionState;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.json.JSONArray;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Import(stylesheet = "context:css/termlist.css")
public class TermList {
	@Parameter
	@Property
	private UIFacet uiFacet;

	@Parameter
	private LabelStore labelStore;

	@Property
	private Label labelItem;

	@Property
	private int labelIndex;

	@SessionState
	private SemedicoSessionState sessionState;

	@Inject
	private ITermService termService;

	@Inject
	private Logger log;

	public JSONArray getLabelSynonyms() {
		JSONArray synonyms = new JSONArray();
		if (labelItem instanceof TermLabel) {
			Concept term = ((TermLabel) labelItem).getTerm();
			for (String synonym : term.getSynonyms())
				synonyms.put(synonym);
		}
		return synonyms;
	}

	public JSONArray getLabelDescriptions() {
		JSONArray descriptions = new JSONArray();
		if (labelItem instanceof TermLabel) {
			Concept term = ((TermLabel) labelItem).getTerm();
			for (String description : term.getDescriptions())
				descriptions.put(description);
		}
		return descriptions;
	}

	public String getLabelFacetName() {
		if (labelItem instanceof TermLabel) {
			Concept term = ((TermLabel) labelItem).getTerm();
			String facetname = term.getFirstFacet().getName();
			if (facetname.length() > 30 && term.getFirstFacet().getShortName() != null)
				facetname = term.getFirstFacet().getShortName();
			return facetname;
		}
		return "";
	}

	public String getTermIndexAndFacetId() {
		return labelIndex + "_" + uiFacet.getId();
	}

	public void onTermSelect(String termIndexAndFacetId) {
		String[] termIdxFacetId = termIndexAndFacetId.split("_");
		int conceptIndex = Integer.parseInt(termIdxFacetId[0]);
		String facetId = termIdxFacetId[1];

		SearchState searchState = sessionState.getDocumentRetrievalSearchState();
		UserInterfaceState uiState = sessionState.getDocumentRetrievalUiState();
		UIFacet uiFacet = uiState.getUIFacet(facetId);
		if (!(conceptIndex < uiFacet.getLabelDisplayGroup().getNumberOfDisplayedObjects())) {
			throw new IllegalStateException(
					"Term with index " + conceptIndex + " does not exist in this FacetBox component (there are only "
							+ uiFacet.getLabelDisplayGroup().getNumberOfDisplayedObjects() + ").  FacetConfiguration: "
							+ uiFacet);
		}

		Label label = uiFacet.getLabelDisplayGroup().getDisplayedObjects().get(conceptIndex);
		Concept selectedConcept = ((TermLabel) label).getTerm();
		// if (!termService.isStringTermID(termId)) {
		// selectedConcept = (Concept) termService.getTerm(termId);
		// } else {
		// selectedConcept = termService.getTermObjectForStringTerm(termId,
		// facetId);
		// }
		if (uiFacet.isInHierarchicViewMode()) {
			if (label.hasChildHitsInFacet(uiFacet)) {
				uiFacet.appendNodeToCurrentPath(selectedConcept);
			}
			if (selectedConcept.hasChildrenInFacet(uiFacet.getId())) {
				uiFacet.appendNodeToCurrentPath(selectedConcept);
			}
		}

		// Get the FacetConfiguration associated with the selected term.
		Facet selectedFacet = uiFacet;

		Concept selectedTerm = selectedConcept;
		boolean selectedTermIsAlreadyInQuery = false;
		// //Multimap<String, IFacetTerm> newQueryTerms = HashMultimap.create();

		ParseTree parseTree = searchState.getSemedicoQuery();
		
		log.debug("Current query: {}", parseTree);
		log.debug("Current query (IDs): {}", parseTree.toString(Serialization.NODE_IDS));

		if (selectedConcept.hasChildren() || selectedConcept.hasParent()) {
			log.debug("Searching for ancestors of {} in the query for refinement...", selectedTerm.getPreferredName());
			// We have to take caution when refining a term. Only the
			// deepest term of each root-node-path in the hierarchy may be
			// included in our queryTerms map.
			// Reason 1: The root-node-path of _each_ term in queryTerms is
			// computed automatically in the QueryPanel
			// currently.
			// Reason 2: We associate refined terms with the (user) query string
			// of the original term. Multiple terms per string -> disambiguation
			// triggers.
			Collection<IPath> rootPathsOfSelectedTerm = termService.getAllPathsFromRootsInFacet(selectedTerm, null);
			List<Node> nodesToBeReplacedWithNewTerm = new ArrayList<>();
			for (Node node : parseTree.getConceptNodes()) {
				if (node.getClass().equals(TextNode.class)) {
					TextNode textNode = (TextNode) node;
					// Until we see a reason to, we do not include ambiguous
					// query tokens / nodes into the refinement process. It
					// doesn't seem to make much sense.
					if (textNode.isAmbiguous())
						continue;

					IConcept concept = textNode.getConcepts().get(0);
					if (concept.getConceptType() == ConceptType.KEYWORD)
						continue;

					if (concept.equals(selectedTerm)) {
						selectedTermIsAlreadyInQuery = true;
						log.debug(
								"Found selected term {} with ID {} to be equal to term {} with ID {} already in the query.",
								new Object[] { selectedTerm.getPreferredName(), selectedTerm.getId(),
										concept.getPreferredName(), concept.getId() });
						break;
					}

					boolean existingTermIsOnSelectedTermRootPath = false;
					boolean selectedTermIsOnExistingRootPath = false;
					for (IPath rootPath : rootPathsOfSelectedTerm) {
						if (rootPath.containsNode((Concept) concept)) {
							existingTermIsOnSelectedTermRootPath = true;
							break;
						}
					}
					Collection<IPath> potentialAncestorRootPaths = termService.getAllPathsFromRootsInFacet(concept,
							null);
					for (IPath rootPath : potentialAncestorRootPaths) {
						if (rootPath.containsNode(selectedTerm)) {
							selectedTermIsOnExistingRootPath = true;
							break;
						}
					}

					if (selectedTermIsOnExistingRootPath || existingTermIsOnSelectedTermRootPath) {
						nodesToBeReplacedWithNewTerm.add(textNode);
						log.debug("Found related term of {} in current search query: {}",
								selectedTerm.getPreferredName(), concept.getPreferredName());
					}
				}
			}
			if (!selectedTermIsAlreadyInQuery) {
				if (!nodesToBeReplacedWithNewTerm.isEmpty()) {
					for (Node node : nodesToBeReplacedWithNewTerm) {
						TextNode textNode = createTextNodeFromConcept(selectedFacet, selectedTerm, parseTree, node);
						parseTree.replaceNode(node, textNode);
						log.debug("Replaced node {} by {}, new query: {}", new Object[] {node.getText(), textNode.getText(), parseTree});
					}
				} else {
					// Otherwise, add a new mapping.
					log.debug("No ancestor found, add the term into the current search	query.");
					log.debug("Selected term is a non-event concept term, adding a new text node to the parse tree.");
					// Associate the new term with its ID as query string.
					TextNode newTextNode = createTextNodeFromConcept(selectedFacet, selectedTerm, parseTree, null);
					try {
						parseTree.add(parseTree.getRoot(), newTextNode, NodeType.AND);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		// Facet is not hierarchic
		else {
			// selectedTerm = ((TermLabel) selectedLabel).getTerm();
			if (parseTree.getTerms().contains(selectedTerm))
				selectedTermIsAlreadyInQuery = true;
			else {
				TextNode newTextNode = createTextNodeFromConcept(selectedFacet, selectedTerm, parseTree, null);
				try {
					parseTree.add(parseTree.getRoot(), newTextNode, NodeType.AND);
				} catch (Exception e) {
					log.error(
							"Error when trying to the selected string term (i.e. non-hierarchic facet) to the query: {}",
							e);
					e.printStackTrace();
				}
			}
		}

		if (!selectedTermIsAlreadyInQuery) {
			log.debug("Added selected term {} for facet {} into current query.", selectedTerm, selectedFacet);
			log.debug("New semedico query: {}", searchState.getSemedicoQuery());
			log.debug("New semedico query (IDs): {}", searchState.getSemedicoQuery().toString(Serialization.NODE_IDS));

		} else {
			log.debug("Selected term is already contained in the query. No changes made.");
		}
	}

	private QueryToken createQueryTokenFromConcept(Facet selectedFacet, Concept concept, ParseTree parseTree,
			Node replaced) {
		String tokenString = concept.getPreferredName();
		int tokenBegin = 0;
		if (!parseTree.getQueryTokens().isEmpty()) {
			List<QueryToken> currentTokens = parseTree.getQueryTokens();
			int positionOfNewQt = currentTokens.size();
			if (replaced != null)
				positionOfNewQt = parseTree.getIndexOfNodeQueryToken(replaced);
			QueryToken previousToken = currentTokens.get(positionOfNewQt - 1);
			tokenBegin = previousToken.getEndOffset() + 1;
		}
		QueryToken qt = new QueryToken(tokenBegin, tokenBegin + tokenString.length(), tokenString);
		qt.setInputTokenType(TokenType.CONCEPT);
		qt.setFacetMapping(concept, selectedFacet);
		qt.addTermToList(concept);
		qt.setUserSelected(true);
		return qt;
	}

	protected TextNode createTextNodeFromConcept(Facet selectedFacet, Concept selectedTerm, ParseTree parseTree,
			Node replaced) {
		QueryToken qt = createQueryTokenFromConcept(selectedFacet, selectedTerm, parseTree, replaced);
		TextNode newTextNode = new TextNode(selectedTerm.getPreferredName(), qt);
		newTextNode.setFacetMapping(selectedTerm, selectedFacet);
		return newTextNode;
	}
}
