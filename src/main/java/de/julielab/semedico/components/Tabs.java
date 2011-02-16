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
	private final static String FOURTH_TAB = "fourthTab";	
	private final static String FIRST_TAB_ACTIVE = "firstTabActive"; 
	private final static String FIRST_TAB_INACTIVE = "firstTabInActive";
	private final static String SECOND_TAB_ACTIVE = "secondTabActive"; 
	private final static String SECOND_TAB_INACTIVE = "secondTabInActive";
	private final static String THIRD_TAB_ACTIVE = "thirdTabActive"; 
	private final static String THIRD_TAB_INACTIVE = "thirdTabInActive";
	private final static String FOURTH_TAB_ACTIVE = "fourthTabActive"; 
	private final static String FOURTH_TAB_INACTIVE = "fourthTabInActive";	
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
	@Parameter("true")
	private boolean showLabelCount;

	@Property
	@Parameter
	private List<FacetHit> currentTabFacetHits;
	
	@Persist
	private Collection<FacetConfiguration> firstTabConfigurations;
	
	@Persist
	private Collection<FacetConfiguration> secondTabConfigurations;
	
	@Persist
	private Collection<FacetConfiguration> thirdTabConfigurations;

	@Persist
	private Collection<FacetConfiguration> fourthTabConfigurations;
	
	@Inject
    private ComponentResources resources;

	@Environmental
	private RenderSupport renderSupport;
	
	@Inject
    private Request request;
	
	@Inject
	private IFacetHitCollectorService facetHitCollectorService;
	
	@Parameter
	private int selectedFacetType;
	
	public String getFirstTabCSSClass(){
		if( selectedFacetType == Facet.BIO_MED  )
			return FIRST_TAB_ACTIVE;
		else
			return FIRST_TAB_INACTIVE;
	}

	public String getSecondTabCSSClass(){
		if( selectedFacetType == Facet.IMMUNOLOGY  )
			return SECOND_TAB_ACTIVE;
		else
			return SECOND_TAB_INACTIVE;
	}

	public String getThirdTabCSSClass(){
		if( selectedFacetType == Facet.BIBLIOGRAPHY  )
			return THIRD_TAB_ACTIVE;
		else
			return THIRD_TAB_INACTIVE;
	}
	
	public String getFourthTabCSSClass(){
		if( selectedFacetType == Facet.BIBLIOGRAPHY  )
			return FOURTH_TAB_ACTIVE;
		else
			return FOURTH_TAB_INACTIVE;
	}
	
	public FacetHit getFacetHit1(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 0 )
			return currentTabFacetHits.get(0);
		else
			return null;
	}
	
	public void setFacetHit1(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 0 )
				currentTabFacetHits.set(0, facetHit);
	}

	public FacetHit getFacetHit2(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 1 )
			return currentTabFacetHits.get(1);
		else
			return null;
	}

	public void setFacetHit2(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 1 )
				currentTabFacetHits.set(1, facetHit);
	}

	public FacetHit getFacetHit3(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 2 )
			return currentTabFacetHits.get(2);
		else
			return null;
	}

	public void setFacetHit3(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 2 )
				currentTabFacetHits.set(2, facetHit);
	}

	public FacetHit getFacetHit4(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 3 )
			return currentTabFacetHits.get(3);
		else
			return null;
	}

	public void setFacetHit4(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 3 )
				currentTabFacetHits.set(3, facetHit);
	}

	public FacetHit getFacetHit5(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 4 )
			return currentTabFacetHits.get(4);
		else
			return null;
	}

	public void setFacetHit5(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 4 )
				currentTabFacetHits.set(4, facetHit);
	}

	public FacetHit getFacetHit6(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 5 )
			return currentTabFacetHits.get(5);
		else
			return null;
	}

	public void setFacetHit6(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 5 )
				currentTabFacetHits.set(5, facetHit);
	}

	public FacetHit getFacetHit7(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 6 )
			return currentTabFacetHits.get(6);
		else
			return null;
	}

	public void setFacetHit7(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 6 )
				currentTabFacetHits.set(6, facetHit);
	}

	
	public FacetHit getFacetHit8(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 7 )
			return currentTabFacetHits.get(7);
		else
			return null;
	}

	public void setFacetHit8(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 7 )
				currentTabFacetHits.set(7, facetHit);
	}

	public FacetHit getFacetHit9(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 8 )
			return currentTabFacetHits.get(8);
		else
			return null;
	}

	public void setFacetHit9(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 8 )
				currentTabFacetHits.set(8, facetHit);
	}

	public FacetHit getFacetHit10(){
		if( currentTabFacetHits != null && 
			currentTabFacetHits.size() > 9 )
			return currentTabFacetHits.get(9);
		else
			return null;
	}

	public void setFacetHit10(FacetHit facetHit){
		if( currentTabFacetHits != null && 
				currentTabFacetHits.size() > 9 )
				currentTabFacetHits.set(9, facetHit);
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
		String selectedTab = request.getParameter(SELECTED_TAB_PARAMETER);
		if( selectedTab.equals(FIRST_TAB) ){
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(firstTabConfigurations, documents);
			selectedFacetType = Facet.BIO_MED;
		}
		else if( selectedTab.equals(SECOND_TAB) ){
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(secondTabConfigurations, documents);
			selectedFacetType = Facet.IMMUNOLOGY;
		}
		else if( selectedTab.equals(THIRD_TAB) ){
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(thirdTabConfigurations, documents);
			selectedFacetType = Facet.BIBLIOGRAPHY;
		}
		else if( selectedTab.equals(FOURTH_TAB) ){
			currentTabFacetHits = facetHitCollectorService.collectFacetHits(fourthTabConfigurations, documents);
			selectedFacetType = Facet.BIBLIOGRAPHY;
		}		
		return this;
	}
	
	@AfterRender
	void addJavaScript(MarkupWriter markupWriter){
		renderSupport.addScriptLink(tabsJS);
		Link link = resources.createEventLink(EVENT_NAME);
		String selectedTab = null;
		if( selectedFacetType == Facet.BIO_MED )
			selectedTab = FIRST_TAB;
		else if( selectedFacetType == Facet.IMMUNOLOGY )
			selectedTab = SECOND_TAB;
		else if( selectedFacetType == Facet.BIBLIOGRAPHY )
			selectedTab = THIRD_TAB;
		
		renderSupport.addScript(INIT_JS, FACET_BAR_ID, 
								selectedTab, 
								link.toAbsoluteURI());
	}

	@BeginRender
	void initialize(){
		if( firstTabConfigurations == null ){
			firstTabConfigurations = new ArrayList<FacetConfiguration>();
			secondTabConfigurations = new ArrayList<FacetConfiguration>();
			thirdTabConfigurations = new ArrayList<FacetConfiguration>();
			fourthTabConfigurations = new ArrayList<FacetConfiguration>();

			
			for( FacetConfiguration facetConfiguration: facetConfigurations.values()){
				Facet facet = facetConfiguration.getFacet();
				if( facet.getType() == Facet.BIO_MED )
					firstTabConfigurations.add(facetConfiguration);
				else if( facet.getType() == Facet.IMMUNOLOGY )
					secondTabConfigurations.add(facetConfiguration);
				else if( facet.getType() == Facet.BIBLIOGRAPHY ) {
					thirdTabConfigurations.add(facetConfiguration);
					fourthTabConfigurations.add(facetConfiguration);
				}
				
			}
		}
	}
}
