package de.julielab.semedico.core.docmod.base.defaultmodule.entities;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.entities.docmods.DocModInfo;
import de.julielab.semedico.core.docmod.base.entities.SerpItemResult;
import de.julielab.semedico.core.docmod.base.services.IHighlightingService;
import de.julielab.semedico.core.search.components.data.SemedicoESSearchCarrier;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.searchresponse.IElasticServerResponse;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule.*;

public class DefaultSerpItemCollector extends SearchResultCollector<SemedicoESSearchCarrier, SerpItemResult<DefaultSerpItem>> {
    private DocModInfo defaultDocModInfo;
    private IHighlightingService hlService;

    public DefaultSerpItemCollector(DocModInfo defaultDocModInfo, IHighlightingService hlService) {
        super("Default SERP item collector");
        this.defaultDocModInfo = defaultDocModInfo;
        this.hlService = hlService;
    }


    @Override
    public SerpItemResult<DefaultSerpItem> collectResult(SemedicoESSearchCarrier carrier, int responseIndex) {
        final IElasticServerResponse searchResponse = carrier.getSearchResponse(responseIndex);
        final Stream<ISearchServerDocument> documentResults = searchResponse.getDocumentResults();
        final List<DefaultSerpItem> items = documentResults.map(this::getSerpItemFromServerDocument).collect(Collectors.toList());
        return new SerpItemResult<>(items);
    }

    private DefaultSerpItem getSerpItemFromServerDocument(ISearchServerDocument document) {
        final DefaultSerpItem serpItem = new DefaultSerpItem(defaultDocModInfo, document.getId());

        serpItem.addHighlight(FIELD_TITLE, hlService.getFieldHighlights(document, FIELD_TITLE, false).single());
        serpItem.addHighlight(FIELD_AUTHORS, hlService.getAuthorHighlights(document, FIELD_AUTHORS));
        serpItem.addHighlight(FIELD_ALL_TEXT, hlService.getFieldHighlights(document, FIELD_ALL_TEXT, false, true, false, false, 200));
        return serpItem;
    }
}
