package de.julielab.scicopia.core.parsing;// Generated from /Users/faessler/Coding/git/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ScicopiaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, ARROW=6, ARROWRIGHT=7, ARROWLEFT=8, 
		ARROWBOTH=9, IRI=10, HASHTAG=11, DASH=12, NUM=13, COMPOUND=14, APOSTROPHE=15, 
		AND=16, NOT=17, OR=18, DIGITS=19, ALPHA=20, ABBREV=21, ALPHANUM=22, LPAR=23, 
		RPAR=24, SPECIAL=25, NL=26, WHITESPACE=27;
	public static final int
		RULE_question = 0, RULE_line = 1, RULE_query = 2, RULE_negation = 3, RULE_token = 4, 
		RULE_quotes = 5, RULE_prefixed = 6, RULE_relation = 7, RULE_term = 8, 
		RULE_charged = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"question", "line", "query", "negation", "token", "quotes", "prefixed", 
			"relation", "term", "charged"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\"'", "'''", "':'", "'+'", "'-'", null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			"'('", "')'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "ARROW", "ARROWRIGHT", "ARROWLEFT", 
			"ARROWBOTH", "IRI", "HASHTAG", "DASH", "NUM", "COMPOUND", "APOSTROPHE", 
			"AND", "NOT", "OR", "DIGITS", "ALPHA", "ABBREV", "ALPHANUM", "LPAR", 
			"RPAR", "SPECIAL", "NL", "WHITESPACE"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "Scicopia.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public ScicopiaParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class QuestionContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(ScicopiaParser.EOF, 0); }
		public List<LineContext> line() {
			return getRuleContexts(LineContext.class);
		}
		public LineContext line(int i) {
			return getRuleContext(LineContext.class,i);
		}
		public QuestionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_question; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterQuestion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitQuestion(this);
		}
	}

	public final QuestionContext question() throws RecognitionException {
		QuestionContext _localctx = new QuestionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_question);
		System.out.println("Question last update 1213");
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(21); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(20);
					line();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(23); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,0,_ctx);
			} while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER );
			setState(25);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LineContext extends ParserRuleContext {
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode NL() { return getToken(ScicopiaParser.NL, 0); }
		public LineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitLine(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		LineContext _localctx = new LineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_line);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(27);
			query(0);
			setState(28);
			match(NL);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QueryContext extends ParserRuleContext {
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
	 
		public QueryContext() { }
		public void copyFrom(QueryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class NegContext extends QueryContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public NegContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterNeg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitNeg(this);
		}
	}
	public static class TokenSequenceContext extends QueryContext {
		public List<TokenContext> token() {
			return getRuleContexts(TokenContext.class);
		}
		public TokenContext token(int i) {
			return getRuleContext(TokenContext.class,i);
		}
		public TokenSequenceContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterTokenSequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitTokenSequence(this);
		}
	}
	public static class ParQueryContext extends QueryContext {
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public ParQueryContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterParQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitParQuery(this);
		}
	}
	public static class LeadingNegContext extends QueryContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public LeadingNegContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterLeadingNeg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitLeadingNeg(this);
		}
	}
	public static class TrailingNegContext extends QueryContext {
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public TrailingNegContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterTrailingNeg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitTrailingNeg(this);
		}
	}
	public static class BinaryBooleanContext extends QueryContext {
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public TerminalNode AND() { return getToken(ScicopiaParser.AND, 0); }
		public TerminalNode OR() { return getToken(ScicopiaParser.OR, 0); }
		public BinaryBooleanContext(QueryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterBinaryBoolean(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitBinaryBoolean(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		return query(0);
	}

	private QueryContext query(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		QueryContext _localctx = new QueryContext(_ctx, _parentState);
		QueryContext _prevctx = _localctx;
		int _startState = 4;
		enterRecursionRule(_localctx, 4, RULE_query, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				_localctx = new NegContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(31);
				negation();
				}
				break;
			case 2:
				{
				_localctx = new LeadingNegContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(32);
				negation();
				setState(33);
				query(6);
				}
				break;
			case 3:
				{
				_localctx = new ParQueryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(35);
				match(LPAR);
				setState(36);
				query(0);
				setState(37);
				match(RPAR);
				}
				break;
			case 4:
				{
				_localctx = new TokenSequenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(42);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(39);
						token();
						}
						} 
					}
					setState(44);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(59);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(57);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						_localctx = new TrailingNegContext(new QueryContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(47);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(48);
						negation();
						setState(49);
						query(6);
						}
						break;
					case 2:
						{
						_localctx = new BinaryBooleanContext(new QueryContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(51);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(52);
						match(AND);
						setState(53);
						query(5);
						}
						break;
					case 3:
						{
						_localctx = new BinaryBooleanContext(new QueryContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(54);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(55);
						match(OR);
						setState(56);
						query(4);
						}
						break;
					}
					} 
				}
				setState(61);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class NegationContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(ScicopiaParser.NOT, 0); }
		public TokenContext token() {
			return getRuleContext(TokenContext.class,0);
		}
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterNegation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitNegation(this);
		}
	}

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_negation);
		try {
			setState(71);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(62);
				match(NOT);
				setState(63);
				token();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(64);
				match(NOT);
				setState(65);
				match(LPAR);
				setState(66);
				query(0);
				setState(67);
				match(RPAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(69);
				match(NOT);
				setState(70);
				negation();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TokenContext extends ParserRuleContext {
		public QuotesContext quotes() {
			return getRuleContext(QuotesContext.class,0);
		}
		public RelationContext relation() {
			return getRuleContext(RelationContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public TerminalNode IRI() { return getToken(ScicopiaParser.IRI, 0); }
		public PrefixedContext prefixed() {
			return getRuleContext(PrefixedContext.class,0);
		}
		public TerminalNode SPECIAL() { return getToken(ScicopiaParser.SPECIAL, 0); }
		public TokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_token; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitToken(this);
		}
	}

	public final TokenContext token() throws RecognitionException {
		TokenContext _localctx = new TokenContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_token);
		try {
			setState(79);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(73);
				quotes();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(74);
				relation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(75);
				term();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(76);
				match(IRI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(77);
				prefixed();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(78);
				match(SPECIAL);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class QuotesContext extends ParserRuleContext {
		public QuotesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_quotes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterQuotes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitQuotes(this);
		}
	}

	public final QuotesContext quotes() throws RecognitionException {
		QuotesContext _localctx = new QuotesContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_quotes);
		try {
			int _alt;
			setState(97);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(81);
				match(T__0);
				setState(85);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				while ( _alt!=1 && _alt!=ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(82);
						matchWildcard();
						}
						} 
					}
					setState(87);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				}
				setState(88);
				match(T__0);
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(89);
				match(T__1);
				setState(93);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				while ( _alt!=1 && _alt!=ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(90);
						matchWildcard();
						}
						} 
					}
					setState(95);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				}
				setState(96);
				match(T__1);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class PrefixedContext extends ParserRuleContext {
		public TerminalNode SPECIAL() { return getToken(ScicopiaParser.SPECIAL, 0); }
		public QuotesContext quotes() {
			return getRuleContext(QuotesContext.class,0);
		}
		public TermContext term() {
			return getRuleContext(TermContext.class,0);
		}
		public List<TerminalNode> ALPHA() { return getTokens(ScicopiaParser.ALPHA); }
		public TerminalNode ALPHA(int i) {
			return getToken(ScicopiaParser.ALPHA, i);
		}
		public PrefixedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_prefixed; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterPrefixed(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitPrefixed(this);
		}
	}

	public final PrefixedContext prefixed() throws RecognitionException {
		PrefixedContext _localctx = new PrefixedContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_prefixed);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(100); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(99);
				match(ALPHA);
				}
				}
				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHA );
			setState(104);
			match(T__2);
			setState(108);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPECIAL:
				{
				setState(105);
				match(SPECIAL);
				}
				break;
			case T__0:
			case T__1:
				{
				setState(106);
				quotes();
				}
				break;
			case DASH:
			case NUM:
			case COMPOUND:
			case APOSTROPHE:
			case ALPHA:
			case ABBREV:
			case ALPHANUM:
				{
				setState(107);
				term();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RelationContext extends ParserRuleContext {
		public List<QuotesContext> quotes() {
			return getRuleContexts(QuotesContext.class);
		}
		public QuotesContext quotes(int i) {
			return getRuleContext(QuotesContext.class,i);
		}
		public List<TerminalNode> ARROW() { return getTokens(ScicopiaParser.ARROW); }
		public TerminalNode ARROW(int i) {
			return getToken(ScicopiaParser.ARROW, i);
		}
		public List<TerminalNode> SPECIAL() { return getTokens(ScicopiaParser.SPECIAL); }
		public TerminalNode SPECIAL(int i) {
			return getToken(ScicopiaParser.SPECIAL, i);
		}
		public List<TermContext> term() {
			return getRuleContexts(TermContext.class);
		}
		public TermContext term(int i) {
			return getRuleContext(TermContext.class,i);
		}
		public RelationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_relation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitRelation(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_relation);
		try {
			int _alt;
			setState(143);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				quotes();
				setState(117); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(111);
						match(ARROW);
						setState(115);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(112);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(113);
							quotes();
							}
							break;
						case DASH:
						case NUM:
						case COMPOUND:
						case APOSTROPHE:
						case ALPHA:
						case ABBREV:
						case ALPHANUM:
							{
							setState(114);
							term();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(119); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
				} while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER );
				}
				break;
			case DASH:
			case NUM:
			case COMPOUND:
			case APOSTROPHE:
			case ALPHA:
			case ABBREV:
			case ALPHANUM:
				enterOuterAlt(_localctx, 2);
				{
				setState(121);
				term();
				setState(128); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(122);
						match(ARROW);
						setState(126);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(123);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(124);
							quotes();
							}
							break;
						case DASH:
						case NUM:
						case COMPOUND:
						case APOSTROPHE:
						case ALPHA:
						case ABBREV:
						case ALPHANUM:
							{
							setState(125);
							term();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(130); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
				} while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER );
				}
				break;
			case SPECIAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(132);
				match(SPECIAL);
				setState(139); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(133);
						match(ARROW);
						setState(137);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(134);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(135);
							quotes();
							}
							break;
						case DASH:
						case NUM:
						case COMPOUND:
						case APOSTROPHE:
						case ALPHA:
						case ABBREV:
						case ALPHANUM:
							{
							setState(136);
							term();
							}
							break;
						default:
							throw new NoViableAltException(this);
						}
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(141); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,17,_ctx);
				} while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER );
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TermContext extends ParserRuleContext {
		public TerminalNode DASH() { return getToken(ScicopiaParser.DASH, 0); }
		public TerminalNode NUM() { return getToken(ScicopiaParser.NUM, 0); }
		public TerminalNode COMPOUND() { return getToken(ScicopiaParser.COMPOUND, 0); }
		public TerminalNode ALPHA() { return getToken(ScicopiaParser.ALPHA, 0); }
		public TerminalNode ABBREV() { return getToken(ScicopiaParser.ABBREV, 0); }
		public TerminalNode ALPHANUM() { return getToken(ScicopiaParser.ALPHANUM, 0); }
		public ChargedContext charged() {
			return getRuleContext(ChargedContext.class,0);
		}
		public TerminalNode APOSTROPHE() { return getToken(ScicopiaParser.APOSTROPHE, 0); }
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_term; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_term);
		try {
			setState(153);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(145);
				match(DASH);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				match(NUM);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(147);
				match(COMPOUND);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(148);
				match(ALPHA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(149);
				match(ABBREV);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(150);
				match(ALPHANUM);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(151);
				charged();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(152);
				match(APOSTROPHE);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ChargedContext extends ParserRuleContext {
		public List<TerminalNode> ALPHANUM() { return getTokens(ScicopiaParser.ALPHANUM); }
		public TerminalNode ALPHANUM(int i) {
			return getToken(ScicopiaParser.ALPHANUM, i);
		}
		public ChargedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_charged; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterCharged(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitCharged(this);
		}
	}

	public final ChargedContext charged() throws RecognitionException {
		ChargedContext _localctx = new ChargedContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_charged);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(156); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(155);
				match(ALPHANUM);
				}
				}
				setState(158); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHANUM );
			setState(160);
			_la = _input.LA(1);
			if ( !(_la==T__3 || _la==T__4) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 2:
			return query_sempred((QueryContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean query_sempred(QueryContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		case 2:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\35\u00a5\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\3\2\6\2\30\n\2\r\2\16\2\31\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\3\4\3\4\3\4\7\4+\n\4\f\4\16\4.\13\4\5\4\60\n\4\3\4\3\4\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\4\3\4\7\4<\n\4\f\4\16\4?\13\4\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\5\5J\n\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6R\n\6\3\7\3\7\7"+
		"\7V\n\7\f\7\16\7Y\13\7\3\7\3\7\3\7\7\7^\n\7\f\7\16\7a\13\7\3\7\5\7d\n"+
		"\7\3\b\6\bg\n\b\r\b\16\bh\3\b\3\b\3\b\3\b\5\bo\n\b\3\t\3\t\3\t\3\t\3\t"+
		"\5\tv\n\t\6\tx\n\t\r\t\16\ty\3\t\3\t\3\t\3\t\3\t\5\t\u0081\n\t\6\t\u0083"+
		"\n\t\r\t\16\t\u0084\3\t\3\t\3\t\3\t\3\t\5\t\u008c\n\t\6\t\u008e\n\t\r"+
		"\t\16\t\u008f\5\t\u0092\n\t\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n\u009c"+
		"\n\n\3\13\6\13\u009f\n\13\r\13\16\13\u00a0\3\13\3\13\3\13\4W_\3\6\f\2"+
		"\4\6\b\n\f\16\20\22\24\2\3\3\2\6\7\2\u00c2\2\27\3\2\2\2\4\35\3\2\2\2\6"+
		"/\3\2\2\2\bI\3\2\2\2\nQ\3\2\2\2\fc\3\2\2\2\16f\3\2\2\2\20\u0091\3\2\2"+
		"\2\22\u009b\3\2\2\2\24\u009e\3\2\2\2\26\30\5\4\3\2\27\26\3\2\2\2\30\31"+
		"\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\33\3\2\2\2\33\34\7\2\2\3\34\3"+
		"\3\2\2\2\35\36\5\6\4\2\36\37\7\34\2\2\37\5\3\2\2\2 !\b\4\1\2!\60\5\b\5"+
		"\2\"#\5\b\5\2#$\5\6\4\b$\60\3\2\2\2%&\7\31\2\2&\'\5\6\4\2\'(\7\32\2\2"+
		"(\60\3\2\2\2)+\5\n\6\2*)\3\2\2\2+.\3\2\2\2,*\3\2\2\2,-\3\2\2\2-\60\3\2"+
		"\2\2.,\3\2\2\2/ \3\2\2\2/\"\3\2\2\2/%\3\2\2\2/,\3\2\2\2\60=\3\2\2\2\61"+
		"\62\f\7\2\2\62\63\5\b\5\2\63\64\5\6\4\b\64<\3\2\2\2\65\66\f\6\2\2\66\67"+
		"\7\22\2\2\67<\5\6\4\789\f\5\2\29:\7\24\2\2:<\5\6\4\6;\61\3\2\2\2;\65\3"+
		"\2\2\2;8\3\2\2\2<?\3\2\2\2=;\3\2\2\2=>\3\2\2\2>\7\3\2\2\2?=\3\2\2\2@A"+
		"\7\23\2\2AJ\5\n\6\2BC\7\23\2\2CD\7\31\2\2DE\5\6\4\2EF\7\32\2\2FJ\3\2\2"+
		"\2GH\7\23\2\2HJ\5\b\5\2I@\3\2\2\2IB\3\2\2\2IG\3\2\2\2J\t\3\2\2\2KR\5\f"+
		"\7\2LR\5\20\t\2MR\5\22\n\2NR\7\f\2\2OR\5\16\b\2PR\7\33\2\2QK\3\2\2\2Q"+
		"L\3\2\2\2QM\3\2\2\2QN\3\2\2\2QO\3\2\2\2QP\3\2\2\2R\13\3\2\2\2SW\7\3\2"+
		"\2TV\13\2\2\2UT\3\2\2\2VY\3\2\2\2WX\3\2\2\2WU\3\2\2\2XZ\3\2\2\2YW\3\2"+
		"\2\2Zd\7\3\2\2[_\7\4\2\2\\^\13\2\2\2]\\\3\2\2\2^a\3\2\2\2_`\3\2\2\2_]"+
		"\3\2\2\2`b\3\2\2\2a_\3\2\2\2bd\7\4\2\2cS\3\2\2\2c[\3\2\2\2d\r\3\2\2\2"+
		"eg\7\26\2\2fe\3\2\2\2gh\3\2\2\2hf\3\2\2\2hi\3\2\2\2ij\3\2\2\2jn\7\5\2"+
		"\2ko\7\33\2\2lo\5\f\7\2mo\5\22\n\2nk\3\2\2\2nl\3\2\2\2nm\3\2\2\2o\17\3"+
		"\2\2\2pw\5\f\7\2qu\7\b\2\2rv\7\33\2\2sv\5\f\7\2tv\5\22\n\2ur\3\2\2\2u"+
		"s\3\2\2\2ut\3\2\2\2vx\3\2\2\2wq\3\2\2\2xy\3\2\2\2yw\3\2\2\2yz\3\2\2\2"+
		"z\u0092\3\2\2\2{\u0082\5\22\n\2|\u0080\7\b\2\2}\u0081\7\33\2\2~\u0081"+
		"\5\f\7\2\177\u0081\5\22\n\2\u0080}\3\2\2\2\u0080~\3\2\2\2\u0080\177\3"+
		"\2\2\2\u0081\u0083\3\2\2\2\u0082|\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u0082"+
		"\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0092\3\2\2\2\u0086\u008d\7\33\2\2"+
		"\u0087\u008b\7\b\2\2\u0088\u008c\7\33\2\2\u0089\u008c\5\f\7\2\u008a\u008c"+
		"\5\22\n\2\u008b\u0088\3\2\2\2\u008b\u0089\3\2\2\2\u008b\u008a\3\2\2\2"+
		"\u008c\u008e\3\2\2\2\u008d\u0087\3\2\2\2\u008e\u008f\3\2\2\2\u008f\u008d"+
		"\3\2\2\2\u008f\u0090\3\2\2\2\u0090\u0092\3\2\2\2\u0091p\3\2\2\2\u0091"+
		"{\3\2\2\2\u0091\u0086\3\2\2\2\u0092\21\3\2\2\2\u0093\u009c\7\16\2\2\u0094"+
		"\u009c\7\17\2\2\u0095\u009c\7\20\2\2\u0096\u009c\7\26\2\2\u0097\u009c"+
		"\7\27\2\2\u0098\u009c\7\30\2\2\u0099\u009c\5\24\13\2\u009a\u009c\7\21"+
		"\2\2\u009b\u0093\3\2\2\2\u009b\u0094\3\2\2\2\u009b\u0095\3\2\2\2\u009b"+
		"\u0096\3\2\2\2\u009b\u0097\3\2\2\2\u009b\u0098\3\2\2\2\u009b\u0099\3\2"+
		"\2\2\u009b\u009a\3\2\2\2\u009c\23\3\2\2\2\u009d\u009f\7\30\2\2\u009e\u009d"+
		"\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1"+
		"\u00a2\3\2\2\2\u00a2\u00a3\t\2\2\2\u00a3\25\3\2\2\2\27\31,/;=IQW_chnu"+
		"y\u0080\u0084\u008b\u008f\u0091\u009b\u00a0";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}