// Faessler, 20.05.2011: Depricated anyway
///** 
// * TermOccurrenceExtractorServiceTest.java
// * 
// * Copyright (c) 2008, JULIE Lab. 
// * All rights reserved. This program and the accompanying materials 
// * are protected. Please contact JULIE Lab for further information.  
// *
// * Author: landefeld
// * 
// * Current version: //TODO insert current version number 	
// * Since version:   //TODO insert version number of first appearance of this class
// *
// * Creation date: 20.08.2008 
// * 
// * //TODO insert short description
// **/
//
//package de.julielab.stemnet.core.services;
//
//import static org.junit.Assert.assertArrayEquals;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import org.apache.lucene.analysis.Token;
//import org.apache.lucene.analysis.TokenStream;
//import org.apache.lucene.analysis.WhitespaceAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.document.Field.Index;
//import org.apache.lucene.document.Field.Store;
//import org.apache.lucene.index.CorruptIndexException;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.LockObtainFailedException;
//import org.apache.lucene.store.RAMDirectory;
//import org.junit.Before;
//import org.junit.Test;
//
//import de.julielab.jules.lucene.analysis.CollectionTokenStream;
//import de.julielab.stemnet.IndexFieldNames;
//import de.julielab.stemnet.core.FacetTerm;
//import de.julielab.lucene.IndexReaderWrapper;
//
//import static org.easymock.EasyMock.*;
//
//public class TermOccurrenceExtractorServiceTest {
//
//	private IndexReaderWrapper indexReaderWrapper;
//	private FacetTerm term1;
//	private FacetTerm term2;
//	private FacetTerm term3;
//	
//	@Before
//	public void setUp() throws CorruptIndexException, LockObtainFailedException, IOException{
//		Directory directory = new RAMDirectory();
//		IndexWriter writer = new IndexWriter(directory, new WhitespaceAnalyzer());
//		// doc 1
//		List<Token> tokens = new ArrayList<Token>();
//		tokens.add(new Token("token01", 0, 7));
//		tokens.add(new Token("token02", 8, 15));
//
//		Token termToken = new Token("ID1", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//		termToken = new Token("ID2", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//		termToken = new Token("ID3", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//		
//		tokens.add(new Token("token03", 16, 23));
//
//		TokenStream tokenStream = new CollectionTokenStream(tokens);
//		Document document = new Document();
//		Field textField = new Field(IndexFieldNames.TEXT, tokenStream, Field.TermVector.WITH_POSITIONS_OFFSETS);
//		Field abstractField = new Field(IndexFieldNames.ABSTRACT, "token01 token02 token03", Store.YES, Index.NO);
//		
//		document.add(textField);
//		document.add(abstractField);
//		
//		writer.addDocument(document);
//
//		// doc 2
//		tokens = new ArrayList<Token>();
//		tokens.add(new Token("token04", 0, 7));
//
//		tokens.add(new Token("token05", 8, 15));
//		termToken = new Token("ID2", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//
//		tokens.add(new Token("token06", 16, 23));
//
//		tokenStream = new CollectionTokenStream(tokens);
//		document = new Document();
//		textField = new Field(IndexFieldNames.TEXT, tokenStream, Field.TermVector.WITH_POSITIONS_OFFSETS);
//		abstractField = new Field(IndexFieldNames.ABSTRACT, "token04 token05 token06", Store.YES, Index.NO);
//
//		document.add(textField);
//		document.add(abstractField);
//		
//		writer.addDocument(document);
//
//		//doc3
//		tokens = new ArrayList<Token>();
//		tokens.add(new Token("token07", 0, 7));
//		tokens.add(new Token("token05", 8, 15));
//
//		termToken = new Token("ID2", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//
//		tokens.add(new Token("token09", 16, 23));
//
//		tokenStream = new CollectionTokenStream(tokens);
//		document = new Document();
//		textField = new Field(IndexFieldNames.TEXT, tokenStream, Field.TermVector.WITH_POSITIONS_OFFSETS);
//		abstractField = new Field(IndexFieldNames.ABSTRACT, "token07 token05 token09", Store.YES, Index.NO);
//
//		document.add(textField);
//		document.add(abstractField);
//		writer.addDocument(document);
//
//		//doc4
//		tokens = new ArrayList<Token>();
//		tokens.add(new Token("token10", 0, 7));
//		tokens.add(new Token("token05", 8, 15));
//
//		termToken = new Token("ID2", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//
//		tokens.add(new Token("token11", 16, 23));
//
//		tokenStream = new CollectionTokenStream(tokens);
//		document = new Document();
//		textField = new Field(IndexFieldNames.TEXT, tokenStream, Field.TermVector.WITH_POSITIONS_OFFSETS);
//		abstractField = new Field(IndexFieldNames.ABSTRACT, "token10 token05 token11", Store.YES, Index.NO);
//
//		document.add(textField);
//		document.add(abstractField);
//		writer.addDocument(document);
//
//		//doc5
//		tokens = new ArrayList<Token>();
//		tokens.add(new Token("token12", 0, 7));
//		tokens.add(new Token("token13", 8, 15));
//
//		termToken = new Token("ID2", 8, 15);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//
//		tokens.add(new Token("token14", 16, 23));
//		tokens.add(new Token("token13", 24, 31));		
//		termToken = new Token("ID2", 24, 31);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//
//		tokens.add(new Token("token15", 32, 39));
//		termToken = new Token("ID2", 32, 39);
//		termToken.setPositionIncrement(0);
//		tokens.add(termToken);
//
//		tokenStream = new CollectionTokenStream(tokens);
//		document = new Document();
//		textField = new Field(IndexFieldNames.TEXT, tokenStream, Field.TermVector.WITH_POSITIONS_OFFSETS);
//		abstractField = new Field(IndexFieldNames.ABSTRACT, "token12 token13 token14 token13 token15", Store.YES, Index.NO);
//
//		document.add(textField);
//		document.add(abstractField);
//		writer.addDocument(document);
//		
//		writer.optimize();
//		writer.close();
//		
//		IndexReader reader = IndexReader.open(directory);
//		indexReaderWrapper = new IndexReaderWrapper(reader);		
//		
//		term1 = new FacetTerm("ID1");
//		term2 = new FacetTerm("ID2");
//		term3 = new FacetTerm("ID3");
//		
//		term1.setParent(term2);
//		term2.getSubTerms().add(term1);
//		
//		term2.setParent(term3);
//		term3.getSubTerms().add(term2);		
//	}
//
//	@Test
//	public void testExtractMostFrequentOccurrences() throws Exception{
//
//		ITermService termService = createMock(ITermService.class);
//		expect(termService.getTermWithInternalIdentifier(startsWith("token"), null)).andReturn(null).anyTimes();
//		expect(termService.getTermWithInternalIdentifier(eq("ID1"), null)).andReturn(term1).anyTimes();
//		expect(termService.getTermWithInternalIdentifier(eq("ID2"), null)).andReturn(term2).anyTimes();
//		expect(termService.getTermWithInternalIdentifier(eq("ID3"), null)).andReturn(term3).anyTimes();
//		replay(termService);
//
//		TermOccurrenceExtractorService extractor = new TermOccurrenceExtractorService();
//		extractor.setIndexReaderWrapper(indexReaderWrapper);
//		extractor.setTermService(termService);
//		
//		Collection<String> resultCollection = extractor.extractMostFrequentOccurences(term2, 1, 3);
//		String[] result = resultCollection.toArray(new String[resultCollection.size()]);
//		
//		assertArrayEquals(new String[]{"token05"}, result);
//		
//		resultCollection = extractor.extractMostFrequentOccurences(term2, 2, 3);
//		result = resultCollection.toArray(new String[resultCollection.size()]);
//
//		assertArrayEquals(new String[]{"token05", "token13"}, result);
//
//		resultCollection = extractor.extractMostFrequentOccurences(term2, 3, 3);
//		result = resultCollection.toArray(new String[resultCollection.size()]);
//
//		assertArrayEquals(new String[]{"token05", "token13", "token15"}, result);
//		verify(termService);
//	}
//	
//}
