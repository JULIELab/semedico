package de.julielab.scicopia.core.elasticsearch;

import com.google.common.collect.Multimap;
import de.julielab.scicopia.core.parsing.DisambiguatingRangeChunker;
import de.julielab.scicopia.core.parsing.ScicopiaLexer;
import de.julielab.scicopia.core.parsing.ScicopiaParser;
import de.julielab.scicopia.core.parsing.ScicopiaQueryListener;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.search.query.QueryToken;
import de.julielab.semedico.core.services.interfaces.IConceptService;
import de.julielab.semedico.core.services.interfaces.IStopWordService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService;
import de.julielab.semedico.core.services.interfaces.ITokenInputService.TokenType;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.lang3.Range;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.slf4j.Logger;

import java.util.*;

public class ElasticsearchQueryBuilder implements IElasticsearchQueryBuilder {

	private IStopWordService stopWordService;
	private Logger log;
	private DisambiguatingRangeChunker chunker;
	private IConceptService termService;
	
	private Set<ITokenInputService.TokenType> preanalyzed;

	public ElasticsearchQueryBuilder(Logger log, IStopWordService stopWordService, DisambiguatingRangeChunker chunker,
			IConceptService termService) {
		this.log = log;
		this.stopWordService = stopWordService;
		this.preanalyzed = new TreeSet<>();
		preanalyzed.add(TokenType.AMBIGUOUS_CONCEPT);
		preanalyzed.add(TokenType.CONCEPT);
		preanalyzed.add(TokenType.KEYWORD);
		this.chunker = chunker;
		this.termService = termService;
	}

	@Override
	public QueryBuilder analyseQueryString(List<QueryToken> tokens) {
		try {
			List<QueryToken> specialTokens = new ArrayList<>();
			log.debug("Got user query tokens: {}", tokens);
			int special = 0;
			StringBuilder builder = new StringBuilder();
			if (!tokens.isEmpty()) {
				for (QueryToken userToken : tokens) {
					if (preanalyzed.contains(userToken.getInputTokenType())) {
						specialTokens.add(userToken);
						builder.append("⌨" + special + "⌨ ");
						special++;
					} else {
						builder.append(userToken.getOriginalValue() + " ");
					}
				}
				builder.setLength(builder.length()-1);
				String queryString = builder.toString();
				queryString = recognize(queryString, special, specialTokens);
				chunker.reset();
				CodePointCharStream stream = CharStreams.fromString(queryString);
				ScicopiaLexer lexer = new ScicopiaLexer(stream);
				CommonTokenStream tokenstream = new CommonTokenStream(lexer);
				ScicopiaParser parser = new ScicopiaParser(tokenstream);
				ParseTree tree = parser.query();
				ParseTreeWalker walker = new ParseTreeWalker();
				ScicopiaQueryListener listener = new ScicopiaQueryListener(specialTokens, tokens, chunker, termService, stopWordService, log);
				walker.walk(listener, tree);
				
				return listener.getFinalQuery();
			 }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private String recognize(String text, int special, List<QueryToken> specialTokens) {
		chunker.match(text, chunker);
		Multimap<Range<Integer>, String> matches = chunker.getMatches();
		log.debug("Number of initial chunks: {}", matches.size());
		SortedSet<Range<Integer>> matchSet = new TreeSet<>(new EndpointComparator());
		matchSet.addAll(matches.keySet());
		for (Range<Integer> key : matchSet) {
			int start = key.getMinimum();
			int end = key.getMaximum();
			QueryToken token = new QueryToken(start, end, text.substring(start, end));
			Collection<String> termIds = matches.get(key);
			for (String termId : termIds) {
				log.debug("Chunk {}, {}", termId, text.substring(start, end));
				IConcept term = termService.getTermSynchronously(termId);
				if (term == null) {
					log.debug(
							"Dictionary matched the term {} with ID {}, but no such term could be found in the database or the database is down.",
							text.substring(start, end), termId);
					continue;
				}
				if (term.getFacets().isEmpty()) {
					log.debug(
							"Term with ID {} has no facets, possible because it belongs to an inactive facet. Skipping this term.", term.getId());
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
			token.setMatchType(MultiMatchQueryBuilder.Type.BEST_FIELDS);
			token.setType(QueryToken.Category.ALPHA); // For ParsingService only
			specialTokens.add(token);
			
			// Since the ranges are sorted in descending fashion,
			// changing the source string won't cause any conflicts
			if (start == 0) {
				if (end == text.length()) {
					return "⌨" + special + "⌨";
				} else {
					text = "⌨" + special + "⌨ " + text.substring(end);
				}
			} else {
				if (end == text.length()-1) {
					text = text.substring(0, start) + " ⌨" + special + "⌨";
				} else {
					text = text.substring(0, start) + " ⌨" + special + "⌨ " + text.substring(end);
				}
			}
		}
		return text;
	}

	private static class EndpointComparator implements Comparator<Range<Integer>> {
		@Override
		public int compare(Range<Integer> range1, Range<Integer> range2) {
			int start = range2.getMinimum() - range1.getMinimum();
			if (start == 0) {
				return range2.getMaximum() - range1.getMaximum();
			}
			return start;
		}
	}
}
