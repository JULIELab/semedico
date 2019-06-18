package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.query.UserQuery;

public interface IQueryAnalysisService {
	ParseTree analyseQueryString(String queryString);

	ParseTree analyseQueryString(UserQuery userQuery);
	
	ParseTree analyseQueryString(UserQuery userQuery, boolean compress);
}
