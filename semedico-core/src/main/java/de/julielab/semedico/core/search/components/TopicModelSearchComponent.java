package de.julielab.semedico.core.search.components;

import com.google.common.collect.Multiset;
import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.ISearchServerComponent;
import de.julielab.java.utilities.prerequisites.PrerequisiteChecker;
import de.julielab.semedico.core.concepts.TopicTag;
import de.julielab.semedico.core.search.components.data.TopicModelSearchCarrier;
import de.julielab.semedico.core.search.query.TopicModelQuery;
import de.julielab.semedico.core.search.searchresponse.TopicModelSearchResponse;
import de.julielab.semedico.core.services.interfaces.ITopicModelService;
import de.julielab.topicmodeling.businessobjects.Document;
import de.julielab.topicmodeling.businessobjects.TMSearchResult;
import org.slf4j.Logger;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.stream.Collectors;

public class TopicModelSearchComponent extends AbstractSearchComponent<TopicModelSearchCarrier>
        implements ISearchServerComponent<TopicModelSearchCarrier> {

    private ITopicModelService topicModelService;

    public TopicModelSearchComponent(Logger log, ITopicModelService topicModelService) {
        super(log);
        this.topicModelService = topicModelService;
    }

    @Override
    protected boolean processSearch(TopicModelSearchCarrier carrier) {
        PrerequisiteChecker.checkThat().notEmpty(carrier.getQueries()).withNames("Queries").execute();
        for (TopicModelQuery semedicoQuery : carrier.getQueries()) {
            TopicModelQuery query = semedicoQuery;
            Multiset<TopicTag> words = query.getQuery();
            Document queryDocument = new Document();
            queryDocument.text = words.stream().map(TopicTag::getPreferredName).collect(Collectors.joining(" "));
            TMSearchResult result = topicModelService.search(queryDocument);

            carrier.addSearchResponse(new TopicModelSearchResponse(result));
        }
        return false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface TopicModelSearch {
        //
    }
}
