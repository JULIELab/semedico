package de.julielab.scicopia.core.parsing;// Generated from /Users/faessler/tmp/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link ScicopiaParser}.
 */
public interface ScicopiaListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#phrase}.
	 * @param ctx the parse tree
	 */
	void enterPhrase(ScicopiaParser.PhraseContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#phrase}.
	 * @param ctx the parse tree
	 */
	void exitPhrase(ScicopiaParser.PhraseContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(ScicopiaParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(ScicopiaParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#part}.
	 * @param ctx the parse tree
	 */
	void enterPart(ScicopiaParser.PartContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#part}.
	 * @param ctx the parse tree
	 */
	void exitPart(ScicopiaParser.PartContext ctx);
	/**
	 * Enter a parse tree produced by {@link ScicopiaParser#logical}.
	 * @param ctx the parse tree
	 */
	void enterLogical(ScicopiaParser.LogicalContext ctx);
	/**
	 * Exit a parse tree produced by {@link ScicopiaParser#logical}.
	 * @param ctx the parse tree
	 */
	void exitLogical(ScicopiaParser.LogicalContext ctx);
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