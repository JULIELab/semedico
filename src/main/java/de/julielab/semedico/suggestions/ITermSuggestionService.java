package de.julielab.semedico.suggestions;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetSuggestionHit;
import de.julielab.semedico.core.services.ITermService;

public interface ITermSuggestionService {

	public final static class Fields{
		public final static String SUGGESTION = "suggestion";
		public final static String TERM_IDENTIFIER = "term_identifier";
		public final static String FACET = "facet";
		public final static String INDEX = "index"; 	
		public final static String SHORT_DESCRIPTION = "short_description";
	}
	
	public List<FacetSuggestionHit> createSuggestions(String termFragment,
			List<Facet> facets) throws IOException;

	public int termCount();
	
	public void createSuggestionIndex(String path) throws IOException, SQLException;
	public void setMaxTokenLength(int maxTokenLength);
	public int getMaxTokenLength();
	
	public void setMinTokenLength(int minTokenLength);
	public int getMinTokenLength();
	
	public void setTermService(ITermService termService);
	public ITermService getTermService();
}