package de.julielab.semedico.mesh.gene2mesh;

import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This is a class to fetch, parse and transform data from the API Gene2MeSH.
 * We thought that this is a way getting NCBI genes from MeSH but we get more data that we want.
 * Now, we don't need this class for Semedico.
 * 
 * This class fetch data by a RESTful interface and it is parsed by the class Gene2MeSHSAXHandler.java.
 * 
 * The XML-file mesh=diabetes.xml contains an example of the fetched request:
 * http://gene2mesh.ncibi.org/fetch?mesh=diabetes
 * 
 * http://gene2mesh.ncibi.org/
 * http://ws.ncibi.org/g2m.html
 * https://academic.oup.com/nar/article/37/suppl_1/D642/1007349/Michigan-molecular-interactions-r2-from
 * 
 * @author Christina Lohr
 * @date Jan 2017
 * 
 */

public class Gene2MeSH
{
	public static String LinkGeneralGene2MeSH = "http://gene2mesh.ncibi.org/fetch?mesh";
	
	public static void main(String[] args) throws Exception
	{
		String MeSH_EntryTerm = "diabetes";
		
		FetchAndParseDataFromGene2MeSH(MeSH_EntryTerm);		
	}

	static public void FetchAndParseDataFromGene2MeSH (String MeSH_EntryTerm) throws Exception
	{
		String url_input = LinkGeneralGene2MeSH + "=" + MeSH_EntryTerm;
		
	    URL url_ncbi = new URL( url_input );

		@SuppressWarnings("resource")
		String result_ncbi = new Scanner( url_ncbi.openStream() ).useDelimiter( "\\Z" ).next();
		
		SAXParserFactory factory_ncbi	= SAXParserFactory.newInstance();
		SAXParser saxParser_ncbi		= factory_ncbi.newSAXParser();
		DefaultHandler handler_ncbi		= new Gene2MeSHSAXHandler();
		saxParser_ncbi.parse( new InputSource(new StringReader(result_ncbi)), handler_ncbi);
		
		HashMap<String, String> ResultList = new HashMap<String, String>();

		ResultList = Gene2MeSHSAXHandler.getResultList(); // <-- Das kann auch der Return-Typ der Funktion werden!
		
		System.out.println("ResultList.size() " + ResultList.size());
		
		for( String name: ResultList.keySet() )
		{
			System.out.println(name + ": "+ ResultList.get(name));    
		}
	}
}