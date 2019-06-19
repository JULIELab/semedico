package de.julielab.semedico.core.services.interfaces;

import com.fasterxml.jackson.databind.JsonNode;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.concepts.ConceptDescription;
import de.julielab.semedico.core.concepts.KeywordConcept;
import de.julielab.semedico.core.concepts.SyncDbConcept;
import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.util.ConceptCreationException;

public interface IConceptCreator {

	IHierarchicalConcept createConceptFromJson(String jsonString, JsonNode conceptLabels);

	/**
	 * Creates a complete Semedico concept from the string in JSON format given by <tt>jsonString</tt>. The JSON input is
	 * expected to include all data necessary to create a Semedico concept. The <tt>conceptLabels</tt> must not be empty since
	 * it should at least contain the <tt>TERM</tt> label. However, this label would be ignored. An interesting label
	 * would be {@link TermLabels.GeneralLabel#EVENT_TERM}, for example, since this identifies a concept to be an event
	 * trigger.
	 *
	 * @param jsonString
	 * @param conceptLabels
	 * @return
	 */
	IHierarchicalConcept createConceptFromJson(String jsonString, JsonNode conceptLabels, Class<? extends IHierarchicalConcept> conceptClass);

	void updateProxyConceptFromDescription(IHierarchicalConcept proxy, ConceptDescription description);

	/**
	 * This method just calls {@link #createConceptFromJson(String, JsonNode, Class)} with the default class
	 * <tt>FacetTerm.class</tt>.
	 *
	 * @param jsonString
	 * @param conceptLabels
	 * @return
	 */
	void updateProxyConceptFromJson(IHierarchicalConcept proxy, String jsonString, JsonNode conceptLabels);

	/**
	 * Creates an almost empty concept that only knows about its ID. Such concepts are intended to be used as proxy objects
	 * that can be used immediately in data structures while their actual data is requested from the concept database.
	 * Thus, this method should only be used if the concept's data is loaded soon and written into the instance via
	 * {@link #updateProxyConceptFromJson(IHierarchicalConcept, String, JsonNode)}.
	 * <p>
	 * For concurrent loading - e.g. as done by {@link de.julielab.semedico.core.services.AsyncCacheLoader} - a {@link SyncDbConcept} should be requested.
	 * </p>
	 *
	 * @param id
	 * @param conceptClass
	 * @return
	 */
	IHierarchicalConcept createDatabaseProxyConcept(String id, Class<? extends SyncDbConcept> conceptClass);

	/**
	 * Creates a keyword concept, i.e. a concept that has no record in the concept database. Thus, its just an ordinary word,
	 * without any knowledge about synonyms, writing variants or a taxonomic structure.
	 *
	 * @param id
	 *            The actual Lucene concept that should be searched for in the index. For keyword concepts, this is most
	 *            commonly just the stemmed form of the concept to match the token processing for the index.
	 * @param name
	 *            The written form of the concept - or word - as it occurred in the user query.
	 * @return
	 */
	KeywordConcept createKeywordConcept(String id, String name);

    IHierarchicalConcept createConceptFromJsonNodes(JsonNode conceptTree, JsonNode conceptLabelsNode);

	<T extends IHierarchicalConcept> T createConceptFromDescription(ConceptDescription description, Class<T> conceptClass) throws ConceptCreationException;
}
