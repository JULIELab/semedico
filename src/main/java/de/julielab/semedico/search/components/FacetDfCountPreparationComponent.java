package de.julielab.semedico.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.semedico.core.services.interfaces.IIndexInformationService;

public class FacetDfCountPreparationComponent implements ISearchComponent {

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FacetDfCountPreparation {}
	
	private final IIndexInformationService indexInformationService;

	public FacetDfCountPreparationComponent(IIndexInformationService indexInformationService){
		this.indexInformationService = indexInformationService;
		
	}
	
	@Override
	public boolean process(SearchCarrier searchCarrier) {
		
		SolrSearchCommand solrCmd = searchCarrier.solrCmd;
		solrCmd.dofacet = true;
		solrCmd.dofacetdf = true;
		solrCmd.rows = 0;
		
		FacetCommand fc = new FacetCommand();
		fc.limit = -1;
		fc.mincount = 1;
		fc.sort = "index";
		solrCmd.addFacetCommand(fc);
		
		fc = new FacetCommand();
		for (String field : indexInformationService.getBTermFieldNames())
			fc.addFacetField(field);
		solrCmd.addFacetCommand(fc);
			
		return false;
	}

}
