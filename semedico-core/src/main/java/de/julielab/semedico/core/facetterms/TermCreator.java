package de.julielab.semedico.core.facetterms;

import java.io.IOException;

import org.apache.tapestry5.json.JSONArray;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.deser.ResolvableDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import de.julielab.semedico.core.TermLabels;
import de.julielab.semedico.core.TermLabels.GeneralLabel;
import de.julielab.semedico.core.concepts.interfaces.IFacetTerm;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermCreator;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class TermCreator implements ITermCreator {
	private ITermService termService;
	/**
	 * This object is able to read JSON strings and return deserialized objects that correspond to the JSON input value.
	 * The use of the <tt>ObjectReader</tt> is thread safe.
	 */
	private ObjectReader objectReader;
	private IFacetService facetService;

	/**
	 * <p>
	 * A special FacetTermDeserializer for the Jackson library. It can be used to deserialize any subtype of FacetTerm.
	 * </p>
	 * <p>
	 * We need such a special deserializer because <tt>FacetTerm</tt> have a list of <tt>Facets</tt> they belong to. But
	 * from the term database, we only get the respective facet IDs. The deserializer first deserializes those IDs into
	 * the respective field of <tt>FacetTerm</tt>. Then, it gets the <tt>Facet</tt> objects from the facet service and
	 * adds them to the <tt>FacetTerm</tt> instance.
	 * </p>
	 * 
	 * @author faessler
	 * @param <T>
	 * 
	 */
	private class FacetTermDeserializer<T extends FacetTerm> extends StdDeserializer<T> implements
			ResolvableDeserializer {
		private static final long serialVersionUID = -317202974147116933L;
		private JsonDeserializer<?> defaultDeserializer;

		public FacetTermDeserializer(JsonDeserializer<?> defaultDeserializer) {
			super(SyncFacetTerm.class);
			this.defaultDeserializer = defaultDeserializer;
		}

		@SuppressWarnings("unchecked")
		@Override
		public T deserialize(JsonParser jp, DeserializationContext ctxt, T intoValue) throws IOException,
				JsonProcessingException {

			((JsonDeserializer<T>) defaultDeserializer).deserialize(jp, ctxt, intoValue);
			intoValue.setFacets(facetService.getFacetsById(intoValue.getFacetIds()));
			intoValue.setTermService(termService);

			return intoValue;
		}

		@Override
		public T deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
			@SuppressWarnings("unchecked")
			T createdValue = (T) defaultDeserializer.deserialize(jp, ctxt);
			createdValue.setFacets(facetService.getFacetsById(createdValue.getFacetIds()));
			createdValue.setTermService(termService);

			return createdValue;
		}

		@Override
		public void resolve(DeserializationContext ctxt) throws JsonMappingException {
			((ResolvableDeserializer) defaultDeserializer).resolve(ctxt);
		}

	}

	public TermCreator(ITermService termService, IFacetService facetService) {
		this.termService = termService;
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
		// logic we need to create a fully functional <tt>FacetTerm</tt> object.
		module.setDeserializerModifier(new BeanDeserializerModifier() {
			@Override
			public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
					final JsonDeserializer<?> deserializer) {
				if (beanDesc.getBeanClass() == SyncFacetTerm.class)
					return new FacetTermDeserializer<SyncFacetTerm>(deserializer);
				else if (beanDesc.getBeanClass() == FacetTerm.class)
					return new FacetTermDeserializer<FacetTerm>(deserializer);
				else if (beanDesc.getBeanClass() == AggregateTerm.class)
					return new FacetTermDeserializer<FacetTerm>(deserializer);
				return deserializer;
			}
		});
		objectMapper.registerModule(module);
		objectReader = objectMapper.reader(SyncFacetTerm.class);
	}

	@Override
	public IFacetTerm createFacetTerm(String id) {
		return createFacetTerm(id, FacetTerm.class);
	};

	@Override
	public void updateFacetTermFromJson(IFacetTerm proxy, String jsonString, JSONArray termLabels) {
		try {
			objectReader.withValueToUpdate(proxy).readValue(jsonString);
			handleTermLabels(proxy, termLabels);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	protected void handleTermLabels(IFacetTerm proxy, JSONArray labelsArray) {
		for (int i = 0; i < labelsArray.length(); i++) {
			String labelName = labelsArray.getString(i);
			try {
				GeneralLabel label = TermLabels.GeneralLabel.valueOf(labelName);
				if (label == GeneralLabel.EVENT_TERM)
					proxy.setIsEventTrigger(true);
			} catch (IllegalArgumentException e) {// Nothing, just skip unknown labels, they probably are good for
													// other purpose in resources management.
			}
		}
	}

	@Override
	public IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels) {
		boolean isAggregate = false;
		// We have to do this here rather than in 'handleTermLabels' because we need a different class if we have an
		// aggregate
		for (int i = 0; i < termLabels.length(); i++) {
			String labelName = termLabels.getString(i);
			try {
				GeneralLabel label = TermLabels.GeneralLabel.valueOf(labelName);
				if (label == GeneralLabel.AGGREGATE)
					isAggregate = true;
			} catch (IllegalArgumentException e) {// Nothing, just skip unknown labels, they probably are good for
													// other purpose in resources management.
			}
		}
		IFacetTerm term = null;
		if (isAggregate) {
			term = createFacetTermFromJson(jsonString, termLabels, AggregateTerm.class);
		} else {
			term = createFacetTermFromJson(jsonString, termLabels, FacetTerm.class);
		}
		return term;
	}

	@Override
	public IFacetTerm createFacetTermFromJson(String jsonString, JSONArray termLabels,
			Class<? extends IFacetTerm> termClass) {
		try {
			IFacetTerm term = objectReader.withType(termClass).readValue(jsonString);
			term.setTermService(termService);
			handleTermLabels(term, termLabels);
			return term;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public IFacetTerm createFacetTerm(String id, Class<? extends IFacetTerm> termClass) {
		try {
			IFacetTerm term = termClass.newInstance();
			if (!termClass.equals(KeywordTerm.class))
				term.setTermService(termService);
			term.setId(id);
			return term;
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public KeywordTerm createKeywordTerm(String id, String name) {
		KeywordTerm keywordTerm = new KeywordTerm();
		keywordTerm.setId(id);
		keywordTerm.setPreferredName(name);
		return keywordTerm;
	}

}
