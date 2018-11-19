package de.julielab.semedico.core.search.query;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import de.julielab.semedico.core.concepts.TopicTag;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.ServerType;

import java.util.ArrayList;
import java.util.Objects;

import static java.util.stream.Collectors.toCollection;

/**
 * A query to be sent to the topic model. This query is extremely simple, it is just a multiset of topic tags, or words.
 * It is a multiset because a word can be boosted by repeating it in the query. The order of the query words does not
 * matter as the topic model maps them to an ordered vector anyway that is then compared to document vectors.
 */
public class TopicModelQuery implements ISemedicoQuery {

    private Multiset<TopicTag> topicTags;

    /**
     * Extracts all topic tags from <code>parseTree</code>, ignoring all tree structure, and uses those as topic query.
     *
     * @param parseTree The parse tree to extract topic tags from.
     */
    public TopicModelQuery(ParseTree parseTree) {
        topicTags = parseTree.getNodes(parseTree.getRoot(), TextNode.class, new ArrayList<>()).stream().
                map(TextNode.class::cast).
                map(TextNode::getConcepts).
                map(concepts -> concepts.get(0)).
                filter(TopicTag.class::isInstance).
                map(TopicTag.class::cast).
                collect(toCollection(HashMultiset::create));
    }

    public TopicModelQuery(Multiset<TopicTag> topicTags) {

        this.topicTags = topicTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicModelQuery that = (TopicModelQuery) o;
        return Objects.equals(topicTags, that.topicTags);
    }

    @Override
    public int hashCode() {


        return Objects.hash(topicTags);
    }

    public Multiset<TopicTag> getTopicTags() {

        return topicTags;
    }

    public void setTopicTags(Multiset<TopicTag> topicTags) {
        this.topicTags = topicTags;
    }

    @Override
    public <T> T getQuery() {
        return (T) topicTags;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.TOPIC_MODEL;
    }

    @Override
    public TopicModelQuery clone() throws CloneNotSupportedException {
        final TopicModelQuery clone = (TopicModelQuery) super.clone();
        clone.topicTags = HashMultiset.create();
        clone.topicTags.addAll(topicTags);
        return clone;
    }
}
