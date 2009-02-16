package de.julielab.semedico.components;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.util.OpenBitSet;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.Term;
import de.julielab.stemnet.search.IFacetHitCollectorService;

public class Tabs {
	private final static String FIRST_TAB = "firstTab"; 
	private final static String SECOND_TAB = "secondTab";
	private final static String THIRD_TAB = "thirdTab";
	private final static String FIRST_TAB_ACTIVE = "firstTabActive"; 
	private final static String FIRST_TAB_INACTIVE = "firstTabInActive";
	private final static String SECOND_TAB_ACTIVE = "secondTabActive"; 
	private final static String SECOND_TAB_INACTIVE = "secondTabInActive";
	private final static String THIRD_TAB_ACTIVE = "thirdTabActive"; 
	private final static String THIRD_TAB_INACTIVE = "thirdTabInActive";
	private static final String EVENT_NAME = "tabselect";
	private static final String FACET_BAR_ID = "facetBar";
	private static final String INIT_JS = "var %s = new Tabs(\"%s\", \"%s\");";
	private static final String SELECTED_TAB_PARAMETER = "selectedTab";
	
	@Inject @Path("tabs.js")
	private Asset tabsJS;
	
	@Property
	@Parameter
	private Map<Facet, FacetConfiguration> facetConfigurations;

	@Property
	@Parameter
	private OpenBitSet documents;
	
	@Property
	@Parameter
	private Term selectedTerm;

	@Property
	@Parameter
	private List<FacetHit> currentTabFacetHits;
	
	@Persist
	private Collection<FacetConfiguration> firstTabConfigurations;
	
	@Persist
	private Collection<FacetConfiguration> secondTabConfigurations;
	
	@Persist
	private Collection<FacetConfiguration> thirdTabConfigurations;
	
	@Property
	@Persist
	private String selectedTab;
	
	@Inject
    private ComponentResources resources;

	@Environmental
	private RenderSupport renderSupport;
	
	@Inject
    private Request request;
	
	@Inject
	private IFacetHitCollectorService facetHitCollectorService;
	
	public String getFirstTabCSSClass(){
		if( selectedTab == null || selectedTab.equals(FIRST_TAB)  )
			return FIRST_TAB_ACTIVE;
		else
			return FIRST_TAB_INACTIVE;
	}

	public String getSecondTabCSSClass(){
		if( selectedTab != null && selectedTab.equals(SECOND_TAB)  )
			return SECOND_TAB_ACTIVE;
		else
			return SECOND_TAB_INACTIVE;
	}

	public String getThirdTabCSSClass(){
		if( selectedTab != null && selectedTab.equals(THIRD_TAB)  )
			return THIRD_TAB_ACTIVE;
		else
			return THIRD_TAB_INACTIVE;
	}
	
	public FacetHit getFacetHit1(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 0 )
			return currentTabFacetHits.get(0);
		else
			return null;
	}

	public FacetHit getFacetHit2(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 1 )
			return currentTabFacetHits.get(1);
		else
			return null;
	}

	public FacetHit getFacetHit3(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 2 )
			return currentTabFacetHits.get(2);
		else
			return null;
	}

	public FacetHit getFacetHit4(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 3 )
			return currentTabFacetHits.get(3);
		else
			return null;
	}

	public FacetHit getFacetHit5(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 4 )
			return currentTabFacetHits.get(4);
		else
			return null;
	}

	public FacetHit getFacetHit6(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 5 )
			return currentTabFacetHits.get(5);
		else
			return null;
	}

	public FacetHit getFacetHit7(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 6 )
			return currentTabFacetHits.get(6);
		else
			return null;
	}

	public FacetHit getFacetHit8(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 7 )
			return currentTabFacetHits.get(7);
		else
			return null;
	}

	public FacetHit getFacetHit9(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 8 )
			return currentTabFacetHits.get(8);
		else
			return null;
	}
	
	public FacetHit getFacetHit10(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 9 )
			return currentTabFacetHits.get(9);
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration1(){
		FacetHit facetHit = getFacetHit1();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}
	
	public FacetConfiguration getFacetConfiguration2(){
		FacetHit facetHit = getFacetHit2();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}
	
	public FacetConfiguration getFacetConfiguration3(){
		FacetHit facetHit = getFacetHit3();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration4(){
		FacetHit facetHit = getFacetHit4();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration5(){
		FacetHit facetHit = getFacetHit5();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration6(){
		FacetHit facetHit = getFacetHit6();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration7(){
		FacetHit facetHit = getFacetHit7();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration8(){
		FacetHit facetHit = getFacetHit8();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration9(){
		FacetHit facetHit = getFacetHit9();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}

	public FacetConfiguration getFacetConfiguration10(){
		FacetHit facetHit = getFacetHit10();
		if( facetHit != null )
			return facetConfigurations.get(facetHit.getFacet());
		else
			return null;
	}
	
	public Object onTabSelect(){
		selectedTab = request.getParameter(SELECTED_TAB_PARAMETER);
		if( selectedTab.equals(FIRST_TAB) )
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(firstTabConfigurations, documents);
		else if( selectedTab.equals(SECOND_TAB) )
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(secondTabConfigurations, documents);
		else if( selectedTab.equals(THIRD_TAB) )
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(thirdTabConfigurations, documents);

		return this;
	}
	
	@AfterRender
	void addJavaScript(MarkupWriter markupWriter){
		renderSupport.addScriptLink(tabsJS);
		Link link = resources.createEventLink(EVENT_NAME);
		renderSupport.addScript(INIT_JS, FACET_BAR_ID, 
								selectedTab, 
								link.toAbsoluteURI());
	}

	@BeginRender
	void initialize(){
		if( selectedTab == null )
			selectedTab = FIRST_TAB;
		
		if( firstTabConfigurations == null ){
			firstTabConfigurations = new ArrayList<FacetConfiguration>();
			secondTabConfigurations = new ArrayList<FacetConfiguration>();
			thirdTabConfigurations = new ArrayList<FacetConfiguration>();
			
			for( FacetConfiguration facetConfiguration: facetConfigurations.values()){
				Facet facet = facetConfiguration.getFacet();
				if( facet.getType() == Facet.BIO_MED )
					firstTabConfigurations.add(facetConfiguration);
				else if( facet.getType() == Facet.IMMUNOLOGY )
					secondTabConfigurations.add(facetConfiguration);
				else if( facet.getType() == Facet.BIBLIOGRAPHY )
					thirdTabConfigurations.add(facetConfiguration);
			}
		}
	}
}
