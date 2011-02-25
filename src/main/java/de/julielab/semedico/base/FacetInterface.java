package de.julielab.semedico.base;

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
	public void initialize();
	
	public void onTermSelect(int index);
	
	public boolean getIsHidden();
	
	public String getClientId();
	
	public String getBoxId();

	public String getPanelId();
	
	public String getListId();

	public String getPanelStyle();

	public String getLabelDescription();
	
}
