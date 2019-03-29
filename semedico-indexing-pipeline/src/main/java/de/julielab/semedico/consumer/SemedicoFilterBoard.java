package de.julielab.semedico.consumer;

import de.julielab.jcore.consumer.es.ExternalResource;
import de.julielab.jcore.consumer.es.FilterBoard;
import de.julielab.jcore.consumer.es.filter.*;

import java.util.Map;
import java.util.Set;

public class SemedicoFilterBoard extends FilterBoard {

	public static final String SEMEDICO_BOARD = "SemedicoFilterBoard";
	
	public final static String RESOURCE_HYPERNYMS = "Hypernyms";
	public final static String RESOURCE_MESH_TERM_MAPPING = "MeshTermMapping";
	public final static String RESOURCE_STOPWORDS = "Stopwords";
	public final static String RESOURCE_AGG_FACETS = "AggFacetIds";
	public static final String RESOURCE_EVENT_TERM_PATTERNS = "EventTermPatterns";
	public final static String RESOURCE_VALID_TERMS = "ValidTerms";
	public final static String RESOURCE_TERM_FACETS = "TermFacetIds";
	public final static String RESOURCE_ELEMENTS_AGGREGATES_ID_MAPPING = "ElementsAggregatesIdMapping";

	public StopWordFilter stopWordFilter;
	public SemedicoTermFilter semedicoTermFilter;
	/**
	 * Lowercasing, stopword-Removal, Stemming
	 */
	public FilterChain tokenFilterChain;
	/**
	 * Lowercasing and stemming, no stopword-removal.
	 */
	public FilterChain wordNormalizationChain;
	public FilterChain journalFilterChain;
	public SnowballFilter snowballFilter;
	@Deprecated
	public FilterChain eventFilterChain;
	@Deprecated
	public FilterChain facetRecommenderFilterChain;
	public AddonTermsFilter hypernymsFilter;
	public ReplaceFilter meshTermReplaceFilter;
	public FilterChain meshFilterChain;
	@Deprecated
	public LatinTransliterationFilter latinTransliterationFilter;
	public ReplaceFilter elementsAggregateIdReplaceFilter;
	
	@ExternalResource(key=RESOURCE_HYPERNYMS)
	private Map<String, String[]> hypernyms;
	
	@ExternalResource(key=RESOURCE_MESH_TERM_MAPPING)
	private Map<String, String> meshTermMapping;
	
	@ExternalResource(key=RESOURCE_STOPWORDS, methodName="getStopWords")
	private Set<String> stopwords;
	
	@ExternalResource(key=RESOURCE_ELEMENTS_AGGREGATES_ID_MAPPING)
	private Map<String, String> elementsToAggregatesIdMapping;

	@Override
	public void setupFilters() {
		
		hypernymsFilter = new AddonTermsFilter(hypernyms);
		meshTermReplaceFilter = new ReplaceFilter(meshTermMapping);
		stopWordFilter = new StopWordFilter(stopwords, true);
		semedicoTermFilter = new SemedicoTermFilter();
		snowballFilter = new SnowballFilter();
		latinTransliterationFilter = new LatinTransliterationFilter(true);

		tokenFilterChain = new FilterChain();
		tokenFilterChain.add(latinTransliterationFilter);
		tokenFilterChain.add(new LowerCaseFilter());
		tokenFilterChain.add(stopWordFilter);
		tokenFilterChain.add(new SnowballFilter());
		
		wordNormalizationChain = new FilterChain();
		wordNormalizationChain.add(latinTransliterationFilter);
		// this default stuff should be done by elasticsearch

		journalFilterChain = new FilterChain();
		journalFilterChain.add(new LuceneStandardTokenizerFilter());
		journalFilterChain.add(new LowerCaseFilter());
		journalFilterChain.add(stopWordFilter);
		journalFilterChain.add(snowballFilter);

		meshFilterChain = new FilterChain();
		meshFilterChain.add(meshTermReplaceFilter);
		meshFilterChain.add(semedicoTermFilter);
		meshFilterChain.add(hypernymsFilter);
		meshFilterChain.add(new UniqueFilter());
		
		elementsAggregateIdReplaceFilter = new ReplaceFilter(elementsToAggregatesIdMapping);
	}

}
