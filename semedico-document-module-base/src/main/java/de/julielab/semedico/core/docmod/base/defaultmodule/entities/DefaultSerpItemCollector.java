package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.docmod.base.entities.DocModInfo;
import de.julielab.semedico.core.docmod.base.entities.SerpItemResult;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.search.searchresponse.IElasticServerResponse;
import org.apache.xerces.impl.xs.opti.DefaultDocument;

import java.util.Optional;
import java.util.stream.Stream;

import static de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule.FIELD_TITLE;

public class DefaultSerpItemCollector extends SearchResultCollector<SemedicoESSearchCarrier, SerpItemResult<DefaultSerpItem>> {
    private DocModInfo defaultDocModInfo;

    public DefaultSerpItemCollector(DocModInfo defaultDocModInfo) {
        super("Default SERP item collector");
        this.defaultDocModInfo = defaultDocModInfo;
    }


    @Override
    public SerpItemResult<DefaultSerpItem> collectResult(SemedicoESSearchCarrier carrier, int responseIndex) {
        final IElasticServerResponse searchResponse = carrier.getSearchResponse(responseIndex);
        final Stream<ISearchServerDocument> documentResults = searchResponse.getDocumentResults();
        return null;
    }

    private DefaultSerpItem getSerpItemFromServerDocument(ISearchServerDocument document) {
        final DefaultSerpItem serpItem = new DefaultSerpItem(defaultDocModInfo, document.getId());


        serpItem.addHighlight(FIELD_TITLE, getHighlightOrFieldValue(FIELD_TITLE, document), document.getScore());
        return serpItem;
    }

    /**
     * Tries to get the single highlight for the requested field. If none is available, the original value of the field
     * is returned. Works only with field where a single highlighted item is returned because only the first
     * highlight is returned by the method.
     * @param fieldname
     * @param document
     * @return
     */
    private String getHighlightOrFieldValue(String fieldname, ISearchServerDocument document) {
        if (document.getHighlights().containsKey(fieldname)) {
            return document.getHighlights().get(fieldname).get(0);
        }
        final Optional<String> fieldValue = document.getFieldValue(fieldname);
        return fieldValue.isPresent() ? fieldValue.get() : "No value present";
    }
}
