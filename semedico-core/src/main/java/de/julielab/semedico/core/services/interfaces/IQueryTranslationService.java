package de.julielab.semedico.core.services.interfaces;

import java.util.Collection;

import de.julielab.elastic.query.components.data.query.SearchServerQuery;
import de.julielab.semedico.core.boosting.IBooster;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.concepts.CoreConcept;
import de.julielab.semedico.core.parsing.ParseTree;
@Deprecated
public interface IQueryTranslationService {

	/**
	 * Transform the disambiguated user query into a corresponding query String
	 * which can be sent to the used search server, e.g. Solr.
	 * 
	 * @param parseTree
	 *            The disambiguated user query in form of the parse tree.
	 * @param searchFields
	 *            The index fields to search in.
	 * @return The index specific query String.
	 */
	public SearchServerQuery createQuery(ParseTree parseTree, Collection<String> searchFields,
			IBooster booster);

//	public SearchServerQuery createQueryForSearchNode(List<ParseTree> searchNodes,
//			int targetSNIndex, Collection<String> searchFields, IBooster booster);
//
//	public SearchServerQuery createQueryForBTermSearchNode(List<ParseTree> searchNodes,
//			IConcept bTerm, int targetSNIndex, Collection<String> searchFields,
//			IBooster booster);
	
	/**
	 * Given an event type (i.e. a concept for 'regulation', 'transcription' or another event type), creates an index term (not a Semedico term/concept!) matching event 
	 * @param eventType
	 * @return
	 */
	@Deprecated
	public String createSearchTermForEventType(IConcept eventType);

	SearchServerQuery createSearchTermForCoreTerm(CoreConcept term);

}
