package de.julielab.semedico.components;

import java.util.List;
import java.util.Map;

import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.annotations.SessionState;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetGroup;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.SearchConfiguration;
import de.julielab.semedico.search.IFacetHitCollectorService;

public class Tabs {
	private final static String FIRST_TAB = "firstTab";
	private final static String SECOND_TAB = "secondTab";
	private final static String THIRD_TAB = "thirdTab";
	private final static String FOURTH_TAB = "fourthTab";
	private final static String FIFTH_TAB = "fifthTab";
	private final static String FIRST_TAB_ACTIVE = "firstTabActive";
	private final static String FIRST_TAB_INACTIVE = "firstTabInActive";
	private final static String SECOND_TAB_ACTIVE = "secondTabActive";
	private final static String SECOND_TAB_INACTIVE = "secondTabInActive";
	private final static String THIRD_TAB_ACTIVE = "thirdTabActive";
	private final static String THIRD_TAB_INACTIVE = "thirdTabInActive";
	private final static String FOURTH_TAB_ACTIVE = "fourthTabActive";
	private final static String FOURTH_TAB_INACTIVE = "fourthTabInActive";
	private final static String FIFTH_TAB_ACTIVE = "fifthTabActive";
	private final static String FIFTH_TAB_INACTIVE = "fifthTabInActive";
	private static final String EVENT_NAME = "tabselect";
	private static final String FACET_BAR_ID = "facetBar";
	private static final String INIT_JS = "var %s = new Tabs(\"%s\", \"%s\");";
	private static final String SELECTED_TAB_PARAMETER = "selectedTab";

	@Property
	@SessionState
	private SearchConfiguration searchConfiguration;
	
	@Inject
	@Path("tabs.js")
	private Asset tabsJS;

	@Property
	private int counter;

	@Property
	@Parameter
	private Map<Facet, FacetConfiguration> facetConfigurations;

	@Persist
	private Map<Integer, List<FacetConfiguration>> facetTypeMap;

	@Property
	@Parameter
	private FacetTerm selectedTerm;

	@Property
	@Parameter
	private int facet_nr;
	
	@SuppressWarnings("unused")
	@Property
	private int facetGroupLoopIndex;
	
	@SuppressWarnings("unused")
	@Property
	private FacetGroup facetGroupLoopItem;

	@Property
	@Parameter("true")
	private boolean showLabelCount;

	@Property
	@Parameter
	private FacetHit facetHit;

	@Persist
	private List<FacetConfiguration> firstTabConfigurations;

	@Persist
	private List<FacetConfiguration> secondTabConfigurations;

	@Persist
	private List<FacetConfiguration> thirdTabConfigurations;

	@Persist
	private List<FacetConfiguration> fourthTabConfigurations;

	@Persist
	private List<FacetConfiguration> fifthTabConfigurations;

	@Inject
	private ComponentResources resources;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private Request request;

	@Inject
	private IFacetHitCollectorService facetHitCollectorService;

	@Persist
	// this seems actually to be more of a tab type
	private int selectedFacetGroupIndex;

	// TODO Rather give the FacetGroup class a type attribute.
	public boolean isFilter() {
		return selectedFacetGroupIndex == Facet.FILTER;
	}

//	public String getFirstTabCSSClass() {
//		if (selectedFacetType == Facet.BIO_MED)
//			return FIRST_TAB_ACTIVE;
//		else
//			return FIRST_TAB_INACTIVE;
//	}
//
//	public String getSecondTabCSSClass() {
//		if (selectedFacetType == Facet.IMMUNOLOGY)
//			return SECOND_TAB_ACTIVE;
//		else
//			return SECOND_TAB_INACTIVE;
//	}
//
//	public String getThirdTabCSSClass() {
//		if (selectedFacetType == Facet.BIBLIOGRAPHY)
//			return THIRD_TAB_ACTIVE;
//		else
//			return THIRD_TAB_INACTIVE;
//	}
//
//	public String getFourthTabCSSClass() {
//		if (selectedFacetType == Facet.AGING)
//			return FOURTH_TAB_ACTIVE;
//		else
//			return FOURTH_TAB_INACTIVE;
//	}
//
//	public String getFifthTabCSSClass() {
//		if (selectedFacetType == Facet.FILTER)
//			return FIFTH_TAB_ACTIVE;
//		else
//			return FIFTH_TAB_INACTIVE;
//	}

	// public FacetHit getFacetHit(int facet_nr){
	// if( currentTabFacetHit != null &&
	// currentTabFacetHit.size() > facet_nr )
	// return currentTabFacetHit.get(facet_nr);
	// else
	// return null;
	// }
	//
	// public FacetHit getFacetHit1(){
	// return getFacetHit(0);
	// }
	// public FacetHit getFacetHit2(){
	// return getFacetHit(1);
	// }
	// public FacetHit getFacetHit3(){
	// return getFacetHit(2);
	// }
	// public FacetHit getFacetHit4(){
	// return getFacetHit(3);
	// }
	// public FacetHit getFacetHit5(){
	// return getFacetHit(4);
	// }
	// public FacetHit getFacetHit6(){
	// return getFacetHit(5);
	// }
	// public FacetHit getFacetHit7(){
	// return getFacetHit(6);
	// }
	// public FacetHit getFacetHit8(){
	// return getFacetHit(7);
	// }
	// public FacetHit getFacetHit9(){
	// return getFacetHit(8);
	// }
	// public FacetHit getFacetHit10(){
	// return getFacetHit(9);
	// }
	//
	// public void setFacetHit(FacetHit facetHit, int facet_nr){
	// if( currentTabFacetHit != null &&
	// currentTabFacetHit.size() > facet_nr )
	// currentTabFacetHit.set(facet_nr, facetHit);
	// }

	// public void setFacetHit1(FacetHit facetHit){
	// setFacetHit(facetHit, 0);
	// }
	// public void setFacetHit2(FacetHit facetHit){
	// setFacetHit(facetHit, 1);
	// }
	// public void setFacetHit3(FacetHit facetHit){
	// setFacetHit(facetHit, 2);
	// }
	// public void setFacetHit4(FacetHit facetHit){
	// setFacetHit(facetHit, 3);
	// }
	// public void setFacetHit5(FacetHit facetHit){
	// setFacetHit(facetHit, 4);
	// }
	// public void setFacetHit6(FacetHit facetHit){
	// setFacetHit(facetHit, 5);
	// }
	// public void setFacetHit7(FacetHit facetHit){
	// setFacetHit(facetHit, 6);
	// }
	// public void setFacetHit8(FacetHit facetHit){
	// setFacetHit(facetHit, 7);
	// }
	// public void setFacetHit9(FacetHit facetHit){
	// setFacetHit(facetHit, 8);
	// }
	// public void setFacetHit10(FacetHit facetHit){
	// setFacetHit(facetHit, 9);
	// }

	public FacetConfiguration getFacetConfiguration(int facet_nr) {
		FacetGroup currentFacetGroup = searchConfiguration.getFacetGroup(selectedFacetGroupIndex);
		if (facet_nr < currentFacetGroup.size()) {
			return searchConfiguration.getFacetConfigurations().get(currentFacetGroup.get(facet_nr));
		}
		
//		List<FacetConfiguration> currentFacetConfigurations = facetTypeMap
//				.get(selectedFacetGroupIndex);
//		if (facet_nr < currentFacetConfigurations.size()) {
//			return currentFacetConfigurations.get(facet_nr);
//		}
		return null;
	}

	public FacetConfiguration getFacetConfiguration1() {
		return getFacetConfiguration(0);
	}

	public FacetConfiguration getFacetConfiguration2() {
		return getFacetConfiguration(1);
	}

	public FacetConfiguration getFacetConfiguration3() {
		return getFacetConfiguration(2);
	}

	public FacetConfiguration getFacetConfiguration4() {
		return getFacetConfiguration(3);
	}

	public FacetConfiguration getFacetConfiguration5() {
		return getFacetConfiguration(4);
	}

	public FacetConfiguration getFacetConfiguration6() {
		return getFacetConfiguration(5);
	}

	public FacetConfiguration getFacetConfiguration7() {
		return getFacetConfiguration(6);
	}

	public FacetConfiguration getFacetConfiguration8() {
		return getFacetConfiguration(7);
	}

	public FacetConfiguration getFacetConfiguration9() {
		return getFacetConfiguration(8);
	}

	public FacetConfiguration getFacetConfiguration10() {
		return getFacetConfiguration(9);
	}
	
	public FacetConfiguration getFacetConfiguration11() {
		return getFacetConfiguration(10);
	}
	
	public FacetConfiguration getFacetConfiguration12() {
		return getFacetConfiguration(11);
	}
	
	public FacetConfiguration getFacetConfiguration13() {
		return getFacetConfiguration(12);
	}
	
	public FacetConfiguration getFacetConfiguration14() {
		return getFacetConfiguration(13);
	}
	
	public FacetConfiguration getFacetConfiguration15() {
		return getFacetConfiguration(14);
	}

	public Object onTabSelect() {
		String selectedTab = request.getParameter(SELECTED_TAB_PARAMETER);
		if (selectedTab.equals("0")) {
//			facetHit = facetHitCollectorService
//					.collectFacetHits(firstTabConfigurations);
			selectedFacetGroupIndex = Facet.BIO_MED;
		} else if (selectedTab.equals("1")) {
//			facetHit = facetHitCollectorService
//					.collectFacetHits(secondTabConfigurations);
			selectedFacetGroupIndex = Facet.IMMUNOLOGY;
		} else if (selectedTab.equals("2")) {
//			facetHit = facetHitCollectorService
//					.collectFacetHits(thirdTabConfigurations);
			selectedFacetGroupIndex = Facet.BIBLIOGRAPHY;
		} else if (selectedTab.equals("3")) {
//			facetHit = facetHitCollectorService
//					.collectFacetHits(fourthTabConfigurations);
			selectedFacetGroupIndex = Facet.AGING;
		} else if (selectedTab.equals("4")) {
//			facetHit = facetHitCollectorService
//					.collectFacetHits(fifthTabConfigurations);
			selectedFacetGroupIndex = Facet.FILTER;
		}
		return this;
	}

	@AfterRender
	void addJavaScript(MarkupWriter markupWriter) {
		javaScriptSupport.importJavaScriptLibrary(tabsJS);
		Link link = resources.createEventLink(EVENT_NAME);

		javaScriptSupport.addScript(INIT_JS, FACET_BAR_ID, selectedFacetGroupIndex,
				link.toAbsoluteURI());
	}

	@BeginRender
	void initialize() {
		if (searchConfiguration.isNewSearch())
			selectedFacetGroupIndex = 0;
		
//		if (facetTypeMap == null) {
//			facetTypeMap = new HashMap<Integer, List<FacetConfiguration>>();
//
//			// Distribution the facet configurations corresponding to their
//			// tab categories (e.g. "BioMed").
//			for (FacetConfiguration facetConfiguration : searchConfiguration.getFacetConfigurations()
//					.values()) {
//				Facet facet = facetConfiguration.getFacet();
//				int facetType = facet.getType();
//				List<FacetConfiguration> facetConfigurations = facetTypeMap
//						.get(facetType);
//				if (facetTypeMap.get(facetType) == null) {
//					facetConfigurations = new ArrayList<FacetConfiguration>();
//					facetTypeMap.put(Integer.valueOf(facet.getType()),
//							facetConfigurations);
//				}
//				facetConfigurations.add(facetConfiguration);
//			}
//
//			// Sort the facets according to their order. This order is initially
//			// set by default values in the database, but can be altered by
//			// searches or user wishes.
//			for (List<FacetConfiguration> facetConfigurations : facetTypeMap
//					.values())
//				Collections.sort(facetConfigurations);
//		}
	}
}
