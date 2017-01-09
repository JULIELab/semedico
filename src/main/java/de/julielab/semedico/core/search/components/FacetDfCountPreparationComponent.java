package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.FacetCommand;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.FacetCommand.SortOrder;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetLabels.General;
import de.julielab.semedico.core.facets.FacetLabels.Unique;
import de.julielab.semedico.core.facetterms.CoreTerm;
import de.julielab.semedico.core.facetterms.CoreTerm.CoreTermType;
import de.julielab.semedico.core.parsing.ParseTree;
import de.julielab.semedico.core.parsing.TextNode;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;

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
	private ITermService termService;

	public FacetDfCountPreparationComponent(ITermService termService, IFacetService facetService) {
		this.termService = termService;
		this.facetService = facetService;

	}

	@Override
	public boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();
//		if (null == serverCmd) {
//			serverCmd = new SearchServerCommand();
//			serverCmd.serverQuery = "*:*";
//			searchCarrier.addSearchServerCommand(serverCmd);
//		}
		if (null == semCarrier.searchCmd || null == semCarrier.searchCmd.semedicoQuery) {
			CoreTerm anyTerm = termService.getCoreTerm(CoreTermType.ANY_TERM);
			TextNode anyNode = new TextNode(anyTerm.getPreferredName());
			anyNode.setTerms(Arrays.asList(anyTerm));
			ParseTree query = new ParseTree(anyNode, null);
			if (null == semCarrier.searchCmd)
				semCarrier.searchCmd = new SemedicoSearchCommand();
			semCarrier.searchCmd.semedicoQuery = query;
			semCarrier.searchCmd.docSize = 0;
		}
		// serverCmd.dofacetdf = true;
//		serverCmd.rows = 0;

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
			fc.mincount = 1;
			fc.sort = SortOrder.TERM;
			fc.addFacetField(field);
			serverCmd.addFacetCommand(fc);
		}
		for (String aggregationField : btermFacet.getAggregationFields()) {
			FacetCommand fc = new FacetCommand();
			fc.name = aggregationField;
			// TODO magic number
			fc.mincount = 1;
			// TODO magic number
			fc.limit = 100;
			fc.sort = SortOrder.TERM;
			fc.fields.add(aggregationField);

			serverCmd.addFacetCommand(fc);
		}

		return false;
	}

}
