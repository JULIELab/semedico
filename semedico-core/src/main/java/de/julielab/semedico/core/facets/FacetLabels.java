package de.julielab.semedico.core.facets;

import java.util.Collection;

import de.julielab.semedico.core.Labels;

public class FacetLabels {
	/**
	 * Unique facets labels like whether a facet is the Author facet, BTerm facet etc. There always must be at most one
	 * facet for each label of this enumeration.
	 * 
	 * @author faessler
	 * 
	 */
	public enum Unique implements Labels {

		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'first author' facet.
		 * </p>
		 */
		FIRST_AUTHORS,
		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'last author' facet.
		 * </p>
		 */
		LAST_AUTHORS,
		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'authors' facet.
		 * </p>
		 */
		AUTHORS,
		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'bterms' facet.
		 * </p>
		 */
		BTERMS,
		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'nofacet' facet, i.e. a container for terms that
		 * are not necessarily displayed for browsing purposes but just exist as background knowledge of the system.
		 * </p>
		 */
		NO_FACET,
		/**
		 * <p>
		 * A label to indicate the "Keywords" facet. The keyword facet is not part of the database but the
		 * "constant facet" for words we can't identify as semantic terms.
		 * </p>
		 */
		KEYWORDS,
		/**
		 * <p>
		 * Facet group property to indicate the facet is the "document classes" filter facet.
		 * </p>
		 */
		DOCUMENT_CLASSES,
		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'journals' facet.
		 * </p>
		 */
		JOURNALS,
		/**
		 * <p>
		 * Database facet object label to indicate that a facet is the 'years' facet.
		 * </p>
		 */
		YEARS,
	}

	/**
	 * These labels are of general nature, i.e. multiple facets may contain each of these labels.
	 * 
	 * @author faessler
	 * 
	 */
	public enum General implements Labels {
		USE_FOR_SUGGESTIONS, USE_FOR_BTERMS, FILTER, USE_FOR_QUERY_DICTIONARY, FACET_BIO_PORTAL, FACET, EVENTS
	}

	public static <T extends Collection<String>, E extends Collection<FacetLabels.Unique>> E uniqueStringLabels2EnumLabels(
			T stringLabels, Class<E> clazz) {
		try {
			E enumLabels = clazz.newInstance();
			for (String stringLabel : stringLabels) {
				try {
					enumLabels.add(FacetLabels.Unique.valueOf(stringLabel));
				} catch (IllegalArgumentException e) {// its okay, then it just wasn't a general label

				}
			}
			return enumLabels;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static <T extends Collection<String>, E extends Collection<FacetLabels.General>> E generalStringLabels2EnumLabels(
			T stringLabels, Class<E> clazz) {
		try {
			E enumLabels = clazz.newInstance();
			for (String stringLabel : stringLabels) {
				try {
					enumLabels.add(FacetLabels.General.valueOf(stringLabel));
				} catch (IllegalArgumentException e) {// its okay, then it just wasn't a general label

				}
			}
			return enumLabels;
		} catch (InstantiationException | IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
