package de.julielab.semedico.components;

import de.julielab.semedico.core.entities.state.AbstractUserInterfaceState;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.facets.UIFacetGroupSection;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.search.services.ISearchService;
import de.julielab.semedico.core.services.SemedicoSymbolConstants;
import org.apache.tapestry5.Asset;
import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.annotations.*;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.ioc.annotations.Symbol;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.slf4j.Logger;

//@Import(library = { "context:js/Sortable.min.js" })
@Import(stylesheet="context:css/facetsection.css")
public class FacetSection {

	@Property
	@Parameter(required = true)
	private UIFacetGroupSection section;

	@Parameter(required = true)
	private ParseTree query;

	@Property
	@Parameter(required = true)
	private AbstractUserInterfaceState uiState;
	
	@Parameter(value = "true")
	private boolean sendFacetLoadingCallback;
	
	@Property
	private UIFacet facetItem;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources resources;

	@Inject
	@Path("context:images/loader.gif")
	private Asset loaderImage;

	@Inject
	private ComponentResources componentResources;

	@Inject
	private ISearchService searchService;

	@Inject
	private Logger log;

	@Inject
	@Symbol(SemedicoSymbolConstants.MAX_DISPLAYED_FACETS)
	private int maxFacets;

	private String elementId;

	@SetupRender
	public boolean setupRender() {
		if (null == section)
			return false;
		return true;
	}

	public UIFacet getUiFacet(int index) {
		if (index < section.size() && index < maxFacets) {
			UIFacet uiFacet = section.get(index);
			log.debug("Displaying facet nr {}: {} (UIFacet: {}).",
					new Object[] { index, uiFacet.getName(), System.identityHashCode(uiFacet) });
			return uiFacet;
		}
		return null;
	}

	public UIFacet getUiFacet1() {
		return getUiFacet(0);
	}

	public UIFacet getUiFacet2() {
		return getUiFacet(1);
	}

	public UIFacet getUiFacet3() {
		return getUiFacet(2);
	}

	public UIFacet getUiFacet4() {
		return getUiFacet(3);
	}

	public UIFacet getUiFacet5() {
		return getUiFacet(4);
	}

	public UIFacet getUiFacet6() {
		return getUiFacet(5);
	}

	public UIFacet getUiFacet7() {
		return getUiFacet(6);
	}

	public UIFacet getUiFacet8() {
		return getUiFacet(7);
	}

	public UIFacet getUiFacet9() {
		return getUiFacet(8);
	}

	public UIFacet getUiFacet10() {
		return getUiFacet(9);
	}

	public UIFacet getUiFacet11() {
		return getUiFacet(10);
	}

	public UIFacet getUiFacet12() {
		return getUiFacet(11);
	}

	public UIFacet getUiFacet13() {
		return getUiFacet(12);
	}

	public UIFacet getUiFacet14() {
		return getUiFacet(13);
	}

	public UIFacet getUiFacet15() {
		return getUiFacet(14);
	}

	public String getFacetSectionElementId() {
		elementId = javaScriptSupport.allocateClientId(resources);
		return elementId;
	}

	public Object onLoadSectionFacets() {
		log.debug("Section \"{}\": Load section facets called.", section.getName());
//		try {
//			// we need to synchronize here by calling get(); otherwise we will run into concurrency issues when the
//			// template loop is trying to read the facets that are currently loaded and written into the same
//			// datastructure from which is read
//
//			// TODO repair
//			//searchService.doFacetNavigationSearch(section, query).get();
//		} catch (InterruptedException | ExecutionException e) {
//			e.printStackTrace();
//		}
		return this;
	}

	public boolean isBTermView() {
		String containerName = componentResources.getContainer().getClass().getSimpleName();
		return containerName.equalsIgnoreCase("btermtabs");
	}

	public void onFacetSort(int fromIndex, int toIndex) {
		section.moveFacet(fromIndex, toIndex);
	}

	@AfterRender
	public boolean afterRender() {
		if (sendFacetLoadingCallback) {
			Link loadSectionFacetsEventLink = resources.createEventLink("loadSectionFacets");
			javaScriptSupport.addScript("loadSectionFacets('%s', '%s', '%s')", loadSectionFacetsEventLink,
					loaderImage.toClientURL(), elementId);
		}
		return true;
	}
}
