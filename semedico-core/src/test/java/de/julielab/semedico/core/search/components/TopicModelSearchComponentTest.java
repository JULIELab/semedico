package de.julielab.semedico.core.search.components;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.julielab.elastic.query.components.data.aggregation.ITermsAggregationUnit;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregationResult;
import de.julielab.semedico.core.concepts.TopicTag;
import de.julielab.semedico.core.search.components.data.TopicModelSearchCarrier;
import de.julielab.semedico.core.search.query.TopicModelQuery;
import de.julielab.semedico.core.services.TopicModelService;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import org.easymock.Capture;
import org.easymock.EasyMock;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
public class TopicModelSearchComponentTest {

    @Test
    public void testProcessSearch(){
        // Setup the search carrier which holds the query
        Multiset<TopicTag> tmQueryTags = HashMultiset.create();
        tmQueryTags.add(new TopicTag("patient"));
        tmQueryTags.add(new TopicTag("alzheimer's"));
        TopicModelQuery tmQuery = new TopicModelQuery(tmQueryTags);
        TopicModelSearchCarrier testcarrier = new TopicModelSearchCarrier("testcarrier");
        testcarrier.addQuery(tmQuery);

        // Setup a mock object that plays the role of the TopicModelService
        TopicModelService tmServiceMock = EasyMock.createMock(TopicModelService.class);
        TMSearchResult tmSearchResult = new TMSearchResult();
        tmSearchResult.pubmedID = Collections.singletonList("1234");
        Capture<Document> queryDocCapture = new Capture<Document>();
        EasyMock.expect(tmServiceMock.search(EasyMock.capture(queryDocCapture))).andReturn(tmSearchResult);
        EasyMock.replay(tmServiceMock);

        // Now create the search component with the mocked TopicModelService
        TopicModelSearchComponent tmComponent = new TopicModelSearchComponent(LoggerFactory.getLogger(TopicModelSearchComponent.class), tmServiceMock);

        // Let the component process the search carrier
        tmComponent.processSearch(testcarrier);

        // First check the query document we have "caught". It is derived from the tmQuery and should contain the
        // query words
        assertThat(queryDocCapture.getValue().text).contains("patient",  "alzheimer's");
        assertThat(testcarrier.getSearchResponses()).hasSize(1);
        assertThat(testcarrier.getSearchResponse(0)).extracting(response -> response.getAggregationResult(null)).isNotNull();
        TermsAggregationResult termsAgg = testcarrier.getSearchResponse(0).getAggregationResult(null);
        assertThat(termsAgg.getAggregationUnits()).isNotEmpty();
        // Finally: Check that the expected aggregation result, the document ID list, is returned
        assertThat(termsAgg.getAggregationUnits()).extracting(ITermsAggregationUnit::getTerm).contains("1234");
    }
}
