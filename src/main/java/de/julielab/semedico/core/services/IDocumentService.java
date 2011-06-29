package de.julielab.semedico.core.services;

import org.apache.solr.common.SolrDocument;

import de.julielab.semedico.core.SemedicoDocument;

public interface IDocumentService {

	public SemedicoDocument readDocumentStubWithPubMedId(Integer id);
	public SemedicoDocument readDocumentWithPubmedId(int pmid);
	public SemedicoDocument buildSemedicoDocFromSolrDoc(SolrDocument solrDoc);
}