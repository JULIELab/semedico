package de.julielab.semedico.base;

import org.apache.tapestry5.annotations.BeginRender;

public interface FacetInterface {
	
	public static final int MAX_PATH_ENTRY_LENGTH = 30;
	
	public final static String EVENT_NAME = "action";
	public final static String EXPAND_LIST_PARAM ="expandList";
	public final static String PAGER_PARAM ="pager";
	public final static String COLLAPSE_PARAM = "collapse";
	public final static String FILTER_TOKEN_PARAM = "filterToken";
	public final static String CLEAR_FILTER_PARAM = "clearFilter";
	public final static String HIDE_PARAM = "hide";
	public final static String HIERARCHIC_MODE_PARAM = "hierarchicMode";
	public final static String DRILL_UP_PARAM = "drillUp";
	public final static String DRILL_TO_TOP_PARAM = "drillToTop";

	@BeginRender
	public boolean initialize();
	
	public void onTermSelect(String termIndexAndFacetId);
	
	public boolean getIsHidden();
	
	public String getClientId();
	
	public String getBoxId();

	public String getPanelId();
	
	public String getListId();

	public String getPanelStyle();

	public String getLabelDescription();
	
}
