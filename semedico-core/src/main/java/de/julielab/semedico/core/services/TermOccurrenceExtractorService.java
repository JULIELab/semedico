/** 
 * TermOccurrenceExtractorService.java
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
 * Creation date: 20.08.2008 
 * 
 * //TODO insert short description
 **/

/**
 * 
 */
package de.julielab.semedico.core.services;


/**
 * @author landefeld
 *
 */
//public class TermOccurrenceExtractorService implements ITermOccurrenceExtractorService {
//
//	private IIndexReaderWrapper indexReaderWrapper;
//	private IConceptService conceptService;
//
//	private class AbstractFieldSelector implements FieldSelector{
//
//		public FieldSelectorResult accept(String fieldName) {
//			if( fieldName.equals(IndexFieldNames.ABSTRACT) )
//				return FieldSelectorResult.LOAD;
//			else
//				return FieldSelectorResult.NO_LOAD;
//		}		
//	}	
//
//	private class ReverseIntegerComparator implements Comparator<Integer>{
//
//		@Override
//		public int compare(Integer int1, Integer int2) {
//
//			return int2-int1;
//		}
//		
//	}
//	/* (non-Javadoc)
//	 * @see de.julielab.stemnet.core.services.ITermOccurrenceExtractorService#getMostFrequentOccurences(de.julielab.stemnet.core.Term, int)
//	 */
//	public Collection<String> extractMostFrequentOccurences(SolrServer sorl, IFacetTerm term, int maxNumberOfOccurrences, int minOccurrences) throws IOException {
//		IndexReader reader = indexReaderWrapper.getIndexReader();
//		org.apache.lucene.index.Term indexTerm = new org.apache.lucene.index.Term(IndexFieldNames.TEXT, term.getId());
//
//		TermDocs termDocs = reader.termDocs(indexTerm);
//		Map<String, Integer>  occurrenceFrequencies = new HashMap<String, Integer>();
//		
//		while( termDocs.next() ){
//			int docId = termDocs.doc();
//			TermFreqVector termFreqVector = reader.getTermFreqVector(docId, IndexFieldNames.TEXT);
//			if( !(termFreqVector instanceof TermPositionVector) )
//				throw new IllegalArgumentException("No positions stored!");
//
//			TermPositionVector termPositionVector = (TermPositionVector) termFreqVector;
//			Multimap<Integer, IFacetTerm> offsetMap = createStartOffsetMap(termPositionVector);
//			TermVectorOffsetInfo[] offsets = termPositionVector.getOffsets(termPositionVector.indexOf(term.getId()));
//			
//			for( TermVectorOffsetInfo offsetInfo: offsets ){
//				Collection<IFacetTerm> termsOnStartOffset = offsetMap.get(offsetInfo.getStartOffset());
//				if( hasNoChildTermsInCollection(term, termsOnStartOffset) ){
//					String occurrence = extractOccurenceOnOffset(docId, offsetInfo).toLowerCase();
//					if( occurrenceFrequencies.containsKey(occurrence) ){
//						Integer frequency = occurrenceFrequencies.get(occurrence);
//						frequency++;
//						occurrenceFrequencies.put(occurrence, frequency);
//					}
//					else
//						occurrenceFrequencies.put(occurrence, 1);
//				}
//			}
//		}
//		
//		if( occurrenceFrequencies.size() == 0 )
//			return Collections.EMPTY_LIST;
//		
//		TreeMultimap<Integer, String> sortedOccurrenceFrequencies = TreeMultimap.create(new ReverseIntegerComparator(), null);
//		for( String occurrence: occurrenceFrequencies.keySet() )
//			sortedOccurrenceFrequencies.put(occurrenceFrequencies.get(occurrence), occurrence);
//		
//		if( sortedOccurrenceFrequencies.keySet().first() < minOccurrences )
//			return Collections.EMPTY_LIST;
//		
//		Collection<String> occurrences = new ArrayList<String>();
//		SortedSet<Integer> frequencies = sortedOccurrenceFrequencies.keySet();
//		Iterator<Integer> frequenciesIterator = frequencies.iterator();
//
//		while( occurrences.size() <= maxNumberOfOccurrences ){
//			if( !frequenciesIterator.hasNext() )
//				break;
//
//			Integer frequency = frequenciesIterator.next();
//			if( frequency < minOccurrences )
//				break;
//			
//			SortedSet<String> occurrencesWithFrequency = sortedOccurrenceFrequencies.get(frequency);
//			for( String occurrence: occurrencesWithFrequency )
//				if( occurrences.size() == maxNumberOfOccurrences )
//					break;
//				else
//					occurrences.add(occurrence);
//		}
//		
//		return occurrences;
//	}
//	
//	private String extractOccurenceOnOffset(int docId, TermVectorOffsetInfo offsetInfo) throws IOException{
//		IndexReader reader = indexReaderWrapper.getIndexReader();
//		Document document = reader.document(docId, new AbstractFieldSelector());
//
//		String abstractText = document.get(IndexFieldNames.ABSTRACT);		
//		return abstractText.substring(offsetInfo.getStartOffset(), offsetInfo.getEnd());
//	}
//
//	private boolean hasNoChildTermsInCollection(IFacetTerm term, Collection<IFacetTerm> allTerms){
//		for( IFacetTerm aTerm: allTerms )
//			if( aTerm.getFirstParent() != null && aTerm.getFirstParent().equals(term) )
//				return false;
//		
//		return true;
//	}
//
//	private Multimap<Integer, IFacetTerm> createStartOffsetMap(TermPositionVector termPositionVector){
//		Multimap<Integer, IFacetTerm> offsetMap = HashMultimap.create();
//		for( String facetTermId : termPositionVector.getConcepts() ){
//			IFacetTerm vocabularTerm = conceptService.getNode(facetTermId);
//			if( vocabularTerm == null )
//				continue;
//			
//			TermVectorOffsetInfo[] offsets = termPositionVector.getOffsets(termPositionVector.indexOf(facetTermId));
//			for( TermVectorOffsetInfo offset: offsets ){
//				offsetMap.put(offset.getStartOffset(), vocabularTerm);
//			}
//		}
//		return offsetMap;
//	}
//
//	
//	public IIndexReaderWrapper getIndexReaderWrapper() {
//		return indexReaderWrapper;
//	}
//	public void setIndexReaderWrapper(IIndexReaderWrapper indexReaderWrapper) {
//		this.indexReaderWrapper = indexReaderWrapper;
//	}
//
//	public IConceptService getTermService() {
//		return conceptService;
//	}
//
//	public void setConceptService(IConceptService conceptService) {
//		this.conceptService = conceptService;
//	}
//
//	// TODO only to avoid the compile error!!
//	@Override
//	public Collection<String> extractMostFrequentOccurences(FacetTerm term,
//			int maxNumberOfOccurrences, int minOccurrences) throws IOException {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//}
