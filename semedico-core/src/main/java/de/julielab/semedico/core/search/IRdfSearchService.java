package de.julielab.semedico.core.search;

public interface IRdfSearchService {

	public String getSubgraph(int pathLength, String... center)
			throws Exception;

	public String getSubgraph(String... center) throws Exception;

	public String getSubgraph(boolean withNil, String... center)
			throws Exception;

	public String getSubgraph(boolean withNil, int pathLength, String[] center)
			throws Exception;

}