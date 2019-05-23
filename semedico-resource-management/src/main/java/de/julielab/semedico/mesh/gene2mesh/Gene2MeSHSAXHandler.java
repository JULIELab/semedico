package de.julielab.semedico.mesh.gene2mesh;
import java.util.HashMap;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

//http://www.saxproject.org/apidoc/org/xml/sax/ContentHandler.html#endElement%28java.lang.String,%20java.lang.String,%20java.lang.String%29

/**
 * This is a SAX-Parser for parsing data from the RESTful API Gene2Mesh.
 * The following elements are parsed: Identifier of genes, Symbol of genes, Description of genes.
 * 
 * @author Christina Lohr
 * @date Jan 2017
 * 
 */


class Gene2MeSHSAXHandler extends DefaultHandler
{
	public static HashMap<String, String> ResultList = new HashMap<String, String>();
//	public static TreeMap<String, String> ResultList = new TreeMap<String, String>();

	public static HashMap<String, String> getResultList() {
		return ResultList;
	}

	public static boolean Gene					= false;
	public static boolean GeneIdentifier		= false;
	public static boolean GeneSymbol			= false;
	public static boolean GeneDescription		= false;
	
	public static boolean GeneTaxonomyIdentifier	= false;
	
	public static String GeneIdentifierLocal	= "";
	public static String GeneSymbolLocal		= "";
	public static String GeneDescriptionLocal	= "";
	
	@Override
	public void startDocument()
	{
		System.out.println( "Start parsing XML-NCBI-code of Gene2MeSH." );
	}
  
	@Override
	public void startElement(
		String namespaceURI,
		String localName,
		String qName,
		Attributes attributes )
	{
		if (qName.equals("Gene"))
		{
			Gene = true;
		}
		
		if (Gene == true)
		{
			switch (qName)
			{
				case "Identifier":
					GeneIdentifier = true;
					break;
				
				case "Symbol":
					GeneSymbol = true;
					break;
				
				case "Description":
					GeneDescription = true;
					break;
					
				case "Taxonomy":
					GeneTaxonomyIdentifier = true;
					break;
			}
		}
	}
	
	@Override
	public void characters(
		char[] ch,
		int start,
		int length
		) throws SAXException
	{
		String attribute = "";
		
		for ( int i = start; i < (start + length); i++ )
		{
			char c = ch[i];
			attribute = attribute + c;
		}
		
		if (Gene)
		{
			if ( (GeneIdentifier) && !(GeneTaxonomyIdentifier))
			{
				GeneIdentifierLocal = attribute;
			}
			if (GeneSymbol)
			{
				GeneSymbolLocal = attribute;
			}
			if (GeneDescription)
			{
				GeneDescriptionLocal = attribute;
			}
		}
	}
	
	@Override
	public void endElement(
			String uri,
	        String localName,
	        String qName
			) throws SAXException
	{
		switch (qName)
		{
			case "Identifier":
				GeneIdentifier = false;
				break;

			case "Symbol":
				GeneSymbol = false;
				break;
			
			case "Description":
				GeneDescription = false;
				break;
				
			case "Taxonomy":
				GeneTaxonomyIdentifier = false;
				break;
			
			case "Gene":
				Gene = false;
				ResultList.put(GeneIdentifierLocal, GeneSymbolLocal + "\t" + GeneDescriptionLocal);
				GeneIdentifierLocal		= "";
				GeneSymbolLocal			= "";
				GeneDescriptionLocal	= "";
				break;
		}
	}
	
	@Override
	public void endDocument()
	{
		System.out.println( "End parsing XML-NCBI-code of Gene2MeSH." );
	}
}

