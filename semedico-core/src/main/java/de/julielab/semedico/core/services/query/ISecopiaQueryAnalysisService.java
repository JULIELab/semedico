package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.UserQuery;

public interface ISecopiaQueryAnalysisService {
	SecopiaParse analyseQueryString(String queryString);

	SecopiaParse analyseQueryString(UserQuery userQuery);
}
