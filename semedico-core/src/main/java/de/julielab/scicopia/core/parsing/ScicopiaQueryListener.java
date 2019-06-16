package de.julielab.scicopia.core.parsing;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import de.julielab.semedico.core.search.query.QueryToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.slf4j.Logger;

import com.google.common.collect.Multimap;
import com.google.common.collect.Range;

import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService.GeneralIndexStructure;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;

public class ScicopiaQueryListener extends ScicopiaBaseListener {

	private Deque<QueryToken> termMemory;
	private Deque<QueryFragment> partMemory;
	private Deque<QueryFragment> logicalMemory;
	private Deque<QueryFragment> blockMemory;
	private Deque<QueryFragment> phraseMemory;
	private QueryBuilder finalQuery;
	private List<QueryToken> specialTokens;
	private List<QueryToken> tokens;

	private DisambiguatingRangeChunker chunker;
	private IConceptService termService;
	private IStopWordService stopWordService;

	private final String[] metaFields = {GeneralIndexStructure.docmeta,
			GeneralIndexStructure.alltext, GeneralIndexStructure.mesh};

	private Logger logger;

	public ScicopiaQueryListener(List<QueryToken> specialTokens, List<QueryToken> tokens,
			DisambiguatingRangeChunker chunker, IConceptService termService, IStopWordService stopWordService, Logger log) {
		this.specialTokens = specialTokens;
		this.termMemory = new LinkedList<>();
		this.partMemory = new LinkedList<>();
		this.logicalMemory = new LinkedList<>();
		this.blockMemory = new LinkedList<>();
		this.phraseMemory = new LinkedList<>();
		this.tokens = tokens;
		this.tokens.clear();

		this.chunker = chunker;
		this.termService = termService;
		this.stopWordService = stopWordService;
		this.logger = log;
	}

	public QueryBuilder getFinalQuery() {
		return finalQuery;
	}

	private QueryToken recognize(String text) {
		chunker.match(text, chunker);
		Multimap<Range<Integer>, String> matches = chunker.getMatches();
		QueryToken token = new QueryToken(text);
		logger.debug("Number of initial chunks: {}", matches.size());
		for (Map.Entry<Range<Integer>, String> entry : matches.entries()) {
			Range<Integer> range = entry.getKey();
			int start = range.lowerEndpoint();
			int end = range.upperEndpoint();
			String termId = entry.getValue();
			logger.debug("Chunk {}, {}", termId, text.substring(start, end));

			IConcept term = termService.getTermSynchronously(termId);
			if (term == null) {
				logger.debug(
						"Dictionary matched the term {} with ID {}, but no such term could be found in the database or the database is down.",
						text.substring(start, end), termId);
				continue;
			}
			if (term.getFacets().isEmpty()) {
				logger.debug(
						"Concept with ID {} has no facets, possible because it belongs to an inactive facet. Skipping this term.", term.getId());
				continue;
			}

			token.addConceptToList(term);
		}

		List<IConcept> conceptList = token.getConceptList();
		if (conceptList.size() == 0) {
			token.setInputTokenType(TokenType.KEYWORD);
		} else if (conceptList.size() == 1) {
			token.setInputTokenType(TokenType.CONCEPT);
		} else {
			token.setInputTokenType(TokenType.AMBIGUOUS_CONCEPT);
		}
		return token;
	}

	@Override
	public void exitTerm(ScicopiaParser.TermContext ctx) {
		//term: DASH | NUM | COMPOUND | ALPHA | ABBREV | ALPHANUM | charged | APOSTROPHE ;
		String text = ctx.getText();
		QueryToken token = recognize(text);
		token.setMatchType(MultiMatchQueryBuilder.Type.BEST_FIELDS);
		token.setType(QueryToken.Category.ALPHA); // For ParsingService only
		termMemory.add(token);
	}

	@Override
	public void exitQuotes(ScicopiaParser.QuotesContext ctx) {
		//quotes: '"' .*? '"'
		//      |  '\'' .*? '\''
        //      ;
		String text = ctx.getText();
		text = text.substring(1, text.length()-1);
		QueryToken token = new QueryToken(text);
		token.setMatchType(MultiMatchQueryBuilder.Type.PHRASE);
		token.setType(QueryToken.Category.ALPHA); // For ParsingService only
		termMemory.add(token);

	}

	@Override
	public void exitPrefixed(ScicopiaParser.PrefixedContext ctx) {
		//prefixed: ( ALPHA )+ ':' ( quotes | term );
		String text = ctx.getText();
		int separatorIndex = text.indexOf(':');
		//TODO: Check, if it's a valid field
		String field = text.substring(0, separatorIndex);
		if (field.equals("author")) {
			field = GeneralIndexStructure.authors;
		}
		QueryToken token = termMemory.removeLast();
		if (field.equals(GeneralIndexStructure.authors)) {
			QueryBuilder query = QueryBuilders.matchQuery(field, token.getOriginalValue());
			partMemory.add(new QueryFragment(query, QueryPriority.MUST));
			token.setQuery(query);
			token.setPriority(QueryPriority.MUST);
			token.setOriginalValue(text);
			tokens.add(token);
			return;
		}

		if (token.getInputTokenType() == TokenType.KEYWORD) {
			QueryBuilder query = QueryBuilders.matchQuery(field, token.getOriginalValue());
			partMemory.add(new QueryFragment(query, QueryPriority.MUST));
			token.setQuery(query);
			token.setPriority(QueryPriority.MUST);
			tokens.add(token);
		} else if (token.getInputTokenType() == TokenType.CONCEPT) {
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			String conceptId = token.getConceptList().get(0).getId();
			boolQuery.should(QueryBuilders.matchQuery(field, token.getOriginalValue()));
			boolQuery.should(QueryBuilders.termQuery(field, conceptId));
			boolQuery.minimumShouldMatch(1);
			partMemory.add(new QueryFragment(boolQuery, QueryPriority.MUST));
			token.setQuery(boolQuery);
			token.setPriority(QueryPriority.MUST);
			tokens.add(token);
		} else { // TokenType.AMBIGUOUS_CONCEPT
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			boolQuery.should(QueryBuilders.matchQuery(field, token.getOriginalValue()));
			for (IConcept concept : token.getConceptList()) {
				String conceptId = concept.getId();
				boolQuery.should(QueryBuilders.termQuery(field, conceptId));
			}
			boolQuery.minimumShouldMatch(1);
			partMemory.add(new QueryFragment(boolQuery, QueryPriority.MUST));
			token.setQuery(boolQuery);
			token.setPriority(QueryPriority.MUST);
			tokens.add(token);
		}
		token.setOriginalValue(text);
	}

	@Override
	public void exitRelation(ScicopiaParser.RelationContext ctx) {
		//relation: quotes ( ARROW ( quotes | term ) )+
        //        | term ( ARROW ( quotes | term ) )+
        //        ;
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (int i = 0; i < 2; ++i) {
			QueryToken token = termMemory.removeLast();
			//TODO: Handle concepts
			String conceptId = token.getConceptList().get(0).getId();
			boolQuery.should(QueryBuilders.multiMatchQuery(token.getOriginalValue(), metaFields));
			boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.alltext, conceptId));
			boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.mesh, conceptId));
			token.setQuery(boolQuery);
			tokens.add(token);
		}
		boolQuery.minimumShouldMatch(1);
		partMemory.add(new QueryFragment(boolQuery, QueryPriority.MUST));
	}

	@Override
	public void exitPart(ScicopiaParser.PartContext ctx) {
		//part: quotes | relation | term | IRI | prefixed | SPECIAL ;
		TerminalNode special = ctx.SPECIAL();
		if (special != null) {
			String text = special.getText();
			text = text.substring(1, text.length()-1);
			int id = Integer.parseInt(text);
			QueryToken token = specialTokens.get(id);
			QueryBuilder query = token.getQuery();
			if (query != null) {
				if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
					blockMemory.add(new QueryFragment(query, token.getPriority()));
				} else { // Logical
					partMemory.add(new QueryFragment(query, token.getPriority()));
				}
			} else {
				if (token.getType() == QueryToken.Category.PREFIXED) {
					String queryText = token.getOriginalValue();
					int separatorIndex = queryText.indexOf(':');
					//TODO: Check, if it's a valid field
					String field = queryText.substring(0, separatorIndex);
					String value = queryText.substring(separatorIndex+1);
					if (field.equals(GeneralIndexStructure.authors)) {
						query = QueryBuilders.matchQuery(field, value);
						if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
							blockMemory.add(new QueryFragment(query, QueryPriority.MUST));
						} else { // Logical
							partMemory.add(new QueryFragment(query, QueryPriority.MUST));
						}
						token.setQuery(query);
						token.setPriority(QueryPriority.MUST);
					}
				} else {
					query = buildMultiMatchQuery(token);
					if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
						blockMemory.add(new QueryFragment(query));
					} else {
						partMemory.add(new QueryFragment(query));
					}
					token.setQuery(query);
				}
			}
			tokens.add(token);
			return;
		}
		if (!termMemory.isEmpty()) { // Common input
			QueryToken token = termMemory.removeLast();
			if (token.getInputTokenType() == TokenType.KEYWORD
					&& stopWordService.isStopWord(token.getOriginalValue())) {
				return;
			}
			QueryBuilder query;
			if (token.getInputTokenType() == TokenType.KEYWORD) {
				query = QueryBuilders.multiMatchQuery(token.getOriginalValue(), metaFields);
				partMemory.add(new QueryFragment(query, QueryPriority.MUST));
				token.setQuery(query);
				token.setPriority(QueryPriority.MUST);
				tokens.add(token);
			} else if (token.getInputTokenType() == TokenType.CONCEPT) {
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				String conceptId = token.getConceptList().get(0).getId();
				boolQuery.should(QueryBuilders.multiMatchQuery(token.getOriginalValue(), metaFields));
				boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.alltext, conceptId));
				boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.mesh, conceptId));
				boolQuery.minimumShouldMatch(1);
				query = boolQuery;
				token.setQuery(boolQuery);
				token.setPriority(QueryPriority.MUST);
				tokens.add(token);
			} else { // TokenType.AMBIGUOUS_CONCEPT
				BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
				boolQuery.should(QueryBuilders.multiMatchQuery(token.getOriginalValue(), metaFields));
				for (IConcept concept : token.getConceptList()) {
					String conceptId = concept.getId();
					boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.alltext, conceptId));
					boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.mesh, conceptId));
				}
				boolQuery.minimumShouldMatch(1);
				query = boolQuery;
				token.setQuery(boolQuery);
				token.setPriority(QueryPriority.MUST);
				tokens.add(token);
			}
			if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
				blockMemory.add(new QueryFragment(query, QueryPriority.MUST));
			} else { // Logical
				partMemory.add(new QueryFragment(query, QueryPriority.MUST));
			}
			return;
		}

		// Prefixed or Relation
		if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
			QueryFragment fragment = partMemory.removeLast();
			blockMemory.add(fragment);
		}
	}

	@Override
	public void exitLogical(ScicopiaParser.LogicalContext ctx) {
		int children = ctx.getChildCount();
		if (children == 2) { // Negation
			ParseTree operand = ctx.getChild(1);
			QueryFragment fragment;
			boolean isPart = false;
			if (operand.getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.LogicalContext.class)) {
				fragment = logicalMemory.removeLast();
			} else { // PartContext
				fragment = partMemory.removeLast();
				isPart = true;
			}

			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			boolQuery.mustNot(fragment.query);

			if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
				blockMemory.add(new QueryFragment(boolQuery, QueryPriority.MUSTNOT));
			} else { // Logical
				logicalMemory.add(new QueryFragment(boolQuery, QueryPriority.MUSTNOT));
			}

			if (isPart) {
				QueryToken token = tokens.get(tokens.size()-1);
				token.setPriority(QueryPriority.MUSTNOT);
				token.setOriginalValue("NOT " + token.getOriginalValue());
				token.setQuery(boolQuery);
//				QueryBuilder query = token.getQuery();
//				if

//				QueryToken notToken = new QueryToken("NOT");
//				notToken.setInputTokenType(TokenType.NOT);
//				notToken.setType(Category.NOT);
//				tokens.add(tokens.size()-1, notToken);
			}
		} else { // AND/OR
			ParseTree operand1 = ctx.getChild(2);
			QueryFragment fragment1;
			if (operand1.getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.LogicalContext.class)) {
				fragment1 = logicalMemory.removeLast();
			} else { // PartContext
				fragment1 = partMemory.removeLast();
			}

			ParseTree operand2 = ctx.getChild(0);
			QueryFragment fragment2;
			if (operand2.getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.LogicalContext.class)) {
				fragment2 = logicalMemory.removeLast();
			} else { // PartContext
				fragment2 = partMemory.removeLast();
			}

			TerminalNode operatorNode = (TerminalNode) ctx.getChild(1);
			String operator = operatorNode.getSymbol().getText();
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			QueryFragment boolFragment;
			if (operator.equalsIgnoreCase("and")) {
				boolQuery.must(fragment1.query);
				boolQuery.must(fragment2.query);
				boolFragment = new QueryFragment(boolQuery, QueryPriority.MUST);
			} else { // "OR"
				boolQuery.should(fragment1.query);
				boolQuery.should(fragment2.query);
				boolFragment = new QueryFragment(boolQuery, QueryPriority.SHOULD);
			}

			if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.BlockContext.class)) {
				blockMemory.add(boolFragment);
			} else { // Logical
				logicalMemory.add(boolFragment);
			}
		}
	}

	@Override
	public void exitBlock(ScicopiaParser.BlockContext ctx) {
		//block: ( part ) +
	    //     | ( logical ) +
	    //     | ( part ) + ( AND | OR ) LPAR block+ RPAR
	    //     | ( logical ) + ( AND | OR ) LPAR block+ RPAR
	    //     | LPAR block+ RPAR ( AND | OR ) ( part ) +
	    //     | LPAR block+ RPAR ( AND | OR ) ( logical ) +
	    //     | LPAR block+ RPAR ( AND | OR ) block+
	    //     | LPAR block+ RPAR
	    //     ;

		if (ctx.getParent().getClass().equals(de.julielab.scicopia.core.parsing.ScicopiaParser.PhraseContext.class)) {
			phraseMemory.addAll(blockMemory);
			blockMemory.clear();
		}
	}

	private QueryBuilder buildMultiMatchQuery(QueryToken token) {
		String value = token.getOriginalValue();
		if (token.getInputTokenType() == TokenType.KEYWORD) {
			return QueryBuilders.multiMatchQuery(value, metaFields);
		} else if (token.getInputTokenType() == TokenType.CONCEPT) {
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			List<IConcept> concepts = token.getConceptList();
			String conceptId = concepts.get(0).getId();
			boolQuery.should(QueryBuilders.multiMatchQuery(value, metaFields));
			boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.alltext, conceptId));
			boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.mesh, conceptId));
			boolQuery.minimumShouldMatch(1);
			return boolQuery;
		} else { // TokenType.AMBIGUOUS_CONCEPT
			BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
			boolQuery.should(QueryBuilders.multiMatchQuery(value, metaFields));
			for (IConcept concept : token.getConceptList()) {
				String conceptId = concept.getId();
				boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.alltext, conceptId));
				boolQuery.should(QueryBuilders.termQuery(GeneralIndexStructure.mesh, conceptId));
			}
			boolQuery.minimumShouldMatch(1);
			return boolQuery;
		}
	}
	
	@Override
	public void exitPhrase(ScicopiaParser.PhraseContext ctx) {
		if (phraseMemory.size() == 1) {
			finalQuery = phraseMemory.pollFirst().query;
			return;
		}
		
		BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
		for (QueryFragment fragment : phraseMemory) {
			QueryBuilder query = fragment.query;
			if (fragment.priority == QueryPriority.MUST) {
				boolQuery.must(query);
			} else if (fragment.priority == QueryPriority.MUSTNOT) {
				if (!query.getName().equals("wrapper")) {
					BoolQueryBuilder temp = (BoolQueryBuilder) query;
					boolQuery.mustNot(temp.mustNot().get(0));
				} else {
					boolQuery.mustNot(query);
				}
				
			} else {
				boolQuery.should(query);
			}

		}
		finalQuery = boolQuery;
		blockMemory.clear();
	}

}
