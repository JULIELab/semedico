package de.julielab.lucene;


/**
 * This class supervises the parsing process and
 * logs all occurring errors.
 * 
 * @author hellrich
 *
 */
public class ParseErrors {
	private int leftParentheses = 0;
	private int rightParentheses = 0;
	private int ignoredANDs = 0;
	private int ignoredORs = 0;
	
	/**
	 * Increases the counter for left parentheses.
	 */
	void incLeftPar(){
		leftParentheses += 1;
	}
	
	/**
	 * Increases the counter for right parentheses.
	 */
	void incRightPar(){
		++rightParentheses;
	}
	
	/**
	 * Increases the counter for ignored ANDs.
	 */
	void incIgnoredANDs(){
		++ignoredANDs;
	}
	
	/**
	 * Increases the counter for ignored ORs.
	 */
	void incIgnoredORs(){
		++ignoredORs;
	}
	
	/**
	 * @return True if there are unmatched parentheses.
	 */
	boolean hasUnmatchedParentheses(){
		return leftParentheses != rightParentheses;
	}	
	
	/**
	 * @return True if ANDs or ORs were ignored.
	 */
	boolean hasIgnoredOperator(){
		return ignoredANDs > 0 || ignoredORs > 0;
	}
	
	/**
	 * @return Number of unmatched left parentheses.
	 */
	int getUnmatchedLeftPar(){
		return leftParentheses - rightParentheses;
	}
	
	/**
	 * @return Number of unmatched right parentheses.
	 */
	int getUnmatchedRightPar(){
		return rightParentheses - leftParentheses;
	}

	/**
	 * @return Number of ignored ANDs.
	 */
	int getIgnoredANDs(){
		return ignoredANDs;
	}
	
	/**
	 * @return Number of ignored ORs.
	 */
	int getIgnoredORs(){
		return ignoredORs;
	}
	
	/**
	 * @return True if any error occured during parsing.
	 */
	boolean hasParsingErrors(){
		return hasUnmatchedParentheses() || hasIgnoredOperator();
	}
}
