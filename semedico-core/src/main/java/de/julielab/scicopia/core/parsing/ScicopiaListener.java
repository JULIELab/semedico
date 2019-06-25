package de.julielab.scicopia.core.parsing;// Generated from /Users/faessler/Coding/git/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
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
	 * Enter a parse tree produced by the {@code neg}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterNeg(ScicopiaParser.NegContext ctx);
	/**
	 * Exit a parse tree produced by the {@code neg}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitNeg(ScicopiaParser.NegContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tokenSequence}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterTokenSequence(ScicopiaParser.TokenSequenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tokenSequence}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitTokenSequence(ScicopiaParser.TokenSequenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code parQuery}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterParQuery(ScicopiaParser.ParQueryContext ctx);
	/**
	 * Exit a parse tree produced by the {@code parQuery}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitParQuery(ScicopiaParser.ParQueryContext ctx);
	/**
	 * Enter a parse tree produced by the {@code leadingNeg}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterLeadingNeg(ScicopiaParser.LeadingNegContext ctx);
	/**
	 * Exit a parse tree produced by the {@code leadingNeg}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitLeadingNeg(ScicopiaParser.LeadingNegContext ctx);
	/**
	 * Enter a parse tree produced by the {@code trailingNeg}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterTrailingNeg(ScicopiaParser.TrailingNegContext ctx);
	/**
	 * Exit a parse tree produced by the {@code trailingNeg}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitTrailingNeg(ScicopiaParser.TrailingNegContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryBoolean}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void enterBinaryBoolean(ScicopiaParser.BinaryBooleanContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryBoolean}
	 * labeled alternative in {@link ScicopiaParser#query}.
	 * @param ctx the parse tree
	 */
	void exitBinaryBoolean(ScicopiaParser.BinaryBooleanContext ctx);
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