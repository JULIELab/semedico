package de.julielab.semedico.resources;

public class ResourceTermLabels {
	public enum Gazetteer {
		/**
		 * Label indicating that respective terms are not to be used in
		 * dictionary lookups for annotation creation in text (can still be used
		 * for query analysis in Semedico).
		 */
		NO_PROCESSING_GAZETTEER,
		/**
		 * Label indicating that terms having this label should be included in
		 * the generation of a BioPortal dictionary for term lookup in the
		 * Semedico UIMA pipeline. There, the type <tt>OntClassMention</tt> is
		 * used to describe BioPortal term matches.
		 * 
		 * @deprecated I don't think this has actually been used at any time; we
		 *             just create a dictionary for all Semedico terms (even
		 *             though its currently called BioPortalGazetteer
		 *             everywhere), except those we have a classifier for and
		 *             thus a "better" source than a dictionary.
		 */
		@Deprecated GAZETTEER_BIOPORTAL,
		/**
		 * Terms that should not be included in the query dictionary. That is,
		 * in a freetext analysis, these terms will not be candidates for
		 * recognition.
		 */
		NO_QUERY_DICTIONARY
	}

	/**
	 * These types are used as labels in the term database to define different
	 * sets for ID mappings. With "mapping" we here mean the replacement of the
	 * original entity ID as recognized in the Semedico UIMA pipeline (project
	 * "semedico-app") with the ID of the respective Semedico term that is
	 * associated with the entity. One has to take care of different issues
	 * here:
	 * <ul>
	 * <li>Some entities in UIMA come with their original ID, mainly the outcome
	 * of machine-learning based methods (JNET, GeNo) or third-party components
	 * (Linneaus Species Annotator), others are already tagged with the
	 * respective Semedico ID (dictionary approaches where the Semedico term IDs
	 * are given). So not all terms have to be mapped.</li>
	 * <li>Some database use overlapping identifiers despite the fact that the
	 * database records are completely different. For example, NCBI Gene uses
	 * numbers as Gene IDs, as does the NCBI Taxonomy for species IDs. Thus, the
	 * two mappings have to be separated.</li>
	 * <li>For <tt>MeshHeadings</tt> (<em>not</em> <tt>MeshMentions</tt>), the
	 * "ID" is their heading - the human-readable name - and not the descriptor
	 * UI. So for the respective mapping, the preferred term has to be used.
	 * </ul>
	 * 
	 * @author faessler
	 * 
	 */
	public enum IdMapping {
		ID_MAP_NCBI_GENES, ID_MAP_IMMUNOLOGY, ID_MAP_NCBI_TAXONOMY, ID_MAP_MESH
	}

	public enum Suggestions {
		/**
		 * Concepts that should not be suggested to the user in Semedico during
		 * autocomplete.
		 */
		NO_SUGGESTIONS
	}
}
