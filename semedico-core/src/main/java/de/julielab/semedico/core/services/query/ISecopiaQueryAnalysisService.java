package de.julielab.semedico.core.services.query;

import de.julielab.semedico.core.parsing.SecopiaParse;
import de.julielab.semedico.core.search.query.UserQuery;
import org.antlr.v4.runtime.tree.ParseTree;

public interface ISecopiaQueryAnalysisService {
	SecopiaParse analyseQueryString(String queryString);

	SecopiaParse analyseQueryString(UserQuery userQuery);
}
