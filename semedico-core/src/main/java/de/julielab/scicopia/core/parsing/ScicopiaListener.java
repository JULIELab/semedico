// Generated from /Users/faessler/Coding/git/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
package de.julielab.scicopia.core.parsing;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ScicopiaParser}.
 */
public interface ScicopiaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#question}.
	 * @param ctx the parse tree
	 */
	void enterQuestion(ScicopiaParser.QuestionContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#question}.
	 * @param ctx the parse tree
	 */
	void exitQuestion(ScicopiaParser.QuestionContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#line}.
	 * @param ctx the parse tree
	 */
	void enterLine(ScicopiaParser.LineContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#line}.
	 * @param ctx the parse tree
	 */
	void exitLine(ScicopiaParser.LineContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterQuery(ScicopiaParser.QueryContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitQuery(ScicopiaParser.QueryContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#bool}.
	 * @param ctx the parse tree
	 */
	void enterBool(ScicopiaParser.BoolContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#bool}.
	 * @param ctx the parse tree
	 */
	void exitBool(ScicopiaParser.BoolContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#negation}.
	 * @param ctx the parse tree
	 */
	void enterNegation(ScicopiaParser.NegationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#negation}.
	 * @param ctx the parse tree
	 */
	void exitNegation(ScicopiaParser.NegationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#token}.
	 * @param ctx the parse tree
	 */
	void enterToken(ScicopiaParser.TokenContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#token}.
	 * @param ctx the parse tree
	 */
	void exitToken(ScicopiaParser.TokenContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#quotes}.
	 * @param ctx the parse tree
	 */
	void enterQuotes(ScicopiaParser.QuotesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#quotes}.
	 * @param ctx the parse tree
	 */
	void exitQuotes(ScicopiaParser.QuotesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#doublequotes}.
	 * @param ctx the parse tree
	 */
	void enterDoublequotes(ScicopiaParser.DoublequotesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#doublequotes}.
	 * @param ctx the parse tree
	 */
	void exitDoublequotes(ScicopiaParser.DoublequotesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#singlequotes}.
	 * @param ctx the parse tree
	 */
	void enterSinglequotes(ScicopiaParser.SinglequotesContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#singlequotes}.
	 * @param ctx the parse tree
	 */
	void exitSinglequotes(ScicopiaParser.SinglequotesContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#prefixed}.
	 * @param ctx the parse tree
	 */
	void enterPrefixed(ScicopiaParser.PrefixedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#prefixed}.
	 * @param ctx the parse tree
	 */
	void exitPrefixed(ScicopiaParser.PrefixedContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#relation}.
	 * @param ctx the parse tree
	 */
	void enterRelation(ScicopiaParser.RelationContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#relation}.
	 * @param ctx the parse tree
	 */
	void exitRelation(ScicopiaParser.RelationContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#term}.
	 * @param ctx the parse tree
	 */
	void enterTerm(ScicopiaParser.TermContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#term}.
	 * @param ctx the parse tree
	 */
	void exitTerm(ScicopiaParser.TermContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#charged}.
	 * @param ctx the parse tree
	 */
	void enterCharged(ScicopiaParser.ChargedContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#charged}.
	 * @param ctx the parse tree
	 */
	void exitCharged(ScicopiaParser.ChargedContext ctx);
}