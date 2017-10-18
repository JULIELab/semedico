package de.julielab.semedico.core.search.components.data;

/**
 * The same as {@link DocumentSpanSearchResult} but for text portions that are
 * rated in terms of epistemic modality ('likelihood').
 * 
 * @author faessler
 *
 */
public class DocumentEMSpanSearchResult extends DocumentSpanSearchResult {
	protected int emRating;
}
