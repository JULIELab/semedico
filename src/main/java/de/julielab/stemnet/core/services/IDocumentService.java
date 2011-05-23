package de.julielab.stemnet.core.services;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrDocument;

import de.julielab.stemnet.core.SemedicoDocument;

public interface IDocumentService {

	public SemedicoDocument readDocumentStubWithPubMedId(Integer id);
	public SemedicoDocument readDocumentWithPubmedId(int pmid);
	public SemedicoDocument buildSemedicoDocFromSolrDoc(SolrDocument solrDoc);
}