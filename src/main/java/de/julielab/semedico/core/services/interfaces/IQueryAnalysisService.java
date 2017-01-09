package de.julielab.semedico.core.services.interfaces;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.UserQuery;

public interface IQueryAnalysisService {
	ParseTree analyseQueryString(String userQuery);

	ParseTree analyseQueryString(UserQuery userQuery, long id);
	
	ParseTree analyseQueryString(UserQuery userQuery, long id, boolean compress);
}
