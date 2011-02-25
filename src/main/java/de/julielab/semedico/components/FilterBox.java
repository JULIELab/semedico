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

import de.julielab.semedico.base.FacetInterface;
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

public class FilterBox implements FacetInterface {
	
	private static final int MAX_PATH_ENTRY_LENGTH = 30;
	
	@Property
	@Parameter
	private FacetHit facetHit;
	
	@Property
	@Parameter
	private FacetConfiguration facetConfiguration;
	
	@SuppressWarnings("unused")
	@Property
	@Parameter("true")
	private boolean showLabelCount;
	
	@Property
	@Persist
	private Format abbreviationFormatter;
	
	@Property
	@Persist
	private DisplayGroup<Label> displayGroup;
	
	@Property
	private Label labelItem;
	
	@SuppressWarnings("unused")
	@Property
	private int labelIndex;
	
	@Parameter
	private Term selectedTerm;

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
		if( facetHit != null)
			return facetHit.getFacet().getCssId();
		else
			return null;
	}
	
	public String getBoxId(){
		return getClientId()+ "Box";
	}

	public String getPanelId(){
		return getClientId()+"Panel";
	}
	
	public String getListId(){
		return getClientId()+"List";
	}

	public String getPanelStyle(){
		return "display:"+ (facetConfiguration.isCollapsed()? "none": "block;");
	}

	public String getLabelDescription(){
		String description = "";
		
		Term term = labelItem.getTerm();
		if( term.getShortDescription() != null && !term.getShortDescription().equals("") ){
			description = "Synonyms: "+ term.getShortDescription()+ "<br/><br/>";
			description = description.replace(';', ',');
		}
		description += term.getDescription();
		
		return description;	
	}		
	
}
