package de.julielab.semedico.core.facets;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.julielab.neo4j.plugins.datarepresentation.ImportFacet;
import de.julielab.neo4j.plugins.datarepresentation.constants.FacetConstants;
import de.julielab.semedico.commons.concepts.SemedicoFacetConstants;
import org.apache.tapestry5.json.JSONObject;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
public class FacetTest {

    @Test
    public void testDeserialization() throws IOException {
        ImportFacet importFacet = new ImportFacet(null, "custom ID", "facet name", "shortname", FacetConstants.SRC_TYPE_HIERARCHICAL, Collections.singletonList("FACET"), false);
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(importFacet);
        JSONObject jsonFacet = new JSONObject(json);
        jsonFacet.put(SemedicoFacetConstants.PROP_SOURCE_NAME, "facet field");
        json = jsonFacet.toString();

        Facet facet = mapper.readValue(json, Facet.class);
        assertThat(facet).isNotNull();
        assertThat(facet.getCustomId()).isEqualTo("custom ID");
        assertThat(facet.getName()).isEqualTo("facet name");
        assertThat(facet.getShortName()).isEqualTo("shortname");
        assertThat(facet.getSource()).isNotNull();
        assertThat(facet.getSource().isHierarchic()).isTrue();
        assertThat(facet.getSource().getName()).isEqualTo("facet field");
    }
}
