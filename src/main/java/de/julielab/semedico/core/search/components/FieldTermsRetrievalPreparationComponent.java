package de.julielab.semedico.core.search.components;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Supplier;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.AbstractSearchComponent;
import de.julielab.elastic.query.components.data.FieldTermsCommand;
import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.components.data.SearchServerRequest;
import de.julielab.elastic.query.components.data.aggregation.AggregationRequest.OrderCommand;
import de.julielab.elastic.query.components.data.aggregation.MaxAggregation;
import de.julielab.elastic.query.components.data.aggregation.TermsAggregation;
import de.julielab.semedico.core.search.components.data.SemedicoSearchCarrier;
import de.julielab.semedico.core.search.query.FieldTermsQuery;
import de.julielab.semedico.core.search.query.FieldTermsQuery.OrderType;
import de.julielab.semedico.core.search.results.SemedicoSearchResult;

/**
 * Makes all settings required to query the search server for terms in one of
 * the index fields as specified by an instance of {@link FieldTermsCommand}.
 * 
 * @author faessler
 * 
 */
public class FieldTermsRetrievalPreparationComponent extends AbstractSearchComponent {

	public FieldTermsRetrievalPreparationComponent(Logger log) {
		super(log);
	}

	public static final String AGG_FIELD_TERMS = "fieldTermsRetrieval";
	public static final String AGG_DOC_SCORE = "maxDocScore";

	@Retention(RetentionPolicy.RUNTIME)
	public @interface FieldTermsRetrievalPreparation {
		//
	}

	@Override
	protected boolean processSearch(SearchCarrier searchCarrier) {
		SemedicoSearchCarrier<FieldTermsQuery, SemedicoSearchResult> semCarrier = castCarrier(searchCarrier);
		Supplier<FieldTermsQuery> q = () -> semCarrier.query;
		Supplier<String> f = () -> q.get().getField();
		checkNotNull(q, "Search query", f, "Field name to retrieve terms from");
		stopIfError();

		FieldTermsQuery query = q.get();
		// We will get the field terms by a TermsAggregation.
		TermsAggregation terms = new TermsAggregation();
		terms.field = f.get();
		terms.name = AGG_FIELD_TERMS;
		terms.size = query.getSize();

		OrderCommand orderCmd = new OrderCommand();
		OrderType orderType = query.orderType;
		orderCmd.sortOrder = query.sortOrder;
		switch (orderType) {
		case COUNT:
			orderCmd.referenceType = OrderCommand.ReferenceType.COUNT;
			break;
		case DOC_SCORE:
			orderCmd.referenceType = OrderCommand.ReferenceType.AGGREGATION_SINGLE_VALUE;
			// The name of the aggregation that will hold the (maximum) document
			// score for the terms to sort them by
			orderCmd.referenceName = AGG_DOC_SCORE;
			// now we also have to create the respective aggregation to get the
			// document score in the first place
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

		SearchServerRequest serverCmd = semCarrier.getSingleSearchServerCommandOrCreate();
		serverCmd.addAggregationCommand(terms);
		// We do not need the actual documents.
		serverCmd.rows = 0;

		return false;
	}

}
