package de.julielab.semedico.commons.concepts;

import java.util.Collection;

public class FacetGroupLabels {
	/**
	 * General labels/properties for facet groups, like in which frontend view the facet group should be shown.
	 * 
	 * @author faessler
	 * 
	 */
	public enum General {
		/**
		 * <p>
		 * Facet group label to indicate the facet group is shown at the frontend when being in search-mode.
		 * </p>
		 */
		SHOW_FOR_SEARCH,
		/**
		 * <p>
		 * Facet group label to indicate the facet group is shown at the frontend when being in
		 * direct-link-discovery-mode.
		 * </p>
		 */
		SHOW_FOR_BTERMS
	}

	/**
	 * Type labels to differentiate between arbitrary types of facet groups. The semantics of this isn't well defined
	 * but can just be anything necessary to distinguish between facet groups, e.g. whether this is the "BioPortal"
	 * facet group.
	 * 
	 * @author faessler
	 * 
	 */
	public enum Type {
		BIO_PORTAL
	}

	public static <T extends Collection<String>, E extends Collection<FacetGroupLabels.General>> E stringLabels2GeneralEnumLabels(
			T stringLabels, Class<E> clazz) {
		try {
			E enumLabels = clazz.newInstance();
			for (String stringLabel : stringLabels) {
				try {
					enumLabels.add(FacetGroupLabels.General.valueOf(stringLabel));
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
