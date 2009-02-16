package de.julielab.semedico.components;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.solr.util.OpenBitSet;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.Term;

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
	
	@Property
	@Parameter
	private Collection<FacetConfiguration> allFacetConfigurations;
	
	@Property
	@Parameter
	private Map<Facet, FacetConfiguration> currentTabFacetConfigurations;
	
	@Property
	@Parameter
	private List<FacetHit> currentTabFacetHits;
	
	@Property
	@Parameter
	private int currentFacetType;
	
	@Property
	@Parameter
	private OpenBitSet documents;

	@Property
	@Parameter
	private Term selectedTerm;
	
	@Property
	@Persist
	private String selectedTab;
	
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
	
	
	@BeginRender
	void initialize(){
		if( selectedTab == null )
			selectedTab = FIRST_TAB;
	}
}
