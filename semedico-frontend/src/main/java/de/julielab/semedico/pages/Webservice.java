package de.julielab.semedico.pages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.StringUtils;
import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.julielab.semedico.core.services.SortCriterium;
import de.julielab.scicopia.core.elasticsearch.legacy.ISearchComponent;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoDocument;
import de.julielab.semedico.core.services.BibliographyEntry;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.services.IStatefulSearchService;
import de.julielab.semedico.services.SearchQueryResultList;

public class Webservice {
	private String inputstring;

	@ActivationRequestParameter(value = "sortcriterium")
	private String sortcriteriumString;

	@ActivationRequestParameter(value = "subsetstart")
	private String subsetstart;

	@ActivationRequestParameter(value = "subsetsize")
	private String subsetsize;

	@Inject
	private Request request;

	@Inject
	protected IStatefulSearchService searchService;

	@Inject
	@QueryTranslation
	private ISearchComponent queryTranslationComponent;

	@Inject
	@QueryAnalysis
	private ISearchComponent queryanalysisservice;

	@Inject
	protected ITokenInputService tokenInputService; // will Token, weg, wenn nicht funktioniert

	StreamResponse onActivate() {
		inputstring = request.getParameter("inputstring");

		return new StreamResponse() {
			private InputStream inputStream;

			@Override
			public void prepareResponse(Response response) {

				SearchQueryResultList searchresult = null;

				searchresult = searchDataInSemedico(inputstring, sortcriteriumString, subsetstart, subsetsize);

				Gson result = new GsonBuilder().setPrettyPrinting().create();
				String searchresultjson = result.toJson(searchresult);

				try {
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

					String output = "";

					output = searchresultjson;

					outputStream.write(output.getBytes());

					inputStream = new ByteArrayInputStream(outputStream.toByteArray());

					response.setHeader("Content-Length", "" + outputStream.size());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			@Override
			public InputStream getStream() throws IOException {
				return inputStream;
			}

			@Override
			public String getContentType() {
				return "application/json";
			}
		};
	}

	public SearchQueryResultList searchDataInSemedico(String inputstring, String sortcriteriumString,
			String subsetstart, String subsetsize) {
		SearchQueryResultList searchqueryresult = new SearchQueryResultList();

		if ((inputstring == null) || (inputstring == "")) {
			searchqueryresult.setError("Error: There is no input searching content in Semedico.");
			return searchqueryresult;
		}

		int startPosition;

		if ((subsetstart == null) || (subsetstart == "")) {
			startPosition = 0; // index starts with "0"
		} else {
			try {
				startPosition = Integer.parseInt(subsetstart) - 1;
			} catch (NumberFormatException e) {
				searchqueryresult.setError("Error: There is no right format for subsetstart!");
				return searchqueryresult; // no search with wrong input
			}
		}

		if (StringUtils.isBlank(subsetsize))
			subsetsize = "10";

		SortCriterium sortcriterium; // Initialisation in If-Statement

		if ((sortcriteriumString == null) || (sortcriteriumString == "") || (sortcriteriumString.equals("RELEVANCE"))) {
			sortcriterium = SortCriterium.RELEVANCE;
		} else if (sortcriteriumString.equals("DATE")) {
			sortcriterium = SortCriterium.DATE;
		} else if (sortcriteriumString.equals("DATE_AND_RELEVANCE")) {
			sortcriterium = SortCriterium.DATE_AND_RELEVANCE;
		} else {
			searchqueryresult.setError(
					"Error: There is no right format for sortcriterium! Right Input: RELEVANCE, DATE, DATE_AND_RELEVANCE");
			return searchqueryresult;
		}

		searchqueryresult.setInputstring(inputstring);
		searchqueryresult.setSortcriterium(sortcriterium);

		List<QueryToken> userInputQueryTokens = new ArrayList<>();

		QueryToken queryToken = new QueryToken(0, inputstring.length() - 1, inputstring);

		userInputQueryTokens.add(queryToken);

		try {
			// Start New Search
			LegacySemedicoSearchResult searchResult = (LegacySemedicoSearchResult)
			searchService
					.doDocumentSearchWebservice(userInputQueryTokens, sortcriterium, startPosition, Integer.parseInt(subsetsize))
					.get();

			Collection<HighlightedSemedicoDocument> displayedObject = searchResult.documentHits.getDisplayedObjects();

			searchqueryresult.setCountallresults(searchResult.documentHits.getTotalSize());

			if (searchResult.documentHits.getTotalSize() == 0) {// return if empty set
				return searchqueryresult;
			}

			ParseTree parsetree = searchResult.query;

			List<String> tokens = parsetree.getText();
			// searchqueryresult.setTokens(listtextnodes); // This does not work correctly,
			// so the next for...

			searchqueryresult.setTokens(tokens);

			// könnte noch zurückgegeben werden --> bei Bexis angefragt, wird später
			// gewünscht // TODO
			// Collection<QueryToken> cnodes = parsetree.getQueryTokens();

			// while(0 < cnodes.size())
			// {
			// QueryToken x = cnodes.iterator().next();
			// System.out.println(x);
			// cnodes.remove(x);
			// }

			searchqueryresult.setSubsetstart(startPosition + 1); // first element // 0 = first count
			searchqueryresult.setSubsetend(Integer.parseInt(subsetsize)); // last element

			List<BibliographyEntry> listbibliography = new ArrayList<>();

			while (!displayedObject.isEmpty()) {
				BibliographyEntry currentEntry = new BibliographyEntry();

				HighlightedSemedicoDocument highlighteddocument = displayedObject.iterator().next(); // get 1. Element
				displayedObject.remove(highlighteddocument); // delete 1. Element

				SemedicoDocument semedicoDoc = highlighteddocument.getDocument();

				currentEntry.setArticleTitle(semedicoDoc.getTitle());
				currentEntry.setAbstractText(semedicoDoc.getAbstractText());
				currentEntry.setDocId(semedicoDoc.getDocId());
				currentEntry.setPmid(semedicoDoc.getPmid());
				currentEntry.setPmcid(semedicoDoc.getPmcid());

				List<Author> authors = semedicoDoc.getAuthors();
				List<Author> authorsWithoutWhitespace = new ArrayList<>();
				while (!authors.isEmpty()) {
					Author author = authors.iterator().next();
					authors.remove(author);

					if (author != null && author.getFirstname() != null)
						author.setForename(author.getFirstname().replaceAll(" ", ""));
					authorsWithoutWhitespace.add(author);
				}
				currentEntry.setAuthors(authorsWithoutWhitespace);

				currentEntry.setExternalLinks(semedicoDoc.getExternalLinks());
				currentEntry.setType(semedicoDoc.getType());
				currentEntry.setReview(semedicoDoc.isReview());
				currentEntry.setIndextype(semedicoDoc.getIndexType());

				currentEntry.setPublication(semedicoDoc.getPublication());
				listbibliography.add(currentEntry);
			}

			searchqueryresult.setBibliographylist(listbibliography);

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		return searchqueryresult;
	}
}
