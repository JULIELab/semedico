/** 
 * TermTableBuilder.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 18.12.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.stemnet.search;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.hivemind.ServiceImplementationFactory;
import org.apache.hivemind.ServiceImplementationFactoryParameters;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.IndexReader;

import com.google.common.collect.TreeMultimap;

import de.julielab.lucene.IIndexReaderWrapper;
import de.julielab.stemnet.IndexFieldNames;
import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetTerm;
import de.julielab.stemnet.core.services.IFacetService;
import de.julielab.stemnet.core.services.ITermService;

public class TermTableBuilder implements ITermTableBuilder, ServiceImplementationFactory{

	private class LabelIDSelector implements FieldSelector{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public FieldSelectorResult accept(String fieldName) {
			if( fieldName.equals(IndexFieldNames.LABEL_IDS) )
				return FieldSelectorResult.LOAD;
			else
				return FieldSelectorResult.NO_LOAD;
		}		
	}
	
	private class FacetIndexComparator implements Comparator<Facet>{

		@Override
		public int compare(Facet facet1, Facet facet2) {
			
			return facet1.getIndex() - facet2.getIndex();
		}
	}
	
	private static Logger LOGGER = Logger.getLogger(TermTableBuilder.class);
	private IIndexReaderWrapper indexReaderWrapper;
	
	private ITermService termService;
	private IFacetService facetService;
	
	public TermTableBuilder(IIndexReaderWrapper indexReaderWrapper,
			ITermService termService, IFacetService facetService) {
		super();
		this.indexReaderWrapper = indexReaderWrapper;
		this.termService = termService;
		this.facetService = facetService;
	}

	@Override
	public FacetTerm[][][] buildTermTable() throws IOException {
		FacetTerm[][][] termTable = null;
		IndexReader reader = indexReaderWrapper.getIndexReader();
		List<Facet> facets;
		try {
			facets = facetService.getFacets();
		} catch (SQLException e) {
			throw new IOException(e);
		}
		
		LOGGER.info("loading document/term table..");
		long time = System.currentTimeMillis();
		termTable = new FacetTerm[reader.maxDoc()][][];
		for( int i = 0; i < reader.maxDoc(); i++ ){

			termTable[i] = new FacetTerm[facets.size()][];
			Set<FacetTerm> terms = extractTermsInDocument(reader, i);
			
			TreeMultimap<Facet, FacetTerm> facetTerms = TreeMultimap.create(new FacetIndexComparator(), null);
			for( FacetTerm term : terms )
				facetTerms.put(term.getFacet(), term);
			
			for( int j = 0; j < facets.size(); j++ ){
				Collection<FacetTerm> termsOfFacet = facetTerms.get(facets.get(j));
				termTable[i][j] = termsOfFacet.toArray(new FacetTerm[termsOfFacet.size()]);
			}
		}

		LOGGER.info(".. finished after " + Math.ceil((float)time / 1000) + " s");
		return termTable;
	}

	protected Set<FacetTerm> extractTermsInDocument(IndexReader reader, Integer docId) throws IOException{
		Document document = reader.document(docId, new LabelIDSelector());
		String[] ids = document.get(IndexFieldNames.LABEL_IDS).split("\\|");
		Set<FacetTerm> terms = new HashSet<FacetTerm>();
		Pattern pattern = Pattern.compile("^\\$(\\d+)\\$\\_(.*)");
		
		for( String id : ids ){
			Matcher matcher = pattern.matcher(id);
			Facet facet = null;
			if( matcher.matches() ){
				Integer facetId = new Integer(matcher.group(1));
				facet = facetService.getFacetWithId(facetId);
				id= matcher.group(2);
				
			}
			
			if( id!= null ){
				FacetTerm term = termService.getTermWithInternalIdentifier(id, facet);
				if( term != null )
					terms.add(term);
			}
		}

		return terms;
	}
	
	public IIndexReaderWrapper getIndexReaderWrapper() {
		return indexReaderWrapper;
	}

	public void setIndexReaderWrapper(IIndexReaderWrapper indexReaderWrapper) {
		this.indexReaderWrapper = indexReaderWrapper;
	}

	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}

	public IFacetService getFacetService() {
		return facetService;
	}

	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
	}

	@Override
	public Object createCoreServiceImplementation(
			ServiceImplementationFactoryParameters parameters) {
		try {
			final FacetTerm[][][] table = buildTermTable();
			return new ITermTable(){

				@Override
				public FacetTerm[][][] getTable()  {
					return table;
				}
			};
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

}
