package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.FieldTermsCommand;
import de.julielab.elastic.query.components.data.FieldTermsCommand.OrderType;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerCommand;
import de.julielab.elastic.query.components.data.aggregation.AggregationCommand.OrderCommand;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCommand;
import de.julielab.elastic.query.components.data.aggregation.MaxAggregation;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;

/**
 * Makes all settings required to query the search server for terms in one of the index fields as specified by an
 * instance of {@link FieldTermsCommand}.
 * 
 * @author faessler
 * 
 */
public class FieldTermsRetrievalPreparationComponent extends AbstractSearchComponent {

	public static final String AGG_FIELD_TERMS = "fieldTermsRetrieval";
	public static final String AGG_DOC_SCORE = "maxDocScore";

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FieldTermsRetrievalPreparation {
		//
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier semCarrier = (SemedicoSearchCarrier) searchCarrier;
		SemedicoSearchCommand searchCmd = semCarrier.searchCmd;
		if (null == searchCmd)
			throw new IllegalArgumentException(
					"The component " + FieldTermsRetrievalPreparationComponent.class.getSimpleName()
							+ " requires a semedico search command as input, but none was given.");
		FieldTermsCommand fieldTermsCmd = searchCmd.fieldTermsCmd;
		if (null == fieldTermsCmd)
			throw new IllegalArgumentException(
					"The component " + FieldTermsRetrievalPreparationComponent.class.getSimpleName()
							+ " requires a field terms command as input, but none was given.");

		// We will get the field terms by a TermsAggregation.
		TermsAggregation terms = new TermsAggregation();
		terms.field = fieldTermsCmd.field;
		terms.name = AGG_FIELD_TERMS;
		terms.size = fieldTermsCmd.size;

		for (int i = 0; i < fieldTermsCmd.orderTypes.length; ++i) {
			OrderCommand orderCmd = new OrderCommand();
			OrderType orderType = fieldTermsCmd.orderTypes[i];
			orderCmd.sortOrder = fieldTermsCmd.sortOrders[i];
			switch (orderType) {
			case COUNT:
				orderCmd.referenceType = OrderCommand.ReferenceType.COUNT;
				break;
			case DOC_SCORE:
				orderCmd.referenceType = OrderCommand.ReferenceType.AGGREGATION_SINGLE_VALUE;
				// The name of the aggregation that will hold the (maximum) document score for the terms to sort them by
				orderCmd.referenceName = AGG_DOC_SCORE;
				// now we also have to create the respective aggregation to get the document score in the first place
				MaxAggregation maxAgg = new MaxAggregation();
				maxAgg.name = AGG_DOC_SCORE;
				maxAgg.script = "_score";
				terms.addSubaggregation(maxAgg);
				break;
			case TERM:
				orderCmd.referenceType = OrderCommand.ReferenceType.TERM;
				break;
			}
			terms.addOrder(orderCmd);
		}

		SearchServerCommand serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();
		serverCmd.addAggregationCommand(terms);
		// We do not need the actual documents.
		serverCmd.rows = 0;

		return false;
	}

}
