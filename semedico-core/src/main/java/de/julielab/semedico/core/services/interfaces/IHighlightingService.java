package de.julielab.semedico.core.services.interfaces;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.search.results.highlighting.AuthorHighlight;
import de.julielab.semedico.core.search.results.highlighting.ISerpHighlight;
import de.julielab.semedico.core.search.results.highlighting.SerpHighlightList;

public interface IHighlightingService {

    ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued);

    ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                       boolean replaceMissingWithFieldValue, boolean merge);

    /**
     * <p>
     * Retrieves the highlights from <tt>serverDoc</tt> and returns highlighting for <tt>field</tt>.
     * </p>
     *
     * @param serverDoc                    The searach server document containing highlighting.
     * @param field                        The field to get highlighting for.
     * @param multivalued                  Whether the requested highlighting field can have multiple values. Required internally to call the correct method.
     * @param replaceMissingWithFieldValue If there is no highlight for the given field, this parameter controls whether the stored field value should be returned instead.
     * @param merge                        For multivalued fields, this parameter specifies if the highlights should merged into the stored field value and then all the values - with the highlights - should be returned.
     * @param replaceConceptIds            If set to <tt>true</tt>, highlighted string passages are interpreted as concept IDs. The highlighted ID is then replaced by the preferred name of the concept.
     * @return The requested field highlights.
     */
    ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                       boolean replaceMissingWithFieldValue, boolean merge, boolean replaceConceptIds);

    ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                             boolean replaceMissingWithFieldValue, boolean merge, boolean replaceConceptIds, int maxHighlightLength);

    ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued, boolean replaceMissingWithFieldValue);

    /**
     * <p>
     * This method creates the specific {@link AuthorHighlight} type from the given document.
     * </p>
     * <p>
     *     For this method to return the expected result, certain requirements to the values of the fields given by <tt>authorField</tt> and <tt>affiliationField</tt>
     *     must be met:
     *     <ul>
     *         <li>The author field values must follow the format <tt>lastname,firstname</tt> or just <tt>name</tt></li>
     *         <li>The affiliationField is parallel to the author field. It might be shorter on which case the trailing authors won't have an affiliation</li>
     *         <li>Both fields are multi-valued fields where each individual value within an array is a single string.</li>
     *     </ul>
     * </p>
     * @param serverDoc The hit document returned by the search server where author highlights have been activated.
     * @param authorField The name of the field containing the author names.
     * @param affiliationField The name of the field containing the author affiliations.
     * @return Structured objects reflecting all authors and all affiliations with highlighting where applicable
     */
    SerpHighlightList getAuthorHighlights(ISearchServerDocument serverDoc, String authorField, String affiliationField);

    /**
     * <p>
     * This method creates the specific {@link AuthorHighlight} type from the given document.
     * </p>
     * <p>
     *     For this method to return the expected result, certain requirements to the values of the field given by <tt>authorField</tt>
     *     must be met:
     *     <ul>
     *         <li>The author field values must follow the format <tt>lastname,firstname</tt> or just <tt>name</tt></li>
     *         <li>The author field is multi-valued where each individual value within an array is a single string.</li>
     *     </ul>
     * </p>
     * @param serverDoc The hit document returned by the search server where author highlights have been activated.
     * @param authorField The name of the field containing the author names.
     * @return Structured objects reflecting all authors and all affiliations with highlighting where applicable
     * @see #getAuthorHighlights(ISearchServerDocument, String, String)
     */
    SerpHighlightList getAuthorHighlights(ISearchServerDocument serverDoc, String authorField);
}
