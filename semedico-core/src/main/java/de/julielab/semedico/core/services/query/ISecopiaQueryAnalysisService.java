package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.search.query.UserQuery;
import org.antlr.v4.runtime.tree.ParseTree;

public interface ISecopiaQueryAnalysisService {
	ParseTree analyseQueryString(String queryString);

	ParseTree analyseQueryString(UserQuery userQuery);
}
