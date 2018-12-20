package de.julielab.semedico.core.services;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.search.results.highlighting.AuthorHighlight;
import de.julielab.semedico.core.search.results.highlighting.Highlight;
import de.julielab.semedico.core.search.results.highlighting.ISerpHighlight;
import de.julielab.semedico.core.search.results.highlighting.SerpHighlightList;
import de.julielab.semedico.core.services.interfaces.IHighlightingService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import org.slf4j.Logger;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HighlightingService implements IHighlightingService {

    private Logger log;
    private Matcher htmlTagMatcher;
    private ITermService termService;

    public HighlightingService(Logger log, ITermService termService) {
        this.log = log;
        this.termService = termService;
        this.htmlTagMatcher = Pattern.compile("<[^>]+>").matcher("");
    }


    @Override
    public ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued) {
        return getFieldHighlights(serverDoc, field, multivalued, false, false);
    }

    @Override
    public ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                             boolean replaceMissingWithFieldValue, boolean merge) {
        return getFieldHighlights(serverDoc, field, multivalued, replaceMissingWithFieldValue, merge, false);
    }

    @Override
    public ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                             boolean replaceMissingWithFieldValue) {
        return getFieldHighlights(serverDoc, field, multivalued, replaceMissingWithFieldValue, false, false);
    }

    @Override
    public ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                             boolean replaceMissingWithFieldValue, boolean merge, boolean replaceConceptIds) {
        return getFieldHighlights(serverDoc, field, multivalued, replaceMissingWithFieldValue, merge, replaceConceptIds, Integer.MAX_VALUE);
    }

    @Override
    public ISerpHighlight getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
                                             boolean replaceMissingWithFieldValue, boolean merge, boolean replaceConceptIds, int maxHighlightLength) {
        SerpHighlightList fieldHighlights = new SerpHighlightList();
        List<Object> fieldValues;
        Map<String, List<String>> highlights = serverDoc.getHighlights();
        if (highlights != null) {
            List<String> fieldHlStrings = highlights.get(field);
            if (null != fieldHlStrings) {
                for (String hlString : fieldHlStrings)
                    fieldHighlights.add(new Highlight(hlString, field, serverDoc.getScore()));
            }
        }
        if ((fieldHighlights.isEmpty() && replaceMissingWithFieldValue) || (merge && multivalued)) {
            if (multivalued) {
                final Optional<List<Object>> valuesOpt = serverDoc.getFieldValues(field);
                fieldValues = valuesOpt.isPresent() ? valuesOpt.get() : Collections.emptyList();
            }
            else {
                final Optional<Object> valueOpt = serverDoc.get(field);
                // If the value is not present, the field value wasn't returned at all or just doesn't exist
                fieldValues = valueOpt.isPresent() ? Collections.singletonList(valueOpt.get()) : Collections.emptyList();
            }

            if (fieldHighlights.isEmpty()) {
                if (null != fieldValues) {
                    for (Object fieldValue : fieldValues)
                        fieldHighlights.add(new Highlight((String) fieldValue, field, 0f));
                }
            } else {
                // merging; the basis are the field values since those are
                // complete in any way. we look for those elements that equal
                // the highlighted items after stripping the HTML tags
                // first create a map to connect the HTML-tag-stripped
                // highlighted items with the highlighted version
                Map<String, Highlight> hlMap = new HashMap<>();
                for (Highlight hl : fieldHighlights) {
                    String hlTerm = stripTags(hl.getHighlight());
                    hlMap.put(hlTerm, hl);
                }

                SerpHighlightList mergedHighlights = new SerpHighlightList();
                for (Object fieldValue : fieldValues) {
                    String fieldValueString = String.valueOf(fieldValue);
                    Highlight highlight = hlMap.get(fieldValueString);
                    if (null != highlight)
                        mergedHighlights.add(highlight);
                    else
                        mergedHighlights.add(new Highlight(fieldValueString, field, 0f));
                }
                fieldHighlights = mergedHighlights;
            }
        }
        if (fieldHighlights.isEmpty()) {
            // this message only makes sense when we wanted to merge and not
            // even got a stored value
            if (merge || replaceMissingWithFieldValue)
                log.warn(
                        "Neither a field highlighting nor the field value could be retrieved for document {}, field {}.",
                        serverDoc.getId(), field);
        } else if (replaceConceptIds) {
            for (Highlight hl : fieldHighlights) {
                String hlTerm = stripTags(hl.getHighlight());
                IConcept concept = termService.getTerm(hlTerm);
                if (null != concept) {
                    List<String> tags = getTags(hl.getHighlight());
                    hl.setHighlight(tags.get(0) + concept.getPreferredName() + tags.get(1));
                }
            }
        }
        // remove duplicates
        Set<String> hlset = new HashSet<>();
        fieldHighlights.removeIf(highlight -> !hlset.add(highlight.getHighlight()));
        if (maxHighlightLength < Integer.MAX_VALUE)
            fieldHighlights.forEach(hl -> formatHighlight(hl, maxHighlightLength));
        return fieldHighlights;
    }

    private List<String> getTags(String highlight) {
        List<String> tags = new ArrayList<>();
        htmlTagMatcher.reset(highlight);
        while (htmlTagMatcher.find())
            tags.add(htmlTagMatcher.group());
        return tags;
    }

    private synchronized String stripTags(String highlight) {
        htmlTagMatcher.reset(highlight);
        return htmlTagMatcher.replaceAll("");
    }

    /**
     * Was used for abstract highlights in the specific abstract highlight method which has been removed because it
     * was too specific. Incorporate this into the getFieldHighlights method.
     *
     * @param fragment
     * @return
     */
    private String addFragmentDots(String fragment) {
        // To determine whether to prefix the fragment with "..." or
        // not,
        // check if the first char is upper case (mostly sentence
        // beginning). If the char is '<', the first word is
        // highlighted, e.g. '<em>Interleukin-2<em> has proven to
        // [...]'. So the first char is the char after the closing brace
        // '>'.
        char firstChar = fragment.charAt(0);
        if (firstChar == '<')
            firstChar = fragment.charAt(fragment.indexOf('>') + 1);

        if (!Character.isUpperCase(firstChar))
            fragment = "..." + fragment;
        final char lastChar = fragment.charAt(fragment.length() - 1);
        if (lastChar != '.' && lastChar != '!' && lastChar != '?')
            fragment = fragment + "...";
        return fragment;
    }

    /**
     * Shortens the highlight to length <tt>length</tt> and calls {@link #addFragmentDots(String)} to pre- and append '...'
     * if the remaining highlight fragment does not begin with a capital character or a highlighting tag or does not
     * end in a sentence ending punctuation, respectively.
     * @param hl The highlight to format.
     * @param length The maximal length of the highlight.
     */
    private void formatHighlight(Highlight hl, int length) {
        if (hl.getHighlight().length() > length)
            hl.setHighlight(hl.getHighlight().substring(0, length));
        hl.setHighlight(addFragmentDots(hl.getHighlight()));
    }

    @Override
    public SerpHighlightList getAuthorHighlights(ISearchServerDocument serverDoc, String authorField) {
        return getAuthorHighlights(serverDoc, authorField, null);
    }


    @Override
    public SerpHighlightList getAuthorHighlights(ISearchServerDocument serverDoc, String authorField, String affiliationField) {
        List<String> authorHls = serverDoc.getHighlights().get(authorField);
        if (null == authorHls || authorHls.isEmpty())
            return null;
        SerpHighlightList ret = new SerpHighlightList();

        SerpHighlightList mergedAuthors = getFieldHighlights(serverDoc, authorField, true, true,
                true).list();
        SerpHighlightList mergedAffilliations = null;
        if (affiliationField != null) {
            mergedAffilliations = getFieldHighlights(serverDoc,
                    affiliationField, true, true, true).list();
        }
        for (int i = 0; i < mergedAuthors.size(); ++i) {
            Highlight mergedAuthorHighlight = mergedAuthors.get(i);
            AuthorHighlight authorHl = new AuthorHighlight(mergedAuthorHighlight.getHighlight(), authorField, serverDoc.getScore());

            if (mergedAffilliations != null && i < mergedAffilliations.size()) {
                Highlight mergedAffiliationHighlight = mergedAffilliations.get(i);
                authorHl.setAffiliation(mergedAffiliationHighlight.getHighlight());
            }
            ret.add(authorHl);
        }
        return ret;
    }

}