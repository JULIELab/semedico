package de.julielab.scicopia.core.parsing;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.roklenarcic.util.strings.LongestMatchSet;
import com.roklenarcic.util.strings.SetMatchListener;
import com.roklenarcic.util.strings.threshold.RangeNodeThreshold;
import org.apache.commons.lang3.Range;

import java.util.Collection;

public class DisambiguatingRangeChunker implements SetMatchListener {

	private Multimap<String, String> dictionary;
	private LongestMatchSet chunker;
	private Multimap<Range<Integer>, String> matchingTokens;
	
	private Range<Integer> needleSpan;
	private String needle;
	
	public DisambiguatingRangeChunker(Multimap<String, String> dictionary) {
		this.dictionary = dictionary;
		this.chunker = new LongestMatchSet(dictionary.keys(), false, new RangeNodeThreshold(0.3));
    	matchingTokens = MultimapBuilder.hashKeys(5).arrayListValues(10).build();
	}

	public void match(String query) {
		chunker.match(query, this);
	}

	public void match(String query, SetMatchListener listener) {
		chunker.match(query, listener);
	}

    public boolean match(String haystack, final int startPosition, final int endPosition) {
    	if (endPosition == haystack.length() || Character.isWhitespace(haystack.charAt(endPosition))) {
        	needle = haystack.substring(startPosition, endPosition);
        	needleSpan = Range.between(startPosition, endPosition);
        	Collection<String> matches = dictionary.get(needle);
        	for (String match : matches) {
        		matchingTokens.put(needleSpan, match);
        	}
    	}
    	return true;
    }
    
    public Multimap<Range<Integer>, String> getMatches() {    	
    	return matchingTokens;
    }
    
    public void reset() {
    	matchingTokens.clear();
    }
}
