package de.julielab.semedico.core.services;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.TermFileEntry;

public class TermFileReaderService implements Enumeration<TermFileEntry>, ITermFileReaderService {
	
    private NodeList termElements;
    private int currentIndex;
    private Facet currentFacet;
    private IFacetService facetService;

    public TermFileReaderService(IFacetService facetService, String filePath) throws IOException{
		super();
		this.facetService = facetService;
		try {
            // Create a builder factory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);

            // Create the builder and parse the file
            Document termFile = factory.newDocumentBuilder().parse(new File(filePath));
            termElements = termFile.getDocumentElement().getElementsByTagName("term");
            String facetName = termFile.getDocumentElement().getAttributes().getNamedItem("facet").getNodeValue();
            if( facetName.equals("null") )
            	currentFacet = FacetService.KEYWORD_FACET;
            else
            	currentFacet = facetService.getFacetWithName(facetName);
            
            if( currentFacet == null )
            	throw new IllegalStateException("Facet " + facetName + " not found!");
          
        } catch (Exception e) {
        	throw new IOException(e.getMessage());
        } 	
	}

	public boolean hasMoreElements() {
		return currentIndex < termElements.getLength();
	}

	public TermFileEntry nextElement() {
		
		Node termNode = termElements.item(currentIndex);
		TermFileEntry term = new TermFileEntry();
		
		term.setFacet(currentFacet);
		term.setId(termNode.getAttributes().getNamedItem("id").getNodeValue());
		if( termNode.getAttributes().getNamedItem("parent-id") != null )
			term.setParentId(termNode.getAttributes().getNamedItem("parent-id").getNodeValue());

		NodeList termChilds = termNode.getChildNodes();
		for( int i = 0; i < termChilds.getLength(); i++ ){
			Node termChild = termChilds.item(i);

			if( termChild.getNodeName().equals("canonic") )
				term.setCanonic(termChild.getTextContent());
			else if( termChild.getNodeName().equals("type") )
				term.setType(termChild.getTextContent());
			else if( termChild.getNodeName().equals("shortDescription") )
				term.setShortDescription(termChild.getTextContent());
			else if( termChild.getNodeName().equals("description") )
				term.setDescription(termChild.getTextContent());			
			else if( termChild.getNodeName().equals("synonyms") ){
				NodeList synonymNodes = termChild.getChildNodes();
				List<String> synonyms = new ArrayList<String>(synonymNodes.getLength());
				for( int j = 0; j < synonymNodes.getLength(); j++ ){					
					Node synonymNode = synonymNodes.item(j);
					if( synonymNode.getNodeName().equals("synonym"))
						synonyms.add(synonymNode.getTextContent());
				}
				term.setSynonyms(synonyms);
			}
			else if( termChild.getNodeName().equals("variations") ){
				NodeList variationNodes = termChild.getChildNodes();
				List<List<String>> variations = new ArrayList<List<String>>(variationNodes.getLength());
				for( int j = 0; j < variationNodes.getLength(); j++ ){
					Node variationNode = variationNodes.item(j);
					if( variationNode.getNodeName().equals("variation") ){
						NodeList tokenNodes = variationNode.getChildNodes();

						List<String> variation = new ArrayList<String>(tokenNodes.getLength());
						for( int k = 0; k < tokenNodes.getLength(); k++ ){
							Node tokenNode = tokenNodes.item(k);
							if( tokenNode.getNodeName().equals("token"))
								variation.add(tokenNode.getTextContent());
						}
						variations.add(variation);
					}
				}
				term.setVariations(variations);
			}				
		}
		
		currentIndex++;		
		return term;
	}
	
	public List<TermFileEntry> sortTopDown(List<TermFileEntry> terms){
		
		List<TermFileEntry> roots = new ArrayList<TermFileEntry>();
		List<TermFileEntry> orderedTermFileEntrys = new ArrayList<TermFileEntry>();
		
		for( TermFileEntry term: terms )
			if( term.getParent() == null )
				roots.add(term);
		
		for( TermFileEntry term: roots ){
			_collectTreeNodesTopDown(orderedTermFileEntrys, term);
		}
		
		return orderedTermFileEntrys;
	}
	
	private void _collectTreeNodesTopDown(List<TermFileEntry> list, TermFileEntry term) {
		list.add(term);
		for( TermFileEntry child: term.getChildren() )
			_collectTreeNodesTopDown(list, child);
	}

	public void resolveRelationships(List<TermFileEntry> terms){
		Map<String, TermFileEntry> termsWithId = new HashMap<String, TermFileEntry>();
		for( TermFileEntry term: terms )
			termsWithId.put(term.getId(), term);
		
		for( TermFileEntry term: terms){
			String parentId = term.getParentId();
			if( parentId != null ){
				TermFileEntry parent = termsWithId.get(parentId);
				
				if( parent != null ){
					parent.getChildren().add(term);
					term.setParent(parent);
				}
			}
		}
	}
	
	@Override
	public IFacetService getFacetService() {
		return facetService;
	}

	@Override
	public void setFacetService(IFacetService facetService) {
		this.facetService = facetService;
		
	}
}
