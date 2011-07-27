package de.julielab.semedico.suggestions;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTermSuggestionStream;
import de.julielab.semedico.suggestions.ITermSuggestionService.Fields;

public class TermSuggestionServiceTest extends TestCase {

	private ITermSuggestionService suggestionService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		
		Directory directory = new RAMDirectory();
		
		IndexWriter writer = new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_23, new NGramAnalyzer()));
		
		Document doc1 = new Document();
		Field termField = new Field(Fields.SUGGESTION, "term 1 suggestion 1", Field.Store.YES, Field.Index.ANALYZED);
		doc1.add(termField);

		Field facetField = new Field(Fields.FACET, "facet 1", Field.Store.YES, Field.Index.NOT_ANALYZED);
		doc1.add(facetField);

		Field resourcesField = new Field(Fields.TERM_IDENTIFIER, "concept 1", Field.Store.YES, Field.Index.NO);
		doc1.add(resourcesField);		 

		Field indexField = new Field(Fields.INDEX, "index 1", Field.Store.YES, Field.Index.NO);
		doc1.add(indexField);	
		
		writer.addDocument(doc1);

		
		Document doc2 = new Document();
		termField = new Field(Fields.SUGGESTION, "term 2 suggestion 1", Field.Store.YES, Field.Index.ANALYZED);
		doc2.add(termField);

		facetField = new Field(Fields.FACET, "facet 2", Field.Store.YES, Field.Index.NOT_ANALYZED);
		doc2.add(facetField);

		resourcesField = new Field(Fields.TERM_IDENTIFIER, "concept 2", Field.Store.YES, Field.Index.NO);
		doc2.add(resourcesField);		 

		indexField = new Field(Fields.INDEX, "index 2", Field.Store.YES, Field.Index.NO);
		doc2.add(indexField);	

		Field descriptionField = new Field(Fields.SHORT_DESCRIPTION, "short description 2", Field.Store.YES, Field.Index.NO);
		doc2.add(descriptionField);	

		writer.addDocument(doc2);

		Document doc3 = new Document();
		termField = new Field(Fields.SUGGESTION, "term 2 suggestion 2", Field.Store.YES, Field.Index.ANALYZED);
		doc3.add(termField);

		facetField = new Field(Fields.FACET, "facet 2", Field.Store.YES, Field.Index.NOT_ANALYZED);
		doc3.add(facetField);

		resourcesField = new Field(Fields.TERM_IDENTIFIER, "concept 2", Field.Store.YES, Field.Index.NO);
		doc3.add(resourcesField);		 

		indexField = new Field(Fields.INDEX, "index 2", Field.Store.YES, Field.Index.NO);
		doc3.add(indexField);	

		descriptionField = new Field(Fields.SHORT_DESCRIPTION, "short description 2", Field.Store.YES, Field.Index.NO);
		doc3.add(descriptionField);	

		writer.addDocument(doc3);

		Document doc4 = new Document();
		termField = new Field(Fields.SUGGESTION, "term 3 suggestion 1", Field.Store.YES, Field.Index.ANALYZED);
		doc4.add(termField);

		facetField = new Field(Fields.FACET, "facet 3", Field.Store.YES, Field.Index.NOT_ANALYZED);
		doc4.add(facetField);

		resourcesField = new Field(Fields.TERM_IDENTIFIER, "concept 3", Field.Store.YES, Field.Index.NO);
		doc4.add(resourcesField);		 

		indexField = new Field(Fields.INDEX, "index 3", Field.Store.YES, Field.Index.NO);
		doc4.add(indexField);	

		writer.addDocument(doc4);

		Document doc5 = new Document();
		termField = new Field(Fields.SUGGESTION, "term 4 suggestion 1", Field.Store.YES, Field.Index.ANALYZED);
		doc5.add(termField);

		facetField = new Field(Fields.FACET, "facet 3", Field.Store.YES, Field.Index.NOT_ANALYZED);
		doc5.add(facetField);

		resourcesField = new Field(Fields.TERM_IDENTIFIER, "concept 4", Field.Store.YES, Field.Index.NO);
		doc5.add(resourcesField);		 

		indexField = new Field(Fields.INDEX, "index 3", Field.Store.YES, Field.Index.NO);
		doc5.add(indexField);	
		writer.addDocument(doc5);
		
		writer.optimize();
		writer.close();
		
		suggestionService = new TermSuggestionService(directory);
	}
	
	public void testCreateSuggestions() throws Exception {
		List<Facet> facets = new ArrayList<Facet>();
		facets.add(new Facet("facet 1"));
		facets.add(new Facet("facet 2"));
		facets.add(new Facet("facet 3"));
		
		List<FacetTermSuggestionStream> facetHits = suggestionService.getSuggestionsForFragment("ter", facets);
		
		assertNotNull(facetHits);
		assertEquals(3, facetHits.size());
		
		FacetTermSuggestionStream facetHit1 = facetHits.get(0);
		FacetTermSuggestionStream facetHit2 = facetHits.get(1);
		FacetTermSuggestionStream facetHit3 = facetHits.get(2);
		
		assertEquals("facet 1", facetHit1.getFacet().getName());
		assertEquals("facet 2", facetHit2.getFacet().getName());
		assertEquals("facet 3", facetHit3.getFacet().getName());
		
		assertEquals(1, facetHit1.getTermHits().size());
		assertEquals(1, facetHit2.getTermHits().size());
		assertEquals(2, facetHit3.getTermHits().size());
		
		assertEquals("term 1 suggestion 1", facetHit1.getTermHits().get(0).getName());		
		assertEquals("term 2 suggestion 1", facetHit2.getTermHits().get(0).getName());		
		assertEquals("term 3 suggestion 1", facetHit3.getTermHits().get(0).getName());
				
		
		SuggestionHit termHit1 = facetHit1.getTermHits().get(0);
		SuggestionHit termHit3 = facetHit3.getTermHits().get(0);
		SuggestionHit termHit4 = facetHit2.getTermHits().get(0);
		
		assertEquals("concept 1", termHit1.getIdentifier());
		assertEquals("concept 3", termHit3.getIdentifier());
		assertEquals("concept 2", termHit4.getIdentifier());
		assertEquals("short description 2", termHit4.getShortDescription());
		
		assertEquals("index 1", termHit1.getIndex());
		//assertEquals((Integer)3, termHit1.getFrequency());
	}
	
}
