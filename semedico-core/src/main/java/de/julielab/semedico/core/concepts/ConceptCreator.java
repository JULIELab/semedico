package de.julielab.semedico.core.concepts;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.TermLabels.GeneralLabel;
import de.julielab.semedico.core.concepts.interfaces.IHierarchicalConcept;
import de.julielab.semedico.core.services.interfaces.IConceptCreator;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.util.ConceptCreationException;

import java.io.IOException;
import java.util.Arrays;

public class ConceptCreator implements IConceptCreator {
    /**
     * This object is able to read JSON strings and return deserialized objects that correspond to the JSON input value.
     * The use of the <tt>ObjectReader</tt> is thread safe.
     */
    private ObjectReader objectReader;
    private IFacetService facetService;

    public ConceptCreator(IFacetService facetService) {
        this.facetService = facetService;
        // The ObjectMapper is the general JSON-read-write-object. We will just get an ObjectReader (below), because we
        // don't want to write JSON, we only want to read the data received from the term database.
        ObjectMapper objectMapper = new ObjectMapper();
        // To extend the mapper, we use "modules". Here we just take a module to add our own TermDeserializer.
        SimpleModule module = new SimpleModule();
        // Now we add our custom "deserializerModifier". Seemingly, the BeanDeserializerModifier is the right place to
        // start. The method "modifyDeserializer", that we override, gets a default deserializer. If we have to
        // deserialize a (Sync)FacetTerm, we create our own, custom deserializer and give to default deserializer to it
        // as a constructor argument. This way, we first can use the normal deserialization and then add the additional
        // logic we need to create a fully functional <tt>Concept</tt> object.
        module.setDeserializerModifier(new BeanDeserializerModifier() {
            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
                                                          final JsonDeserializer<?> deserializer) {
                if (beanDesc.getBeanClass() == SyncDbConcept.class)
                    return new ConceptDeserializer<SyncDbConcept>(deserializer);
                else if (beanDesc.getBeanClass() == DatabaseConcept.class)
                    return new ConceptDeserializer<>(deserializer);
                else if (beanDesc.getBeanClass() == AggregateTerm.class)
                    return new ConceptDeserializer<>(deserializer);
                return deserializer;
            }
        });
        objectMapper.registerModule(module);
        objectReader = objectMapper.readerFor(SyncDbConcept.class);
    }

    @Override
    public void updateProxyConceptFromJson(IHierarchicalConcept proxy, String jsonString, JsonNode termLabels) {
        try {
            objectReader.withValueToUpdate(proxy).readValue(jsonString);
            handleConceptLabels(proxy, termLabels);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void updateProxyConceptFromDescription(IHierarchicalConcept proxy, ConceptDescription description) {
        ((SyncDbConcept) proxy).copyValuesFromDescription(description);
        proxy.setFacets(facetService.getFacetsById(Arrays.asList(description.getFacetIds())));
        handleConceptLabels(proxy, description);
    }

    private void handleConceptLabels(IHierarchicalConcept proxy, JsonNode labelsArray) {
        for (int i = 0; i < labelsArray.size(); i++) {
            String labelName = labelsArray.get(i).textValue();
            try {
                GeneralLabel label = TermLabels.GeneralLabel.valueOf(labelName);
                // label-depending action, if any becomes necessary (not currently the case, but in the past for event concepts, which is why this method exists)
            } catch (IllegalArgumentException e) {// Nothing, just skip unknown labels, they probably are good for
                // other purpose in resources management.
            }
        }
    }

    private void handleConceptLabels(IHierarchicalConcept proxy, ConceptDescription description) {
        if (description.getLabels() != null) {
            for (String labelName : description.getLabels()) {
                try {
                    GeneralLabel label = TermLabels.GeneralLabel.valueOf(labelName);
                    // label-depending action, if any becomes necessary (not currently the case, but in the past for event concepts, which is why this method exists)
                } catch (IllegalArgumentException e) {// Nothing, just skip unknown labels, they probably are good for
                    // other purpose in resources management.
                }
            }
        }
    }

    @Override
    public IHierarchicalConcept createConceptFromJson(String jsonString, JsonNode termLabels) {
        boolean isAggregate = false;
        // We have to do this here rather than in 'handleConceptLabels' because we need a different class if we have an
        // aggregate
        for (int i = 0; i < termLabels.size(); i++) {
            String labelName = termLabels.get(i).textValue();
            try {
                GeneralLabel label = TermLabels.GeneralLabel.valueOf(labelName);
                if (label == GeneralLabel.AGGREGATE)
                    isAggregate = true;
            } catch (IllegalArgumentException e) {// Nothing, just skip unknown labels, they probably are good for
                // other purpose in resources management.
            }
        }
        IHierarchicalConcept term;
        if (isAggregate) {
            term = createConceptFromJson(jsonString, termLabels, AggregateTerm.class);
        } else {
            term = createConceptFromJson(jsonString, termLabels, DatabaseConcept.class);
        }
        return term;
    }

    @Override
    public IHierarchicalConcept createConceptFromJsonNodes(JsonNode conceptTree, JsonNode conceptLabelsNode) {
        boolean isAggregate = false;
        // We have to do this here rather than in 'handleConceptLabels' because we need a different class if we have an
        // aggregate
        for (int i = 0; i < conceptLabelsNode.size() && !isAggregate; i++) {
            String labelName = conceptLabelsNode.get(i).textValue();
            try {
                GeneralLabel label = TermLabels.GeneralLabel.valueOf(labelName);
                if (label == GeneralLabel.AGGREGATE)
                    isAggregate = true;
            } catch (IllegalArgumentException e) {// Nothing, just skip unknown labels, they probably are good for
                // other purpose in resources management.
            }
        }
        IHierarchicalConcept term;
        if (isAggregate) {
            term = createConceptFromJson(conceptTree, conceptLabelsNode, AggregateTerm.class);
        } else {
            term = createConceptFromJson(conceptTree, conceptLabelsNode, DatabaseConcept.class);
        }
        return term;
    }

    @Override
    public <T extends IHierarchicalConcept> T createConceptFromDescription(ConceptDescription description, Class<T> conceptClass) throws ConceptCreationException {
        T concept;
        try {
            concept = conceptClass.newInstance();
            concept.initializeFromDescription(description);
            if (description.getFacetIds() != null)
                concept.setFacets(facetService.getFacetsById(Arrays.asList(description.getFacetIds())));
            handleConceptLabels(concept, description);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConceptCreationException(e);
        }
        return concept;
    }

    private IHierarchicalConcept createConceptFromJson(JsonNode conceptTree, JsonNode conceptLabelsNode, Class<? extends IHierarchicalConcept> conceptClass) {
        try {
            IHierarchicalConcept term = objectReader.forType(conceptClass).readValue(conceptTree);
            handleConceptLabels(term, conceptLabelsNode);
            return term;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IHierarchicalConcept createConceptFromJson(String jsonString, JsonNode conceptLabels,
                                                      Class<? extends IHierarchicalConcept> conceptClass) {
        try {
            IHierarchicalConcept term = objectReader.forType(conceptClass).readValue(jsonString);
            handleConceptLabels(term, conceptLabels);
            return term;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public IHierarchicalConcept createDatabaseProxyConcept(String id, Class<? extends SyncDbConcept> termClass) {
        try {
            IHierarchicalConcept term = termClass.newInstance();
            term.setId(id);
            return term;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public KeywordConcept createKeywordConcept(String id, String name) {
        KeywordConcept keywordConcept = new KeywordConcept();
        keywordConcept.setId(id);
        keywordConcept.setPreferredName(name);
        return keywordConcept;
    }

    /**
     * <p>
     * A special ConceptDeserializer for the Jackson library. It can be used to deserialize any subtype of FacetTerm.
     * </p>
     * <p>
     * We need such a special deserializer because <tt>FacetTerm</tt> have a list of <tt>Facets</tt> they belong to. But
     * from the term database, we only get the respective facet IDs. The deserializer first deserializes those IDs into
     * the respective field of <tt>FacetTerm</tt>. Then, it gets the <tt>Facet</tt> objects from the facet service and
     * adds them to the <tt>FacetTerm</tt> instance.
     * </p>
     *
     * @param <T>
     * @author faessler
     */
    private class ConceptDeserializer<T extends DatabaseConcept> extends StdDeserializer<T> implements
            ResolvableDeserializer {
        private static final long serialVersionUID = -317202974147116933L;
        private JsonDeserializer<?> defaultDeserializer;

        ConceptDeserializer(JsonDeserializer<?> defaultDeserializer) {
            super(SyncDbConcept.class);
            this.defaultDeserializer = defaultDeserializer;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt, T intoValue) throws IOException {

            ((JsonDeserializer<T>) defaultDeserializer).deserialize(jp, ctxt, intoValue);
            intoValue.setFacets(facetService.getFacetsById(intoValue.getFacetIds()));

            return intoValue;
        }

        @Override
        public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            @SuppressWarnings("unchecked")
            T createdValue = (T) defaultDeserializer.deserialize(jp, ctxt);
            createdValue.setFacets(facetService.getFacetsById(createdValue.getFacetIds()));

            return createdValue;
        }

        @Override
        public void resolve(DeserializationContext ctxt) throws JsonMappingException {
            ((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
        }

    }


}
