package de.julielab.semedico.components;

import java.text.Format;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.solr.util.OpenBitSet;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.RenderSupport;
import org.apache.tapestry5.annotations.AfterRender;
import org.apache.tapestry5.annotations.ApplicationState;
import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Path;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import de.julielab.semedico.state.Client;
import de.julielab.semedico.state.IClientIdentificationService;
import de.julielab.semedico.util.AbbreviationFormatter;
import de.julielab.semedico.util.DisplayGroup;
import de.julielab.semedico.util.LabelFilter;
import de.julielab.stemnet.core.Facet;
import de.julielab.stemnet.core.FacetConfiguration;
import de.julielab.stemnet.core.FacetHit;
import de.julielab.stemnet.core.Label;
import de.julielab.stemnet.core.Term;
import de.julielab.stemnet.search.FacetHitCollectorService;
import de.julielab.stemnet.search.IFacetHitCollectorService;

public class FacetBox {
	private static final int MAX_PATH_ENTRY_LENGTH = 30;
	private static final String EVENT_NAME = "action";
	private final static String EXPAND_LIST_PARAM ="expandList";
	private final static String PAGER_PARAM ="pager";
	private final static String COLLAPSE_PARAM = "collapse";
	private final static String FILTER_TOKEN_PARAM = "filterToken";
	private final static String CLEAR_FILTER_PARAM = "clearFilter";
	private final static String HIDE_PARAM = "hide";
	private final static String HIERARCHIC_MODE_PARAM = "hierarchicMode";
	private final static String DRILL_UP_PARAM = "drillUp";
	private final static String DRILL_TO_TOP_PARAM = "drillToTop";
	
	@Property
	@Parameter
	private FacetHit facetHit;
	@Property
	@Parameter 
	private FacetConfiguration facetConfiguration;
	@Property
	@Parameter("true")
	private boolean showLabelCount;
	
	@Property
	@Parameter("true")
	private boolean viewModeSwitchable;
	
	@Parameter
	private OpenBitSet documents;
	
	@Property
	private Term pathItem;
	
	@Property
	private int pathItemIndex;
	
	@Property
	@Persist
	private Format abbreviationFormatter;
	
	@Property
	@Persist
	private DisplayGroup<Label> displayGroup;

	@ApplicationState
	private Client client;
	
	@Property
	private Label labelItem;
	
	@Property
	private int labelIndex;
	
	@Parameter
	private Term selectedTerm;
	
	@Inject @Path("facetbox.js")
	private Asset facetBoxJS;

	private static String INIT_JS = "var %s = new FacetBox(\"%s\", \"%s\", %s, %s, %s)";
	
	@Inject
    private Request request;

	@Inject
    private ComponentResources resources;

	@Environmental
	private RenderSupport renderSupport;
	
	@Inject
	private IFacetHitCollectorService facetHitCollectorService;
	
	@Inject
	private Logger logger;
	
	@BeginRender
	public void initialize(){
		if( abbreviationFormatter == null )
			abbreviationFormatter = new AbbreviationFormatter(MAX_PATH_ENTRY_LENGTH);
		if( displayGroup == null ){
			displayGroup = new DisplayGroup<Label>();
			displayGroup.setBatchSize(3);
			displayGroup.setFilter(new LabelFilter());
		}
		
		if( facetHit != null ){
			int currentBatch = displayGroup.getCurrentBatchNumber();
			displayGroup.setAllObjects(facetHit.getLabels());
			displayGroup.displayBatch(currentBatch);
		}
	}
	
	@AfterRender
	void addJavaScript(MarkupWriter markupWriter){
		renderSupport.addScriptLink(facetBoxJS);
		Link link = resources.createEventLink(EVENT_NAME);
		String id = getClientId();
		renderSupport.addScript(INIT_JS, id, id, link.toAbsoluteURI(), 
								facetConfiguration.isExpanded(), 
								facetConfiguration.isCollapsed(), 
								facetConfiguration.isHierarchicMode());
	}
	
	private void changeExpansion(boolean expanded){

		if( expanded ){
			displayGroup.displayBatch(1);
			displayGroup.setBatchSize(20);
			
			facetConfiguration.setExpanded(true);
			facetConfiguration.setCollapsed(false);				
		}
		else{
			displayGroup.displayBatch(1);
			displayGroup.setBatchSize(3);
			facetConfiguration.setExpanded(false);
			facetConfiguration.setCollapsed(false);
		}

		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		filter.setFilterToken(null);
		displayGroup.setFilter(filter);
	}
	
	private void changeCollapsation(boolean collapsed){
		if( collapsed ){
			facetConfiguration.setCollapsed(true);
			facetConfiguration.setExpanded(false);
		}
		else{
			facetConfiguration.setCollapsed(false);
			facetConfiguration.setExpanded(false);
		}

		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		filter.setFilterToken(null);
		displayGroup.setFilter(filter);

		displayGroup.displayBatch(1);
		displayGroup.setBatchSize(3);			
	}
	
	public Object onAction() {
		
		String isExpanded = request.getParameter(EXPAND_LIST_PARAM);
		String collapse = request.getParameter(COLLAPSE_PARAM);
		String pager = request.getParameter(PAGER_PARAM);
		String filterToken = request.getParameter(FILTER_TOKEN_PARAM);
		String clearFilter = request.getParameter(CLEAR_FILTER_PARAM);
		String hide = request.getParameter(HIDE_PARAM);
		String hierarchicMode = request.getParameter(HIERARCHIC_MODE_PARAM);
		String drillUp = request.getParameter(DRILL_UP_PARAM);
		String drillToTop = request.getParameter(DRILL_TO_TOP_PARAM);
		
		logger.info("trigger() isExpanded: " + isExpanded + " collapse: " + collapse + " pager " + pager + 
					" filterToken " + filterToken + " clearFilter " + clearFilter + " hide " + hide + 
					" hierarchicMode " +hierarchicMode + " drillUp " + drillUp + " drillToTop " + drillToTop);
		
		
		if( isExpanded != null )
			changeExpansion(Boolean.parseBoolean(isExpanded));
						
		if( pager != null ){
			if( pager.equals("next") ){
				displayGroup.displayNextBatch();
			}
			else{
				displayGroup.displayPreviousBatch();
			}					
		}

		if( collapse != null )
			changeCollapsation(Boolean.parseBoolean(collapse));
		
		if( filterToken != null ){
			LabelFilter filter = (LabelFilter) displayGroup.getFilter();
			filter.setFilterToken(filterToken);
			displayGroup.setFilter(filter);
		}

		if( clearFilter != null ){
			LabelFilter filter = (LabelFilter) displayGroup.getFilter();
			filter.setFilterToken(null);
			displayGroup.setFilter(filter);
		}
		
		if( hide != null && hide.equals("true") ){
			facetConfiguration.setHidden(true);
		}
		
		if( hierarchicMode != null ){
			switchViewMode();
		}
		
		if( drillUp != null ){
			int index = Integer.parseInt(drillUp);
			drillUp(index);
		}
		
		if( drillToTop != null ){
			drillToTop();
		}
		
		return this;
	}
	
	public void drillUp(int index){
		List<Term> path = facetConfiguration.getCurrentPath();

		if( index < 0 || index >= path.size() )
			return;
		
		for( Iterator<Term> iterator = path.iterator(); iterator.hasNext(); ){
			Term term = iterator.next();
			if( path.indexOf(term) > index )
				iterator.remove();
		}
		
		refreshFacetHit();
	}
	
	private void refreshFacetHit(){
		Iterator<FacetHit> hitsIterator = facetHitCollectorService.collectFacetHits(Lists.newArrayList(facetConfiguration), documents).iterator(); 

		if( hitsIterator.hasNext() ){
			facetHit = hitsIterator.next();
			displayGroup.setAllObjects(facetHit.getLabels());
		}
	}
	
	public void drillToTop(){
		List<Term> path = facetConfiguration.getCurrentPath();
		path.clear();
		
		refreshFacetHit();
	}
	
	public void switchViewMode(){
		if( facetConfiguration.isHierarchicMode() )
			facetConfiguration.getCurrentPath().clear();
		
		facetConfiguration.setHierarchicMode(!facetConfiguration.isHierarchicMode());
		
		refreshFacetHit();
	}	
	
	public void onTermSelect(int index){
		if( displayGroup.getDisplayedObjects().size() > index ){
			Label label = displayGroup.getDisplayedObjects().get(index);
			selectedTerm = label.getTerm();
			if( facetConfiguration.isHierarchicMode() ){
				facetConfiguration.getCurrentPath().clear();
				facetConfiguration.getCurrentPath().addAll(selectedTerm.getAllParents());
				if( label.hasChildHits() )
					facetConfiguration.getCurrentPath().add(selectedTerm);
				
			}
		}
	}
	
	public boolean getIsHidden(){
		if( facetHit == null )
			return true;
		if( facetHit.getLabels().size() == 0 )
			return true;
		
		if( facetConfiguration != null && facetConfiguration.isHidden() )
			return true;
		
		return false;
	}
	
	public String getClientId(){
		return facetHit.getFacet().getCssId();
	}
	
	public String getBoxId(){
		return getClientId()+ "Box";
	}
	
	public String getCollapseLinkId(){
		return getClientId()+ "CollapseLink";
	}
	
	public String getModeSwitchLinkId(){
		return getClientId()+ "ModeSwitchLink";
	}
	
	public String getCloseLinkId(){
		return getClientId()+ "CloseLink";
	}
	public String getFacetBoxHeaderPathStyle(){
		return "display:"+ (facetConfiguration.isCollapsed()? "none": "block;");
	}
	
	public String getPathEntryStyle(){
		return "margin-left:"+getPathMargin()+"px";
	}
	
	public String getPathLinkId(){
		return getClientId() + "pathLink" +pathItemIndex;
	}
	
	public String getModeSwitchLinkClass(){
		return facetConfiguration.isHierarchicMode() ? "modeSwitchLinkList" : "modeSwitchLinkTree"; 
	}
	
	public String getPanelId(){
		return getClientId()+"Panel";
	}
	
	public String getListId(){
		return getClientId()+"List";
	}
	
	public String getLinkId(){
		return getClientId()+"Link";
	}
	
	public String getTopLinkId(){
		return getClientId()+"TopLink";
	}
	public String getPagerPreviousLinkId(){
		return getClientId()+"PagerPreviousLink";
	}
	
	public String getPagerNextLinkId(){
		return getClientId()+"PagerNextLink";
	}
	
	public String getPanelStyle(){
		return "display:"+ (facetConfiguration.isCollapsed()? "none": "block;");
	}
	
	public String getLabelFilterStyle(){
		return "margin-left:" + getElementMargin() + "px;";
	}
	
	public String getLabelStyle(){
		return "margin-left:" + getListMargin() + "px;";
	}
	
	public String getBoxFooterLeftStyle(){
		return "margin-left:"+getFooterMargin()+"px;";
	}
	
	public String getBoxFooterLeftMaximizedStyle(){
		return "margin-left:" + getFooterMargin() + "px;";
	}
	
	public String getLabelClass(){
		if( !facetConfiguration.isHierarchicMode() )
			return "list";
		else if( labelItem.hasChildHits() )
			return "tree";
		else
			return "list";
	}
	
	public boolean showFilter(){
		 return (facetConfiguration.isExpanded() && 
				 displayGroup.hasMultipleBatches()) || 
				 isFiltered();	
	}
	
	public boolean showMore(){
		return displayGroup.getAllObjects().size() > 3;
	}
	
	public int getPathMargin(){
		return (pathItemIndex+1)*7;
	}
	
	public int getElementMargin(){		
		
		if( client.getName().equals(IClientIdentificationService.IEXPLORER))
			return getPathMargin()+5;
		else
			return getPathMargin()+15;
	}
	
	public int getFooterMargin(){		
		if( client.equals(Client.IEXPLORER6))
			return getPathMargin()+5;
		else
			return getPathMargin()+15;
	}
	
	public boolean isFiltered(){
		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		return filter.getFilterToken() != null && !filter.getFilterToken().equals("");
	}
	
	public String getFilterValue(){
		LabelFilter filter = (LabelFilter) displayGroup.getFilter();
		if( filter.getFilterToken() == null ||  filter.getFilterToken().trim().equals("") )
			return "type to filter";
		else
			return filter.getFilterToken();
	}
	
	public int getListMargin(){
		return getPathMargin()+7;
	}
}
