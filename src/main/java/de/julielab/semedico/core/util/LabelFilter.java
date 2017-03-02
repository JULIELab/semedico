/**
 * 
 */
package de.julielab.semedico.core.util;

import org.apache.commons.lang.StringUtils;

import de.julielab.semedico.core.search.components.data.Label;
import de.julielab.semedico.core.util.DisplayGroup.Filter;

public class LabelFilter implements Filter<Label>{
	
	private String filterToken;

	public boolean displayObject(Label label) {
		if( filterToken ==  null )
			return true;
		
		return label.getName().toLowerCase().contains(filterToken.toLowerCase());
	}
	
	public String getFilterToken() {
		return filterToken;
	}
	
	public void setFilterToken(String filterToken) {
		this.filterToken = filterToken;
	}

	/* (non-Javadoc)
	 * @see de.julielab.util.DisplayGroup.Filter#reset()
	 */
	@Override
	public void reset() {
		filterToken = null;
	}

	/* (non-Javadoc)
	 * @see de.julielab.util.DisplayGroup.Filter#isFiltering()
	 */
	@Override
	public boolean isFiltering() {
		return !StringUtils.isEmpty(filterToken);
	}
	
}