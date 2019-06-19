package de.julielab.semedico.core.concepts;

import de.julielab.neo4j.plugins.datarepresentation.constants.NodeIDPrefixConstants;
import de.julielab.semedico.core.util.ConceptCreationException;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
public class ConceptCreatorTest {

    @Test
    public void testCreateConceptFromDescription() throws ConceptCreationException {
        final ConceptCreator creator = new ConceptCreator(null);
        final ConceptDescription desc = new ConceptDescription();
        desc.setId(NodeIDPrefixConstants.TERM + 1);
        desc.setPreferredName("CONCEPT ONE");
        desc.setChildrenInFacets(new String[]{NodeIDPrefixConstants.FACET + 1});
        desc.setWritingVariants(new String[]{"variant1", "variant2"});

        final DatabaseConcept concept = creator.createConceptFromDescription(desc, DatabaseConcept.class);
        assertThat(concept.getId()).isEqualTo(desc.getId());
        assertThat(concept.getPreferredName()).isEqualTo(desc.getPreferredName());
        assertThat(concept.hasChildrenInFacet(NodeIDPrefixConstants.FACET + 1));
        assertThat(concept.getWritingVariants()).containsExactly("variant1", "variant2");
    }
}
