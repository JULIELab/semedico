package de.julielab.semedico.pages;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.tapestry5.StreamResponse;
import org.apache.tapestry5.annotations.ActivationRequestParameter;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.julielab.elastic.query.SortCriterium;
import de.julielab.elastic.query.components.ISearchComponent;
import de.julielab.semedico.core.Author;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.SemedicoDocument;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.query.QueryToken;
import de.julielab.semedico.core.query.UserQuery;
import de.julielab.semedico.core.search.components.QueryAnalysisComponent.QueryAnalysis;
import de.julielab.semedico.core.search.components.QueryTranslationComponent.QueryTranslation;
import de.julielab.semedico.core.search.components.data.LegacySemedicoSearchResult;
import de.julielab.semedico.core.search.components.data.SemedicoSearchResult;
import de.julielab.semedico.core.services.BibliographyEntry;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.services.IStatefulSearchService;
import de.julielab.semedico.services.SearchQueryResultList;

public class Webservice
{
//	@ActivationRequestParameter(value="inputstring")
	private String inputstring;

	@ActivationRequestParameter(value="sortcriterium")
	private String sortcriteriumString;
	
	@ActivationRequestParameter(value="subsetstart")
	private String subsetstart;

	@ActivationRequestParameter(value="subsetend")
	private String subsetend;
	
	@Inject
	private Request request;
	
	@Inject
	protected IStatefulSearchService searchService;
	
	@Inject @QueryTranslation
	private ISearchComponent queryTranslationComponent;
	
	@Inject @QueryAnalysis
	private ISearchComponent queryanalysisservice;
	
	@Inject
	protected ITokenInputService tokenInputService;	// will Token, weg, wenn nicht funktioniert
	
	StreamResponse onActivate()
	{
		inputstring = request.getParameter("inputstring");
		
		return new StreamResponse()
		{
			private InputStream inputStream;
			
			@Override
			public void prepareResponse(Response response)
			{
				System.out.println("webservice.onActivate().new StreamResponse() {...}.prepareResponse()");
				
				SearchQueryResultList searchresult = null;
				
				searchresult = SearchDataInSemedico(inputstring, sortcriteriumString, subsetstart, subsetend);
				
				Gson result = new GsonBuilder().setPrettyPrinting().create();
				String searchresultjson = result.toJson(searchresult);
				
				try
				{
					ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
					
					String output = "";
					
					output = searchresultjson;					
					
					outputStream.write(output.getBytes());

					inputStream = new ByteArrayInputStream(outputStream.toByteArray());
					
					response.setHeader("Content-Length", "" + outputStream.size());
					
					// output into file
//					response.setHeader("Content-Length", "" + inputStream.available());
//					response.setHeader("Content-disposition", "attachment; filename=semedicoresults.json");					
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
			
			@Override
			public InputStream getStream() throws IOException
			{
				return inputStream;
			}
			
			@Override
			public String getContentType()
			{
				// return "text/plain";
				// return "text/xml";
				return "application/json";
			}
		};
	}

	public SearchQueryResultList SearchDataInSemedico(
			String inputstring,
			String sortcriteriumString,
			String subsetstart,
			String subsetend
			)
	{
		System.out.println("Webservice.SearchDataInSemedico()");
		
		SearchQueryResultList searchqueryresult = new SearchQueryResultList();
		
		if ((inputstring == null) || (inputstring == ""))
		{
			searchqueryresult.setError("Error: There is no input searching content in Semedico.");
			return searchqueryresult;
		}
		
		int startPosition;
		
		if ((subsetstart == null) || (subsetstart == ""))
		{
			startPosition = 0;	// index starts with "0"
		}
		else
		{
			try
			{
				startPosition = Integer.parseInt(subsetstart) - 1;
			}
			catch (NumberFormatException e)
			{
				searchqueryresult.setError("Error: There is no right format for subsetstart!");
				
				System.out.println();
				return searchqueryresult; // no search with wrong input
			}
		}
		
//		int endPosition;	// wird derzeit noch mit übergeben und nichts damit gemacht, weglassen ja / nein?
//		
//		if (subsetend != null)
//		{
//			try
//			{
//				endPosition = Integer.parseInt(subsetend) - 1;
//				System.out.println("endPosition - startPosition = " + (endPosition - startPosition + 1));
//				
//			}
//			catch (NumberFormatException e)
//			{
//				searchqueryresult.setError("Error: There is no right format for subsetend!");
//				return searchqueryresult; // no search with wrong input
//			}
//			
//			if ((endPosition - startPosition) < 10)
//			{
//				searchqueryresult.setSubsetend(endPosition + 1);
//			}
//			else
//			{
//				searchqueryresult.setError("Error: The intervall for the searched subsset is >10.");
//				return searchqueryresult;
//			}
//		}
		
		SortCriterium sortcriterium; // Initialisation in If-Statement
		
		if ( (sortcriteriumString == null) || (sortcriteriumString == "") || (sortcriteriumString.equals("RELEVANCE")) )
		{
			sortcriterium = SortCriterium.RELEVANCE;
		}
		else if (sortcriteriumString.equals("DATE"))
		{
			sortcriterium = SortCriterium.DATE;
		}
		else if(sortcriteriumString.equals("DATE_AND_RELEVANCE"))
		{
			sortcriterium = SortCriterium.DATE_AND_RELEVANCE;
		}
//		else if (sortcriteriumString.equals("RELEVANCE")) // DEFAULT	// TODO prüfen
//		{
//			sortcriterium = SortCriterium.RELEVANCE;
//		}
		else
		{
			searchqueryresult.setError("Error: There is no right format for sortcriterium! Right Input: RELEVANCE, DATE, DATE_AND_RELEVANCE");
			return searchqueryresult;
		}
		
		searchqueryresult.setInputstring(inputstring);
		searchqueryresult.setSortcriterium(sortcriterium);
	
		List <QueryToken> userInputQueryTokens = new ArrayList<>();

		QueryToken queryToken = new QueryToken(0, inputstring.length() - 1);
		
		queryToken.setOriginalValue(inputstring);
		
		userInputQueryTokens.add(queryToken);
		
		UserQuery userQuery = new UserQuery();
		userQuery.tokens = userInputQueryTokens;
		
		System.out.println("userInputQueryTokens.size() " + userInputQueryTokens.size());
		
		try
		{

			LegacySemedicoSearchResult searchResult =
				(LegacySemedicoSearchResult) //searchService.doNewDocumentSearch(userQuery).get(); // Start New Search
			searchService.doDocumentSearchWebservice(userQuery, sortcriterium, startPosition).get();
			
			Collection<HighlightedSemedicoDocument> displayedObject = searchResult.documentHits.getDisplayedObjects();
			
			searchqueryresult.setCountallresults(searchResult.documentHits.getTotalSize());
			
			if (searchResult.documentHits.getTotalSize() == 0) // return if empty set
			{
				return searchqueryresult;
			}
			
			ParseTree parsetree = searchResult.query;
			
			List<TextNode> listtextnodes = parsetree.getTextNodes();
			List<String> tokens = new ArrayList<String>();
			// searchqueryresult.setTokens(listtextnodes);	// This does not work correctly, so the next for...
			
			for (int i = 0; i < listtextnodes.size(); i++)
			{
				tokens.add(listtextnodes.get(i).getText());
			}
			
			searchqueryresult.setTokens(tokens);
			
			// könnte noch zurückgegeben werden --> bei Bexis angefragt, wird später gewünscht // TODO
			//Collection<QueryToken> cnodes = parsetree.getQueryTokens();
			
			//while(0 < cnodes.size())
			//{
			//	QueryToken x = cnodes.iterator().next();
			//	System.out.println(x);
			//	cnodes.remove(x);
			//}
			
			searchqueryresult.setSubsetstart(		startPosition + 1);							// first element // 0 = first count
			searchqueryresult.setSubsetend(			startPosition + displayedObject.size());	// last element
//			searchqueryresult.setSubsetend(			subsetend);
			
			//searchqueryresult.setSubsetstart(		searchResult.documentHits.getNumberOfFirstDisplayedObject()); // = 1 --> wrong output
			//searchqueryresult.setSubsetend(		searchResult.documentHits.getIndexOfLastDisplayedObject());
			
			List <BibliographyEntry> listbibliography = new ArrayList <BibliographyEntry>();

			while(0 < displayedObject.size())
//			for (int i = 0; i < (endPosition - startPosition +1); i++)
				//(endPosition - startPosition + 1)
			{
				BibliographyEntry currentEntry = new BibliographyEntry();

				HighlightedSemedicoDocument highlighteddocument = displayedObject.iterator().next();	// get 1. Element
				displayedObject.remove(highlighteddocument);											// delete 1. Element
		
				SemedicoDocument semedicoDoc = highlighteddocument.getDocument();

				currentEntry.setArticleTitle(			semedicoDoc.getTitle());
				currentEntry.setAbstractText(			semedicoDoc.getAbstractText());
				currentEntry.setDocId(					semedicoDoc.getDocId());
				currentEntry.setPmid(					semedicoDoc.getPmid());
				currentEntry.setPmcid(					semedicoDoc.getPmcid());
				
				//currentEntry.setAuthors(				semedicoDoc.getAuthors());
				List<Author> authors = semedicoDoc.getAuthors();
				List<Author> authors_without_whitespace = new ArrayList<Author>();
				while(0 < authors.size())
				{
					Author author = authors.iterator().next();
					authors.remove(author);
					
					author.setForename(author.getFirstname().replaceAll(" ", ""));
					authors_without_whitespace.add(author);					
				}	
				currentEntry.setAuthors(authors_without_whitespace);
				
				currentEntry.setExternalLinks(			semedicoDoc.getExternalLinks());
				currentEntry.setType(					semedicoDoc.getType());
				currentEntry.setReview(					semedicoDoc.isReview());
				currentEntry.setIndextype(				semedicoDoc.getIndexType());
				
				currentEntry.setPublication(semedicoDoc.getPublication());
				listbibliography.add(currentEntry);
			}
			
			searchqueryresult.setBibliographylist(listbibliography);
		
		}
		catch (InterruptedException | ExecutionException e)
		{
			e.printStackTrace();
		}
		
		return searchqueryresult;
	}	
}
