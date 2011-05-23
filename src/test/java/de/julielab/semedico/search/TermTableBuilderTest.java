package de.julielab.semedico.search;
///** 
// * TermTableBuilderTest.java
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
// * Creation date: 18.12.2008 
// * 
// * //TODO insert short description
// **/
//
//package de.julielab.stemnet.search;
//
//
//import static org.junit.Assert.*;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.Set;
//
//import org.apache.lucene.analysis.standard.StandardAnalyzer;
//import org.apache.lucene.document.Document;
//import org.apache.lucene.document.Field;
//import org.apache.lucene.index.IndexReader;
//import org.apache.lucene.index.IndexWriter;
//import org.apache.lucene.store.Directory;
//import org.apache.lucene.store.RAMDirectory;
//import org.junit.Before;
//import org.junit.Test;
//
//import com.google.common.collect.Lists;
//
//import de.julielab.lucene.IIndexReaderWrapper;
//import de.julielab.lucene.IndexReaderWrapper;
//import de.julielab.stemnet.IndexFieldNames;
//import de.julielab.stemnet.core.Facet;
//import de.julielab.stemnet.core.FacetTerm;
//import de.julielab.stemnet.core.services.IFacetService;
//import de.julielab.stemnet.core.services.ITermService;
//import static org.easymock.EasyMock.*;
//
//public class TermTableBuilderTest {
//
//	private FacetTerm term1;
//	private FacetTerm term2;
//	private FacetTerm term3;
//	private FacetTerm term4;
//	private FacetTerm term5;
//	private FacetTerm term6;
//	private FacetTerm term7;
//	private FacetTerm term8;
//	private FacetTerm term11;
//	
//	private Facet facet1;
//	private Facet facet2;
//	private Facet facet3;
//
//	private IIndexReaderWrapper indexReaderWrapper;
//	private IIndexReaderWrapper indexReaderWrapperFail;
//	
//	@Before
//	public void setUpTerms(){
//		facet1 = new Facet(0);
//		facet1.setIndex(0);
//		
//		facet2 = new Facet(1);
//		facet2.setIndex(1);
//		
//		facet3 = new Facet(2);
//		facet3.setIndex(2);
//		
//		term1 = new FacetTerm("term1");
//		term1.setFacet(facet1);
//		term1.setFacetIndex(0);
//		term1.setId(0);
//		
//		term2 = new FacetTerm("term2");
//		term2.setFacet(facet2);
//		term2.setFacetIndex(0);
//		term2.setId(1);
//		
//		term3 = new FacetTerm("term3");
//		term3.setFacet(facet3);
//		term3.setFacetIndex(0);
//		term3.setId(2);
//		
//		term4 = new FacetTerm("term4");
//		term4.setFacet(facet1);
//		term4.setFacetIndex(1);
//		term4.setId(3);
//		
//		term5 = new FacetTerm("term5");
//		term5.setFacet(facet2);
//		term5.setFacetIndex(1);
//		term5.setId(4);
//		
//		term6 = new FacetTerm("term6");
//		term6.setFacet(facet3);
//		term6.setFacetIndex(1);
//		term6.setId(5);
//		
//		term7 = new FacetTerm("term7");
//		term7.setFacet(facet1);
//		term7.setFacetIndex(2);
//		term7.setId(6);
//		
//		term8 = new FacetTerm("term8");
//		term8.setFacet(facet2);
//		term8.setFacetIndex(2);
//		term8.setId(7);
//		
//		term11 = new FacetTerm("term1");
//		term11.setFacet(facet2);		
//		term11.setFacetIndex(3);
//		term11.setId(8);
//	}
//	
//	@Before
//	public void setUpIndexReader() throws Exception {
//	
//		Directory directory = new RAMDirectory();
//		IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer());
//	
//		Document document1 = new Document();
//		Field labelField = new Field(IndexFieldNames.LABEL_IDS, "term1|term2|term3|$1$_term1", Field.Store.YES, Field.Index.NO);
//		document1.add(labelField);
//		writer.addDocument(document1);
//		
//		Document document2 = new Document();
//		labelField = new Field(IndexFieldNames.LABEL_IDS, "term1|term4|term5", Field.Store.YES, Field.Index.NO);
//		document2.add(labelField);
//		writer.addDocument(document2);
//
//		Document document3 = new Document();
//		labelField = new Field(IndexFieldNames.LABEL_IDS, "term6|term7|term8", Field.Store.YES, Field.Index.NO);
//		document3.add(labelField);
//		writer.addDocument(document3);
//
//
//		writer.optimize();
//		writer.close();
//		
//		
//		indexReaderWrapper = new IndexReaderWrapper(IndexReader.open(directory));				
//	}
//	
//	@Before
//	public void setUpIndexReaderFail() throws Exception {
//		
//		Directory directory = new RAMDirectory();
//		IndexWriter writer = new IndexWriter(directory, new StandardAnalyzer());
//	
//		Document document1 = new Document();
//		Field labelField = new Field(IndexFieldNames.LABEL_IDS, "term1|$1$_", Field.Store.YES, Field.Index.NO);
//		document1.add(labelField);
//		writer.addDocument(document1);
//		
//		writer.optimize();
//		writer.close();
//		
//		indexReaderWrapperFail = new IndexReaderWrapper(IndexReader.open(directory));				
//	}
//	
//	@Test
//	public void extractTermsInDocument() throws IOException{
//		ITermService termService = createMock(ITermService.class);
//		IFacetService facetService = createMock(IFacetService.class);
//		
//		TermTableBuilder termTableBuilder = new TermTableBuilder(indexReaderWrapper, termService, facetService);
//		
//		expect(termService.getTermWithInternalIdentifier("term1", null)).andReturn(term1);
//		expect(termService.getTermWithInternalIdentifier("term2", null)).andReturn(term2);
//		expect(termService.getTermWithInternalIdentifier("term3", null)).andReturn(term3);
//		expect(termService.getTermWithInternalIdentifier("term1", facet2)).andReturn(term11);
//		replay(termService);
//		
//		expect(facetService.getFacetWithId(1)).andReturn(facet2);
//		replay(facetService);
//		
//		Set<FacetTerm> terms = termTableBuilder.extractTermsInDocument(indexReaderWrapper.getIndexReader(), 0);
//		verify(termService);
//		verify(facetService);
//		
//		assertEquals(4, terms.size());
//		assertTrue(terms.contains(term1));
//		assertTrue(terms.contains(term2));
//		assertTrue(terms.contains(term3));
//		assertTrue(terms.contains(term11));
//	}
//	
//	@Test
//	public void extractTermsInDocumentFail() throws IOException{
//		ITermService termService = createMock(ITermService.class);
//		IFacetService facetService = createMock(IFacetService.class);
//		
//		TermTableBuilder termTableBuilder = new TermTableBuilder(indexReaderWrapper, termService, facetService);
//		termTableBuilder.setTermService(termService);
//		termTableBuilder.setFacetService(facetService);
//		
//		expect(termService.getTermWithInternalIdentifier("term1", null)).andReturn(term1);
//		replay(termService);
//		
//		expect(facetService.getFacetWithId(1)).andReturn(facet1);
//		replay(facetService);
//		
//		NullPointerException exception = null;
//		try{
//			termTableBuilder.extractTermsInDocument(indexReaderWrapperFail.getIndexReader(), 0);
//		}
//		catch(NullPointerException npe){
//			exception = npe;
//		}
//		
//		assertNotNull(exception);
//		
//		verify(termService);
//		verify(facetService);		
//	}
//
//
//	@Test
//	public void buildTermTable() throws IOException, SQLException{
//		ITermService termService = createMock(ITermService.class);
//		IFacetService facetService = createMock(IFacetService.class);
//		
//		TermTableBuilder termTableBuilder = new TermTableBuilder(indexReaderWrapper, termService, facetService);
//		
//		expect(termService.getTermWithInternalIdentifier("term1", null)).andReturn(term1);
//		expect(termService.getTermWithInternalIdentifier("term2", null)).andReturn(term2);
//		expect(termService.getTermWithInternalIdentifier("term3", null)).andReturn(term3);
//		expect(termService.getTermWithInternalIdentifier("term1", facet2)).andReturn(term11);
//		expect(termService.getTermWithInternalIdentifier("term1", null)).andReturn(term1);
//		expect(termService.getTermWithInternalIdentifier("term4", null)).andReturn(term4);
//		expect(termService.getTermWithInternalIdentifier("term5", null)).andReturn(term5);
//		expect(termService.getTermWithInternalIdentifier("term6", null)).andReturn(term6);
//		expect(termService.getTermWithInternalIdentifier("term7", null)).andReturn(term7);
//		expect(termService.getTermWithInternalIdentifier("term8", null)).andReturn(term8);		
//		replay(termService);
//		
//		expect(facetService.getFacets()).andReturn(Lists.newArrayList(facet1, facet2, facet3));
//		expect(facetService.getFacetWithId(1)).andReturn(facet2);
//		replay(facetService);
//		
//		FacetTerm[][][] termTable = termTableBuilder.buildTermTable();
//		verify(termService);
//		verify(facetService);
//		
//		assertEquals(3, termTable.length);
//		
//		// test doc 1 entry
//		assertEquals(3, termTable[0].length);
//		assertEquals(1, termTable[0][0].length);
//		assertEquals(term1, termTable[0][0][0]);
//		assertEquals(2, termTable[0][1].length);
//		assertEquals(term2, termTable[0][1][0]);
//		assertEquals(term11, termTable[0][1][1]);
//		assertEquals(1, termTable[0][2].length);
//		assertEquals(term3, termTable[0][2][0]);
//
//		// test doc 2 entry
//		assertEquals(3, termTable[1].length);
//		assertEquals(2, termTable[1][0].length);
//		assertEquals(term1, termTable[1][0][0]);
//		assertEquals(term4, termTable[1][0][1]);
//		assertEquals(1, termTable[1][1].length);
//		assertEquals(term5, termTable[1][1][0]);
//		assertEquals(0, termTable[1][2].length);
//
//		// test doc 3 entry
//		assertEquals(3, termTable[2].length);
//		assertEquals(1, termTable[2][0].length);
//		assertEquals(term7, termTable[2][0][0]);
//		assertEquals(1, termTable[2][1].length);
//		assertEquals(term8, termTable[2][1][0]);
//		assertEquals(1, termTable[2][2].length);
//		assertEquals(term6, termTable[2][2][0]);
//	}
//	
//
//}
