/** 
 * QueryDictionaryBuilderService.java
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
 * Creation date: 30.10.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.query;

import static de.julielab.semedico.core.services.ITermService.ID_SUFFIX_LAST_AUTHORS;
import static de.julielab.semedico.core.services.ITermService.ID_SUFFIX_FIRST_AUTHORS;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.index.FilterIndexReader;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.tapestry5.ioc.annotations.InjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.Taxonomy.IFacetTerm;
import de.julielab.semedico.core.services.IStopWordService;
import de.julielab.semedico.core.services.ITermOccurrenceFilterService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.util.TermVariantGenerator;

public class QueryDictionaryBuilderService implements
		IQueryDictionaryBuilderService {

	private static Logger LOGGER = LoggerFactory
			.getLogger(QueryDictionaryBuilderService.class);
	private ITermService termService;
	private ITermOccurrenceFilterService filterService;
	private Set<String> stopWords;
	private final SolrServer solr;

	public QueryDictionaryBuilderService(ITermService termService,
			ITermOccurrenceFilterService filterService,
			IStopWordService stopWordService,
			@InjectService("SolrSearcher") SolrServer solr) {
		this.termService = termService;
		this.filterService = filterService;
		this.solr = solr;
		stopWords = stopWordService.getAsSet();
	}

	@Override
	public void createTermDictionary(String filePath) throws SQLException,
			IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
		LOGGER.info("creating query term dictionary " + filePath + "...");

		writeDictionaryEntriesForTerms(writer);
		writeDictionaryEntriesForAuthors(writer,
				IndexFieldNames.FACET_FIRST_AUTHORS, ID_SUFFIX_FIRST_AUTHORS);
		writeDictionaryEntriesForAuthors(writer,
				IndexFieldNames.FACET_LAST_AUTHORS, ID_SUFFIX_LAST_AUTHORS);

		writer.close();
		LOGGER.info("query term dictionary finished");

	}

	/**
	 * <p>
	 * This method is intended to write author names to the query dictionary.
	 * </p>
	 * <p>
	 * Because Semedico distinguishes between first and last authors, you must
	 * provide the index field name from which to retrieve the values and the ID
	 * prefix (for first or for last authors, see references) for building term
	 * identifiers for the author names.
	 * </p>
	 * <p>
	 * The author terms are then included in the dictionary by generating
	 * variants of the complete name string and storing them with an ID in the
	 * form <em>idPrefix&lt;lastNameInUpperCase&gt;</em>.
	 * </p>
	 * 
	 * @param writer Writer for the query dictionary to append the author terms to.
	 * @see {@link ITermService#ID_SUFFIX_FIRST_AUTHORS}
	 * @see {@link ITermService#ID_SUFFIX_LAST_AUTHORS}
	 * @see {@link TermVariantGenerator}
	 */
	private void writeDictionaryEntriesForAuthors(BufferedWriter writer,
			String fieldName, String idPrefix) {
		TermVariantGenerator termVariantGenerator = TermVariantGenerator
				.getDefaultInstance();

		try {
			FacetField facetField = getFacetField(fieldName);
			for (Count c : facetField.getValues()) {
				String authorname = c.getName();
				String lastname = authorname.split(",")[0].trim();
				String id = idPrefix + lastname.toUpperCase();
				if (termService.hasNode(id))
					throw new IllegalStateException(
							"A term, denoting an author, with ID '"
									+ id
									+ "' should be generated. However, there already is a term with that ID known to the term service.");

				Set<String> variants = termVariantGenerator
						.makeTermVariants(authorname);
				for (String variant : variants)
					writer.write(variant + "\t" + id + "\n");
			}
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private FacetField getFacetField(String fieldName) throws SolrServerException {
		SolrQuery query = new SolrQuery("*:*");
		query.setRows(0);
		query.setFacet(true);
		query.set("facet.field", fieldName);
		return solr.query(query).getFacetField(fieldName);
	}
	
	/**
	 * @param writer
	 * @throws SQLException
	 * @throws IOException
	 */
	private void writeDictionaryEntriesForTerms(BufferedWriter writer)
			throws SQLException, IOException {
		TermVariantGenerator termVariantGenerator = TermVariantGenerator
				.getDefaultInstance();
		Collection<IFacetTerm> terms = termService
				.filterTermsNotInIndex(termService.getNodes());

		for (IFacetTerm term : terms) {
			// if( term.getFirstFacet().getType() != Facet.BIBLIOGRAPHY )
			// if( !termService.termOccuredInDocumentIndex(term) )
			// continue;

			Collection<String> occurrences = termService
					.readOccurrencesForTerm(term);
			occurrences.addAll(termService.readIndexOccurrencesForTerm(term));
			occurrences = filterService
					.filterTermOccurrences(term, occurrences);

			Set<String> uniqueOccurrences = new HashSet<String>();
			uniqueOccurrences.addAll(occurrences);

			Integer facetId = term.getFirstFacet().getId();
			// TODO This will propably be done directly by index faceting.
			// if( facetId.equals(Facet.FIRST_AUTHOR_FACET_ID) ||
			// facetId.equals(Facet.LAST_AUTHOR_FACET_ID)){
			// for( String occurrence: occurrences ){
			// String[] splitts = occurrence.split(",");
			// String lastName = splitts[0];
			// uniqueOccurrences.add(lastName.toLowerCase());
			// uniqueOccurrences.add(occurrence.toLowerCase());
			// }
			// }
			// else{
			for (String occurrence : occurrences) {
				Collection<String> variants = termVariantGenerator
						.makeTermVariants(occurrence.toLowerCase());
				variants = filterService.filterTermOccurrences(term, variants);
				uniqueOccurrences.addAll(variants);
			}
			// }
			for (String occurrence : uniqueOccurrences)
				if (!stopWords.contains(occurrence))
					writer.write(occurrence + "\t" + term.getId() + "\n");
		}
	}

}
