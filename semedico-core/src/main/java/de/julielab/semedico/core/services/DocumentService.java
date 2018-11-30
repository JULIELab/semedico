package de.julielab.semedico.core.services;

import java.util.List;

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.HighlightedStatement;
import de.julielab.semedico.core.entities.documents.SemedicoDocument;
import de.julielab.semedico.core.services.interfaces.IDocumentService;

public class DocumentService implements IDocumentService {

	@Override
	public SemedicoDocument getSemedicoDocument(int pmid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HighlightedSemedicoDocument getHitListDocument(ISearchServerDocument serverDoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HighlightedSemedicoDocument getHighlightedSemedicoDocument(ISearchServerDocument solrDoc) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<HighlightedStatement> getHighlightedStatements(ISearchServerDocument serverDoc) {
		// TODO Auto-generated method stub
		return null;
	}

}
