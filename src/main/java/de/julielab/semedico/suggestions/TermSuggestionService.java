package de.julielab.semedico.suggestions;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;
import org.apache.lucene.analysis.ngram.EdgeNGramTokenFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import de.julielab.lucene.IIndexReaderWrapper;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetSuggestionHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SuggestionHit;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;

public class TermSuggestionService implements Comparator<SuggestionHit>, ITermSuggestionService{

	private static final int MAX_SUGGESTION_LENGTH = 50;
	private static Logger logger = Logger.getLogger(TermSuggestionService.class);
	private static RAMDirectory directory;
	private Sort sortOrder;
	private int minTokenLength;
	private int maxTokenLength;
	private ITermService termService;
	private ITermOccurrenceFilterService termOccurrenceFilterService;

	public TermSuggestionService(IIndexReaderWrapper indexReaderWrapper) {
		super();
		
		try {
			if( directory == null ){
				Directory fileDirectory = indexReaderWrapper.getIndexReader().directory();

				directory = new RAMDirectory();
				Directory.copy(fileDirectory, directory, true);
			}
		 
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		minTokenLength = -1;
		maxTokenLength = Integer.MAX_VALUE; 
	}
	
	public TermSuggestionService(Directory newDirectory) {
		try {
			if( directory == null ){
				directory = new RAMDirectory();
				Directory.copy(newDirectory, directory, true);
			}
		 
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		minTokenLength = -1;
		maxTokenLength = Integer.MAX_VALUE; 
	}

	/* (non-Javadoc)
	 * @see de.julielab.stemnet.suggestions.ITermSuggestionService#createSuggestions(java.lang.String, java.util.List)
	 */
	public List<FacetSuggestionHit> createSuggestions(String termFragment, List<Facet> facets) throws IOException{		
		IndexSearcher searcher = new IndexSearcher(directory);
		QueryParser queryParser = new QueryParser(Version.LUCENE_23, Fields.SUGGESTION, new KeywordAnalyzer());
		HashMap<Facet, FacetSuggestionHit> facetHits = new HashMap<Facet, FacetSuggestionHit>();
		ArrayList<FacetSuggestionHit> resultList = new ArrayList<FacetSuggestionHit>();
		String[] words = termFragment.split("\\s");
		String termQuery = "";
		for( String word : words ){	
			if( word.length() < minTokenLength )
				continue;
			else if( word.length() > maxTokenLength)
				word = word.substring(0, maxTokenLength);
			
			// The Lucene parser will throw an error when a not-quoted term is searched which begins with a minus sign.
			if (word.startsWith("-"))
				word = "\"" + word + "\"";
			
			termQuery += "+"+Fields.SUGGESTION+":"+ word.toLowerCase() + " ";
		}
		try {
			
			for( Facet facet: facets ){
				
				String query = termQuery +  "+"+Fields.FACET + ":\"" + facet.getName() +"\"";
				TopScoreDocCollector collector = TopScoreDocCollector.create(MAX_SUGGESTION_LENGTH, true);
				   searcher.search(queryParser.parse(query), collector);
				   ScoreDoc[] hits = collector.topDocs().scoreDocs;

							
				if( hits.length == 0 )
					continue;
				
				FacetSuggestionHit facetHit = new FacetSuggestionHit(facet);
				facetHit.setCompleteSize(0);
				facetHits.put(facet, facetHit);
				resultList.add(facetHit);

				List<String> resources = new ArrayList<String>();
				
				for( int i = 0; i <  hits.length && resources.size() < 50; i++ ){					
					
					Document doc = searcher.doc(hits[i].doc);

					doc.getValues(Fields.TERM_IDENTIFIER);
					String identifier = doc.get(Fields.TERM_IDENTIFIER);
					
					if( resources.contains(identifier) ){
						continue;
					}
					
					facetHit.setCompleteSize(facetHit.getCompleteSize()+1);					
					String index = doc.get(Fields.INDEX);
					resources.add(identifier);
					SuggestionHit suggestionHit = new SuggestionHit(doc.get(Fields.SUGGESTION));					
						
					suggestionHit.setIdentifier(identifier);
					suggestionHit.setIndex(index);
					suggestionHit.setShortDescription(doc.get(Fields.SHORT_DESCRIPTION));
					
					//termHit.setFrequency(new Integer(doc.get(TermIndexer.Fields.FREQUENCY)));
					facetHit.getTermHits().add(suggestionHit);
				    
				}
				resources.clear();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new IllegalStateException(e);
		}
		
		return resultList;
	}

	/* (non-Javadoc)
	 * @see de.julielab.stemnet.suggestions.ITermSuggestionService#termCount()
	 */
	public int termCount(){
		int count = 0;
		try {
			count = IndexReader.open(directory).numDocs();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		return count;
	}
	
	@Override
	public void createSuggestionIndex(String path) throws IOException, SQLException {
		NGramAnalyzer analyzer = null;
		if( maxTokenLength > 0 )
			analyzer = new NGramAnalyzer(maxTokenLength);
		else
			analyzer = new NGramAnalyzer();
		
		IndexWriter writer = new IndexWriter(FSDirectory.open(new File(path)), new IndexWriterConfig(Version.LUCENE_23, analyzer));
		Collection<FacetTerm> terms = termService.getRegisteredTerms();
		int count = 0;
		for( FacetTerm term : terms ){
			if( term.getFacet().getType() != Facet.BIBLIOGRAPHY )
				if( !termService.termOccuredInDocumentIndex(term) ){
					logger.info(term.getInternalIdentifier() + " doesn't occur in the index.");
					continue;
				}

			Collection<String> suggestions = termOccurrenceFilterService.filterTermOccurrences(term, termService.readOccurrencesForTerm(term));
			Collection<String> occurrences = termService.readOccurrencesForTerm(term);

			Integer facetId = term.getFacet().getId();
			if( !(facetId.equals(Facet.FIRST_AUTHOR_FACET_ID) || facetId.equals(Facet.LAST_AUTHOR_FACET_ID)))
				termOccurrenceFilterService.filterTermOccurrences(term, occurrences);

			if( suggestions.size() == 0 ){
				logger.info(term.getInternalIdentifier() + " has no suggestions.");
				continue;
			}
			
			String facet = term.getFacet().getName();

			String shortDescription = term.getShortDescription();
			
		
			String id = term.getInternalIdentifier();
			for( String suggestion : suggestions )
					writer.addDocument(createIndexDocument(suggestion, facet, null, id, shortDescription));

			logger.info(term.getInternalIdentifier() + " indexed.");
			count++;
		}

		logger.info(count + "terms indexed.");
		
		writer.optimize();
		writer.close();		
	}

	private Document createIndexDocument(String term, String facet, String index, String indexedRepresentation, String shortDescription){
		Document doc = new Document();
	
		Field termField = new Field(Fields.SUGGESTION, term, Field.Store.YES, Field.Index.ANALYZED);
		doc.add(termField);
	
		Field facetField = new Field(Fields.FACET, facet, Field.Store.YES, Field.Index.NOT_ANALYZED);
		doc.add(facetField);
	
		Field resourcesField = new Field(Fields.TERM_IDENTIFIER, indexedRepresentation, Field.Store.YES, Field.Index.NO);
		doc.add(resourcesField);		 
	
		if( shortDescription != null ){
			String[] synonyms = shortDescription.split(";");
			String filteredDescription = "";
			for( int i = 0; i< synonyms.length; i++ ){
				
				if( !synonyms[i].trim().equals(term) ){
					filteredDescription += synonyms[i].trim();
					if( i < synonyms.length -1 )
						filteredDescription += ", ";					
				}
			}
			
			Field descriptionField = new Field(Fields.SHORT_DESCRIPTION, filteredDescription, Field.Store.YES, Field.Index.NO);
			doc.add(descriptionField);	
		}
		
		return doc;
	}

	public int compare(SuggestionHit hit1, SuggestionHit hit2) {
		return hit1.getName().compareTo(hit2.getName());
	}

	public int getMinTokenLength() {
		return minTokenLength;
	}

	public void setMinTokenLength(int minTokenLength) {
		this.minTokenLength = minTokenLength;
	}

	public int getMaxTokenLength() {
		return maxTokenLength;
	}

	public void setMaxTokenLength(int maxTokenLength) {
		this.maxTokenLength = maxTokenLength;
	}

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public ITermOccurrenceFilterService getTermOccurrenceFilterService() {
		return termOccurrenceFilterService;
	}

	public void setTermOccurrenceFilterService(
			ITermOccurrenceFilterService termOccurrenceFilterService) {
		this.termOccurrenceFilterService = termOccurrenceFilterService;
	}
	

}
class NGramAnalyzer extends Analyzer {
	
	private int maxTokenLength;
	public final static int DEFAULT_MIN_NGRAM_SIZE = 2;
	public final static int DEFAULT_MAX_NGRAM_SIZE = 10;
	
	public NGramAnalyzer(int maxTokenLength) {
		super();
		this.maxTokenLength = maxTokenLength;
	}
	
	public NGramAnalyzer() {
		super();
		this.maxTokenLength = -1;
	}
	
	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		if( maxTokenLength > 0 )
			return new LowerCaseFilter(new EdgeNGramTokenFilter(new WhitespaceTokenizer(reader), EdgeNGramTokenFilter.Side.FRONT, 
					DEFAULT_MIN_NGRAM_SIZE, maxTokenLength));
		else
			return new LowerCaseFilter(new EdgeNGramTokenFilter(new WhitespaceTokenizer(reader), EdgeNGramTokenFilter.Side.FRONT, 
					DEFAULT_MIN_NGRAM_SIZE, DEFAULT_MAX_NGRAM_SIZE));		
	}
	
}
