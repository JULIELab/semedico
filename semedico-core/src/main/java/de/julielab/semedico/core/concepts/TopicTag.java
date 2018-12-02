package de.julielab.semedico.core.concepts;

import de.julielab.semedico.core.facets.Facet;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Topic Tags are just words that are interpreted as lexical representation of a topic that was automatically derived
 * from a document collection using topic modeling techniques. In this model, topics are represented by the words
 * in the document collection vocabulary that describe the topic best. In this way, the topic words - here called
 * topic tags - induce corresponding topics. Topics can then be used for ranking or filtering purposes.
 */
public class TopicTag implements IConcept {

    /**
     * This character is used to represent words that are interpreted as as parts of topics in the input query and also
     * internally. Thus, the word 'archea' that should be used to represent the topic complex revolving around archea
     * would be represented as '#archea'. If such a representation is required verbatim you may use the convencience
     * method {@link #tt(String)} that just prepends this character to a given word.
     */
    public static final String TOPIC_TAG_CHAR = "#";
    private String word;

    public TopicTag(String word) {
        setPreferredName(word);
    }

    public static String tt(String word) {
        return TOPIC_TAG_CHAR + word;
    }

    /**
     * The query word representing the topic word.
     *
     * @return
     */
    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    @Override
    public ConceptType getConceptType() {
        return ConceptType.TOPIC_TAG;
    }

    @Override
    public void addFacet(Facet facet) {

    }

    @Override
    public Facet getFirstFacet() {
        return null;
    }

    @Override
    public List<Facet> getFacets() {
        return null;
    }

    @Override
    public void setFacets(List<Facet> facets) {

    }

    /**
     * The ID of a TopicTag is just its word prepended by the {@link #TOPIC_TAG_CHAR} character.
     *
     * @return This ID of the TopicTag concept.
     */
    @Override
    public String getId() {
        return tt(word);
    }

    /**
     * Sets this word's word to <code>id</code> if it begins with {@link #TOPIC_TAG_CHAR}. Else, the
     * character is first prepended before setting the word.
     *
     * @param id The new ID of the topic model. This also determines its name.
     * @see #setPreferredName(String)
     */
    @Override
    public void setId(String id) {
        setPreferredName(id);
    }

    /**
     * The word of this topic tag, i.e. without the # prefix.
     *
     * @return The topic model word of this tag.
     */
    @Override
    public String getPreferredName() {
        return word;
    }

    /**
     * Sets this tag's word to <code>preferredName</code> after all leading # characters have been removed.
     *
     * @param preferredName The new word of the topic tag. This also determines its ID.
     * @see #setId(String)
     */
    @Override
    public void setPreferredName(String preferredName) {
        Matcher hashMatcher = Pattern.compile("#*(.*)").matcher(preferredName);
        hashMatcher.find();
        word = hashMatcher.group(1);
    }

    @Override
    public boolean isContainedInFacet(Facet otherFacet) {
        return false;
    }

    @Override
    public List<String> getSynonyms() {
        return null;
    }

    @Override
    public List<String> getDescriptions() {
        return null;
    }

    @Override
    public List<String> getOccurrences() {
        return null;
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public void setDescription(List<String> description) {

    }

    @Override
    public boolean isNonDatabaseConcept() {
        return true;
    }

    @Override
    public void setNonDatabaseConcept(boolean isNonDatabaseTerm) {

    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String[] getQualifiers() {
        return null;
    }

    @Override
    public boolean isCoreTerm() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicTag topicTag = (TopicTag) o;
        return Objects.equals(word, topicTag.word);
    }

    @Override
    public int hashCode() {

        return Objects.hash(word);
    }
}
