package de.julielab.scicopia.core.parsing;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Range;
import com.roklenarcic.util.strings.AhoCorasickSet;
import com.roklenarcic.util.strings.SetMatchListener;
import com.roklenarcic.util.strings.threshold.RangeNodeThreshold;
import de.julielab.semedico.core.search.query.QueryToken;

import java.util.Collection;


public class DisambiguatingChunker implements SetMatchListener {

	private Multimap<String, String> dictionary;
	private AhoCorasickSet chunker;
	
	private Range<Integer> needleSpan;
	private String needle;
	
	public DisambiguatingChunker(Multimap<String, String> dictionary) {
		this.dictionary = dictionary;
		this.chunker = new AhoCorasickSet(dictionary.keys(), false, new RangeNodeThreshold(0.3));
	}

	public void match(String query, SetMatchListener listener) {
		chunker.match(query, listener);
	}

    public boolean match(String haystack, final int startPosition, final int endPosition) {
    	if (endPosition == haystack.length() || Character.isWhitespace(haystack.charAt(endPosition))) {
        	needle = haystack.substring(startPosition, endPosition);
        	needleSpan = Range.closed(startPosition, endPosition);
    	}
    	return true;
    }
    
    public Multimap<QueryToken, String> getMatches() {
    	Collection<String> matches = dictionary.get(needle);
    	Multimap<QueryToken, String> matchingTokens = MultimapBuilder.hashKeys(1).arrayListValues(matches.size()).build();
    	for (String match : matches) {
    		matchingTokens.put(new QueryToken(needleSpan.lowerEndpoint(), needleSpan.upperEndpoint(), needle), match);
    	}
    	return matchingTokens;
    }
}
