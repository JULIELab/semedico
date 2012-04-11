/** 
 * JournalService.java
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
 * Creation date: 27.05.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.services;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Journal;

public class JournalService implements IJournalService {
	
	private class FileIterator implements Iterator<Journal>{

		private BufferedReader in;
		private Journal currentJournal;
		
		public FileIterator(String filepath) throws IOException {
			 in = new BufferedReader(new FileReader(filepath));
		}

		public boolean hasMoreElements() {
			String line = null; 
				
			try {
				while ((line = in.readLine()) != null) {
					if( line.startsWith("------") ){
						currentJournal = new Journal();
						return true;
					}
				}
				in.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

			return false;
		}

		public Journal nextElement() {

			String line = null;
			try {

				if( currentJournal == null && in.ready() )
					if( !hasMoreElements() )
						return null;

				while ((line = in.readLine()) != null) {
					if( line.startsWith("JournalTitle: ") )
						currentJournal.setTitle(line.substring(14));									
					
					if( line.startsWith("ISSN: ")){
						String issn = line.substring(6).trim();
						if( !issn.equals("") )
							currentJournal.setIssn( issn );
					}
					if( line.startsWith("ESSN: ")){
						String essn = line.substring(6).trim();
						if( !essn.equals("") )
							currentJournal.setEssn( essn );

					}									
					if( line.startsWith("MedAbbr: ") ){
						String medAbbr = line.substring(9).trim();
						if( !medAbbr.equals("") )
							currentJournal.setShortTitle(medAbbr);
					}
					if( line.startsWith("IsoAbbr: ") ){
						String isoAbbr = line.substring(9).trim();
						if( !isoAbbr.equals("") && currentJournal.getShortTitle() == null )
							currentJournal.setShortTitle(isoAbbr);
					}
					if( line.startsWith("NlmId") )
						return currentJournal;					
				}
				in.close();
			
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
			

			return null;
		}

		@Override
		public boolean hasNext() {
			String line = null; 
			
			try {
				while ((line = in.readLine()) != null) {
					if( line.startsWith("------") ){
						currentJournal = new Journal();
						return true;
					}
				}
				in.close();
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

			return false;
		}

		@Override
		public Journal next() {
			String line = null;
			try {

				if( currentJournal == null && in.ready() )
					if( !hasMoreElements() )
						return null;

				while ((line = in.readLine()) != null) {
					if( line.startsWith("JournalTitle: ") )
						currentJournal.setTitle(line.substring(14));									
					
					if( line.startsWith("ISSN: ")){
						String issn = line.substring(6).trim();
						if( !issn.equals("") )
							currentJournal.setIssn( issn );
					}
					if( line.startsWith("ESSN: ")){
						String essn = line.substring(6).trim();
						if( !essn.equals("") )
							currentJournal.setEssn( essn );

					}									
					if( line.startsWith("MedAbbr: ") ){
						String medAbbr = line.substring(9).trim();
						if( !medAbbr.equals("") )
							currentJournal.setShortTitle(medAbbr);
					}
					if( line.startsWith("IsoAbbr: ") ){
						String isoAbbr = line.substring(9).trim();
						if( !isoAbbr.equals("") && currentJournal.getShortTitle() == null )
							currentJournal.setShortTitle(isoAbbr);
					}
					if( line.startsWith("NlmId") )
						return currentJournal;					
				}
				in.close();
			
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}

			return null;
		}

		@Override
		public void remove() {
			throw new IllegalStateException(new OperationNotSupportedException("remove() is not supported"));			
		}
		
	}
	
	private ITermService termService;
	private static String JOURNAL_FACET_NAME = "Journals";
	/* (non-Javadoc)
	 * @see de.julielab.stemnet.core.services.IJournalService#readJournalFile(java.lang.String)
	 */
	public Iterator<Journal> readJournalFile(String filePath) throws IOException{
		return new FileIterator(filePath);
	}

	/* (non-Javadoc)
	 * @see de.julielab.stemnet.core.services.IJournalService#insertJournalsAsTerms(java.util.Collection)
	 */
	public void insertJournalsAsTerms(Collection<Journal> journals) throws SQLException{
		Facet journalFacet = termService.getFacetService().getFacetByName(JOURNAL_FACET_NAME); 
		for( Journal journal: journals ){
			FacetTerm term = new FacetTerm(journal.getIssn() != null ? journal.getIssn() : journal.getEssn(), journal.getShortTitle());
			term.setShortDescription(journal.getTitle());
			term.setDescription(journal.getTitle());
			term.addFacet(journalFacet);
			Collection<String> indexNames = new ArrayList<String>();
			indexNames.add(IndexFieldNames.JOURNAL);
			term.setIndexNames(indexNames);
			List<String> suggestions = new ArrayList<String>();
			suggestions.add(journal.getShortTitle());
			termService.insertTerm(term, suggestions);

		}
	}
	
	public ITermService getTermService() {
		return termService;
	}

	public void setTermService(ITermService termService) {
		this.termService = termService;
	}
	
	
}
