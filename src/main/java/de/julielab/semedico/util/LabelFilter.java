/**
 * 
 */
package de.julielab.semedico.util;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.util.DisplayGroup.Filter;

public class LabelFilter implements Filter<Label>{
	
	private String filterToken;

	public boolean displayObject(Label label) {
		if( filterToken ==  null )
			return true;
		
		return label.getTerm().getName().toLowerCase().startsWith(filterToken.toLowerCase());
	}
	
	public String getFilterToken() {
		return filterToken;
	}
	
	public void setFilterToken(String filterToken) {
		this.filterToken = filterToken;
	}
	
}