package de.julielab.semedico.components;

import java.text.Format;

import org.apache.tapestry5.annotations.BeginRender;
import org.apache.tapestry5.annotations.Parameter;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;

import de.julielab.semedico.base.FacetInterface;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.util.AbbreviationFormatter;
import de.julielab.semedico.util.DisplayGroup;
import de.julielab.semedico.util.LabelFilter;

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
	private FacetTerm selectedTerm;

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
		
		FacetTerm term = labelItem.getTerm();
		if( term.getShortDescription() != null && !term.getShortDescription().equals("") ){
			description = "Synonyms: "+ term.getShortDescription()+ "<br/><br/>";
			description = description.replace(';', ',');
		}
		description += term.getDescription();
		
		return description;	
	}		
	
}
