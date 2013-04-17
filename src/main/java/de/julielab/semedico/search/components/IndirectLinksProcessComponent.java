package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import org.apache.tapestry5.services.ApplicationStateManager;

import de.julielab.semedico.core.BTermUserInterfaceState;
import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.LabelStore;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.UIFacet;
import de.julielab.semedico.core.services.interfaces.IFacetService;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.services.interfaces.IUIService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;

public class IndirectLinksProcessComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface IndirectLinksProcess {}
	
	private final ApplicationStateManager asm;
	private final ITermService termService;
	private final IFacetService facetService;
	private final IUIService uiService;

	public IndirectLinksProcessComponent(ApplicationStateManager asm,
			ITermService termService, IFacetService facetService, IUIService uiService) {
		this.asm = asm;
		this.termService = termService;
		this.facetService = facetService;
		this.uiService = uiService;

	}

	@Override
	public boolean process(SearchCarrier searchCarrier) {
		if (null == searchCarrier.searchResult
				|| null == searchCarrier.searchResult.indirectLinkLabels)
			throw new IllegalArgumentException(
					"An instance of "
							+ SemedicoSearchResult.class.getName()
							+ " with a non-empty list of indrect link labels is expected but was not passed.");
		List<Label> indirectLinkLabels = searchCarrier.searchResult.indirectLinkLabels;
		organizeIndirectLinksInUI(indirectLinkLabels);
		return false;
	}

	private void organizeIndirectLinksInUI(List<Label> indirectLinksLabels) {
		BTermUserInterfaceState uiState = asm
				.get(BTermUserInterfaceState.class);

		LabelStore labelStore = uiState.getLabelStore();

		for (Label l : indirectLinksLabels) {
			if (termService.hasNode(l.getId())) {
				TermLabel termLabel = (TermLabel) l;
				labelStore.addTermLabel(termLabel);
				IFacetTerm term = termLabel.getTerm();
				for (Facet facet : term.getFacets()) {
					labelStore.incrementTotalFacetCount(facet, 1);
					labelStore.addLabelForFacet(l, facet.getId());
				}
			}
			labelStore.addLabelForFacet(l, IFacetService.FACET_ID_BTERMS);
		}
		uiService.resolveChildHitsRecursively(labelStore);
		Facet bTermFacet = facetService
				.getFacetById(IFacetService.FACET_ID_BTERMS);
		labelStore.setTotalFacetCount(bTermFacet, labelStore.getFlatLabels()
				.get(IFacetService.FACET_ID_BTERMS).size());
		for (UIFacet uiFacet : uiState.getFacetConfigurations().values())
			uiService.sortLabelsIntoFacet(labelStore, uiFacet);
	}
}
