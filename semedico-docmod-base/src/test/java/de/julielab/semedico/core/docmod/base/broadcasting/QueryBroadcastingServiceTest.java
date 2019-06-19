package de.julielab.semedico.core.docmod.base.broadcasting;


import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;
import de.julielab.semedico.core.docmod.base.defaultmodule.entities.DefaultSerpItemCollector;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocModQueryService;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.docmod.base.services.IDocModQueryService;
import de.julielab.semedico.core.docmod.base.services.QueryBroadcastingService;
import de.julielab.semedico.core.entities.docmods.DocModInfo;
import de.julielab.semedico.core.entities.docmods.DocumentPart;
import de.julielab.semedico.core.entities.documents.SemedicoIndexField;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.AggregationRequests;
import de.julielab.semedico.core.search.query.ParseTreeQueryBase;
import de.julielab.semedico.core.search.results.SearchResultCollector;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;
import de.julielab.semedico.core.search.results.collectors.FieldTermCollector;
import org.apache.tapestry5.ioc.internal.services.ChainBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.PlasticProxyFactoryImpl;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertNotNull;

public class QueryBroadcastingServiceTest {
    @Test
    public void testBroadcast() {
        final PlasticProxyFactoryImpl plasticProxyFactory = new PlasticProxyFactoryImpl(getClass().getClassLoader(), null);
        final ChainBuilderImpl chainBuilder = new ChainBuilderImpl(plasticProxyFactory);
        final SemedicoIndexField field1 = new SemedicoIndexField("searched1");
        final SemedicoIndexField field2 = new SemedicoIndexField("searched2");
        final DocumentPart alltextPart1 = new DocumentPart("All Text 1", "testalltextindex1", Arrays.asList(field1), Arrays.asList("requested1"));
        final DocumentPart alltextPart2 = new DocumentPart("All Text 2", "testalltextindex2", Arrays.asList(field2), Arrays.asList("requested2"));
        final DocModInfo docModInfo1 = new DocModInfo("testdocmod1", Arrays.asList(alltextPart1));
        final DocModInfo docModInfo2 = new DocModInfo("testdocmod2", Arrays.asList(alltextPart2));
        final DefaultDocModQueryService docModQueryService1 = new DefaultDocModQueryService(LoggerFactory.getLogger(DefaultDocModQueryService.class), docModInfo1, null);
        final DefaultDocModQueryService docModQueryService2 = new DefaultDocModQueryService(LoggerFactory.getLogger(DefaultDocModQueryService.class), docModInfo2, null);
        final IDocModQueryService queryServiceChain = chainBuilder.build(IDocModQueryService.class, Arrays.asList(docModQueryService1, docModQueryService2));
        final QueryBroadcastingService broadcastingService = new QueryBroadcastingService(queryServiceChain);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(
                new ParseTree(new TextNode("some term"), null), 
                "nonsenseindex", Collections.emptyList(),
                Arrays.asList("nonsenserequiredfields"));

        final QueryTarget target1 = new QueryTarget("testdocmod1", alltextPart1);
        final QueryTarget target2 = new QueryTarget("testdocmod2", alltextPart2);
        final FieldTermAggregationBroadcast fieldTermAggregationBroadcast = new FieldTermAggregationBroadcast(7, AggregationRequests.OrderType.COUNT, AggregationRequest.OrderCommand.SortOrder.DESCENDING);
        // Request facet terms creation
        final FieldTermCollectorBroadcast fieldTermCollectorBroadcast = new FieldTermCollectorBroadcast(fieldTermAggregationBroadcast.getAggregationBaseName());
        // Add the faceting result collector and a SERP item collector
        final List<IResultCollectorBroadcast> resultCollectorBroadCasts = Arrays.asList(fieldTermCollectorBroadcast, new SerpItemCollectorBroadcast());

        final QueryBroadcastResult broadcastResult = broadcastingService.broadcastQuery(query, Arrays.asList(target1, target2), Arrays.asList(fieldTermAggregationBroadcast), resultCollectorBroadCasts);

        assertNotNull(broadcastingService);
        assertThat(broadcastResult.getQueries()).hasSize(2);
        // Check properties of the first generated query
        final ParseTreeQueryBase firstQuery = (ParseTreeQueryBase) broadcastResult.getQueries().get(0);
        assertThat(firstQuery.getIndex()).isEqualTo("testalltextindex1");
        assertThat(firstQuery.getSearchedFields()).containsExactly(field1);
        assertThat(firstQuery.getRequestedFields()).containsExactly("requested1");
        assertThat(firstQuery.getAggregationRequests().size()).isEqualTo(1);
        assertThat(firstQuery.getAggregationRequests().containsKey(fieldTermAggregationBroadcast.getAggregationBaseName()+DefaultDocModQueryService.BROADCAST_SUFFIX));
        final TermsAggregation aggregationRequest1 = (TermsAggregation) firstQuery.getAggregationRequests().get(fieldTermAggregationBroadcast.getAggregationBaseName() + DefaultDocModQueryService.BROADCAST_SUFFIX);
        assertThat(aggregationRequest1.name).isEqualTo(fieldTermAggregationBroadcast.getAggregationBaseName() + DefaultDocModQueryService.BROADCAST_SUFFIX);
        assertThat(aggregationRequest1.field).isEqualTo(DefaultDocumentModule.FIELD_FACETS);

        // Check properties of the second generated query
        final ParseTreeQueryBase secondQuery = (ParseTreeQueryBase) broadcastResult.getQueries().get(1);
        assertThat(secondQuery.getIndex()).isEqualTo("testalltextindex2");
        assertThat(secondQuery.getSearchedFields()).containsExactly(field2);
        assertThat(secondQuery.getRequestedFields()).containsExactly("requested2");
        assertThat(secondQuery.getAggregationRequests().size()).isEqualTo(1);
        assertThat(secondQuery.getAggregationRequests().containsKey(fieldTermAggregationBroadcast.getAggregationBaseName()+DefaultDocModQueryService.BROADCAST_SUFFIX));
        final TermsAggregation aggregationRequest2 = (TermsAggregation) secondQuery.getAggregationRequests().get(fieldTermAggregationBroadcast.getAggregationBaseName() + DefaultDocModQueryService.BROADCAST_SUFFIX);
        assertThat(aggregationRequest2.name).isEqualTo(fieldTermAggregationBroadcast.getAggregationBaseName() + DefaultDocModQueryService.BROADCAST_SUFFIX);
        assertThat(aggregationRequest2.field).isEqualTo(DefaultDocumentModule.FIELD_FACETS);

        // Check the result collectors. First the collectors for the first query.
        final List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>> resultCollectorsQ1 = broadcastResult.getResultCollectors(firstQuery);
        assertThat(resultCollectorsQ1).hasSize(2);
        final FieldTermCollector fieldTermCollector1 = (FieldTermCollector) resultCollectorsQ1.get(0);
        assertThat(fieldTermCollector1.getName()).isEqualTo(FieldTermCollectorBroadcast.FIELDTERMSCOLLECTOR_NAME + DefaultDocModQueryService.BROADCAST_SUFFIX);
        assertThat(fieldTermCollector1.getAggregationNames()).containsExactly(FieldTermAggregationBroadcast.FIELDTERMS_NAME+DefaultDocModQueryService.BROADCAST_SUFFIX);

        assertThat(resultCollectorsQ1.get(1)).isOfAnyClassIn(DefaultSerpItemCollector.class);

        // Now check the result collectors of the second.
        final List<SearchResultCollector<? extends ISemedicoSearchCarrier<?, ?>, ? extends SemedicoSearchResult>> resultCollectorsQ2 = broadcastResult.getResultCollectors(firstQuery);
        assertThat(resultCollectorsQ2).hasSize(2);
        final FieldTermCollector fieldTermCollector2 = (FieldTermCollector) resultCollectorsQ2.get(0);
        // The names are actually the same as for the first query because we used the DefaultDocModQueryService for both.
        assertThat(fieldTermCollector2.getName()).isEqualTo(FieldTermCollectorBroadcast.FIELDTERMSCOLLECTOR_NAME + DefaultDocModQueryService.BROADCAST_SUFFIX);
        assertThat(fieldTermCollector2.getAggregationNames()).containsExactly(FieldTermAggregationBroadcast.FIELDTERMS_NAME+DefaultDocModQueryService.BROADCAST_SUFFIX);

        assertThat(resultCollectorsQ2.get(1)).isOfAnyClassIn(DefaultSerpItemCollector.class);
    }
}
