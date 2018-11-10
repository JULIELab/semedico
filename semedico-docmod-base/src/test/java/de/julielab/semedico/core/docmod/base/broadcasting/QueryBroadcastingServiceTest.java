package de.julielab.semedico.core.docmod.base.broadcasting;


import de.julielab.elastic.query.components.data.aggregation.AggregationRequest;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocModQueryService;
import de.julielab.semedico.core.docmod.base.defaultmodule.services.DefaultDocumentModule;
import de.julielab.semedico.core.docmod.base.entities.DocModInfo;
import de.julielab.semedico.core.docmod.base.entities.DocumentPart;
import de.julielab.semedico.core.docmod.base.entities.QueryTarget;
import de.julielab.semedico.core.docmod.base.services.DocModInformationService;
import de.julielab.semedico.core.docmod.base.services.IDocModInformationService;
import de.julielab.semedico.core.docmod.base.services.IDocModQueryService;
import de.julielab.semedico.core.docmod.base.services.QueryBroadcastingService;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.query.AggregationRequests;
import de.julielab.semedico.core.search.query.ParseTreeQueryBase;
import org.apache.lucene.index.Term;
import org.apache.tapestry5.ioc.internal.services.ChainBuilderImpl;
import org.apache.tapestry5.ioc.internal.services.PlasticProxyFactoryImpl;
import org.apache.tapestry5.ioc.services.PlasticProxyFactory;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

public class QueryBroadcastingServiceTest {
    @Test
    public void testBroadcast() {
        final PlasticProxyFactoryImpl plasticProxyFactory = new PlasticProxyFactoryImpl(getClass().getClassLoader(), null);
        final ChainBuilderImpl chainBuilder = new ChainBuilderImpl(plasticProxyFactory);
        final DocumentPart alltextPart1 = new DocumentPart("All Text 1", "testalltextindex1");
        final DocumentPart alltextPart2 = new DocumentPart("All Text 2", "testalltextindex2");
        final DocModInfo docModInfo1 = new DocModInfo("testdocmod1", Arrays.asList(alltextPart1));
        final DocModInfo docModInfo2 = new DocModInfo("testdocmod2", Arrays.asList(alltextPart2));
        final DefaultDocModQueryService docModQueryService1 = new DefaultDocModQueryService(LoggerFactory.getLogger(DefaultDocModQueryService.class), docModInfo1, null);
        final DefaultDocModQueryService docModQueryService2 = new DefaultDocModQueryService(LoggerFactory.getLogger(DefaultDocModQueryService.class), docModInfo2, null);
        final IDocModQueryService queryServiceChain = chainBuilder.build(IDocModQueryService.class, Arrays.asList(docModQueryService1, docModQueryService2));
        final DocModInformationService docModInformationService = new DocModInformationService(Arrays.asList(docModInfo1));
        final QueryBroadcastingService broadcastingService = new QueryBroadcastingService(queryServiceChain, docModInformationService);

        final ParseTreeQueryBase query = new ParseTreeQueryBase(
                new ParseTree(new TextNode("some term"), null), 
                "nonsenseindex", 
                Arrays.asList("nonsenserequiredfields"));

        final QueryTarget target1 = new QueryTarget("testdocmod1", alltextPart1);
        final QueryTarget target2 = new QueryTarget("testdocmod2", alltextPart2);
        final FieldTermAggregationBroadcast fieldTermAggregationBroadcast = new FieldTermAggregationBroadcast(7, AggregationRequests.OrderType.COUNT, AggregationRequest.OrderCommand.SortOrder.DESCENDING);
        new FieldTermCollectorBroadcast()


        broadcastingService.broadcastQuery(query, Arrays.asList(target1, target2) , Arrays.asList(fieldTermAggregationBroadcast), )
        
        
    }
}
