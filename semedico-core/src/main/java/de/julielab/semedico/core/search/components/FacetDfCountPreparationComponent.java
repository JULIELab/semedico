package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.julielab.scicopia.core.elasticsearch.legacy.AbstractSearchComponent;
import de.julielab.scicopia.core.elasticsearch.legacy.FacetCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchServerCommand;
import de.julielab.scicopia.core.elasticsearch.legacy.FacetCommand.SortOrder;
import de.julielab.scicopia.core.elasticsearch.legacy.SearchCarrier;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.facets.FacetLabels.Unique;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.services.interfaces.IFacetService;

/**
 * This component configures the b-term field terms to be counted for document frequencies.
 * 
 * @author faessler
 * 
 */
public class FacetDfCountPreparationComponent extends AbstractSearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetDfCountPreparation {
		//
	}

	private IFacetService facetService;

	public FacetDfCountPreparationComponent(IFacetService facetService) {
		this.facetService = facetService;

	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();

		Facet btermFacet = facetService.getFacetByLabel(Unique.BTERMS);
		if (null == btermFacet)
			throw new IllegalStateException("There is no facet with label "+ Unique.BTERMS);
		Set<General> aggregationLabels = btermFacet.getAggregationLabels();
		List<Facet> aggregateFacets = facetService.getFacetsByLabels(aggregationLabels);
		for (Facet facet : aggregateFacets) {
			// If the aggregation facet defines a field name, then this is a field other than the default facet
			// field. It is just the trunk of the facet-specific
			// field names and we must append the facet ID to get the actual field names.
			// Otherwise, we directly use the original facet field name.
			String field =
					!StringUtils.isEmpty(btermFacet.getSource().getName()) ? btermFacet.getSource().getName() + facet
							.getId() : facet.getSource().getName();
			FacetCommand fc = new FacetCommand();
			fc.name = field;
			fc.limit = -1;
			fc.sort = SortOrder.TERM;
			fc.setField(field);
			serverCmd.addFacetCommand(fc);
		}
		for (String aggregationField : btermFacet.getAggregationFields()) {
			FacetCommand fc = new FacetCommand();
			fc.name = aggregationField;
			// TODO magic number
			fc.limit = 100;
			fc.sort = SortOrder.TERM;
			fc.setField(aggregationField);

			serverCmd.addFacetCommand(fc);
		}

		return false;
	}

}
