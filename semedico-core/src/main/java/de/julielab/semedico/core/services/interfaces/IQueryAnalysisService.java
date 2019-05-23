package de.julielab.semedico.core.services.interfaces;

import java.util.List;

import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.query.QueryToken;

public interface IQueryAnalysisService {
	ParseTree analyseQueryString(List<QueryToken> userQuery);
}
