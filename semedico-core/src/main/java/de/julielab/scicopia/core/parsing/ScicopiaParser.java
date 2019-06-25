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
		RULE_question = 0, RULE_line = 1, RULE_query = 2, RULE_notexpr = 3, RULE_part = 4, 
		RULE_quotes = 5, RULE_prefixed = 6, RULE_relation = 7, RULE_term = 8, 
		RULE_charged = 9;
	private static String[] makeRuleNames() {
		return new String[] {
			"question", "line", "query", "notexpr", "part", "quotes", "prefixed", 
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
		public NotexprContext notexpr() {
			return getRuleContext(NotexprContext.class,0);
		}
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public List<QueryContext> query() {
			return getRuleContexts(QueryContext.class);
		}
		public QueryContext query(int i) {
			return getRuleContext(QueryContext.class,i);
		}
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public List<PartContext> part() {
			return getRuleContexts(PartContext.class);
		}
		public PartContext part(int i) {
			return getRuleContext(PartContext.class,i);
		}
		public TerminalNode AND() { return getToken(ScicopiaParser.AND, 0); }
		public TerminalNode OR() { return getToken(ScicopiaParser.OR, 0); }
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitQuery(this);
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
			setState(42);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				{
				setState(31);
				notexpr();
				}
				break;
			case 2:
				{
				setState(32);
				match(LPAR);
				setState(33);
				query(0);
				setState(34);
				match(RPAR);
				}
				break;
			case 3:
				{
				setState(39);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1 ) {
						{
						{
						setState(36);
						part();
						}
						} 
					}
					setState(41);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				}
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(54);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
			while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(52);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						_localctx = new QueryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(44);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(45);
						match(AND);
						setState(46);
						query(5);
						}
						break;
					case 2:
						{
						_localctx = new QueryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(47);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(48);
						match(OR);
						setState(49);
						query(4);
						}
						break;
					case 3:
						{
						_localctx = new QueryContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_query);
						setState(50);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(51);
						notexpr();
						}
						break;
					}
					} 
				}
				setState(56);
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

	public static class NotexprContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(ScicopiaParser.NOT, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public NotexprContext notexpr() {
			return getRuleContext(NotexprContext.class,0);
		}
		public NotexprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_notexpr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterNotexpr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitNotexpr(this);
		}
	}

	public final NotexprContext notexpr() throws RecognitionException {
		NotexprContext _localctx = new NotexprContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_notexpr);
		try {
			setState(61);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(57);
				match(NOT);
				setState(58);
				query(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(59);
				match(NOT);
				setState(60);
				notexpr();
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

	public static class PartContext extends ParserRuleContext {
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
		public PartContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_part; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).exitPart(this);
		}
	}

	public final PartContext part() throws RecognitionException {
		PartContext _localctx = new PartContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_part);
		try {
			setState(69);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(63);
				quotes();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(64);
				relation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(65);
				term();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(66);
				match(IRI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(67);
				prefixed();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(68);
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
			setState(87);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(71);
				match(T__0);
				setState(75);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				while ( _alt!=1 && _alt!=ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(72);
						matchWildcard();
						}
						} 
					}
					setState(77);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,7,_ctx);
				}
				setState(78);
				match(T__0);
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(79);
				match(T__1);
				setState(83);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				while ( _alt!=1 && _alt!=ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(80);
						matchWildcard();
						}
						} 
					}
					setState(85);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				}
				setState(86);
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
			setState(90); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(89);
				match(ALPHA);
				}
				}
				setState(92); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHA );
			setState(94);
			match(T__2);
			setState(98);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPECIAL:
				{
				setState(95);
				match(SPECIAL);
				}
				break;
			case T__0:
			case T__1:
				{
				setState(96);
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
				setState(97);
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
			setState(133);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(100);
				quotes();
				setState(107); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(101);
						match(ARROW);
						setState(105);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(102);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(103);
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
							setState(104);
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
					setState(109); 
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
				setState(111);
				term();
				setState(118); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(112);
						match(ARROW);
						setState(116);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(113);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(114);
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
							setState(115);
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
					setState(120); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,15,_ctx);
				} while ( _alt!=2 && _alt!=ATN.INVALID_ALT_NUMBER );
				}
				break;
			case SPECIAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(122);
				match(SPECIAL);
				setState(129); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(123);
						match(ARROW);
						setState(127);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(124);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(125);
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
							setState(126);
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
					setState(131); 
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
			setState(143);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(135);
				match(DASH);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(136);
				match(NUM);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(137);
				match(COMPOUND);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(138);
				match(ALPHA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(139);
				match(ABBREV);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(140);
				match(ALPHANUM);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(141);
				charged();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(142);
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
			setState(146); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(145);
				match(ALPHANUM);
				}
				}
				setState(148); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHANUM );
			setState(150);
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
			return precpred(_ctx, 4);
		case 1:
			return precpred(_ctx, 3);
		case 2:
			return precpred(_ctx, 5);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\35\u009b\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\3\2\6\2\30\n\2\r\2\16\2\31\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\3\4\7\4(\n\4\f\4\16\4+\13\4\5\4-\n\4\3\4\3\4\3\4\3\4\3\4\3\4\3"+
		"\4\3\4\7\4\67\n\4\f\4\16\4:\13\4\3\5\3\5\3\5\3\5\5\5@\n\5\3\6\3\6\3\6"+
		"\3\6\3\6\3\6\5\6H\n\6\3\7\3\7\7\7L\n\7\f\7\16\7O\13\7\3\7\3\7\3\7\7\7"+
		"T\n\7\f\7\16\7W\13\7\3\7\5\7Z\n\7\3\b\6\b]\n\b\r\b\16\b^\3\b\3\b\3\b\3"+
		"\b\5\be\n\b\3\t\3\t\3\t\3\t\3\t\5\tl\n\t\6\tn\n\t\r\t\16\to\3\t\3\t\3"+
		"\t\3\t\3\t\5\tw\n\t\6\ty\n\t\r\t\16\tz\3\t\3\t\3\t\3\t\3\t\5\t\u0082\n"+
		"\t\6\t\u0084\n\t\r\t\16\t\u0085\5\t\u0088\n\t\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\5\n\u0092\n\n\3\13\6\13\u0095\n\13\r\13\16\13\u0096\3\13\3\13"+
		"\3\13\4MU\3\6\f\2\4\6\b\n\f\16\20\22\24\2\3\3\2\6\7\2\u00b6\2\27\3\2\2"+
		"\2\4\35\3\2\2\2\6,\3\2\2\2\b?\3\2\2\2\nG\3\2\2\2\fY\3\2\2\2\16\\\3\2\2"+
		"\2\20\u0087\3\2\2\2\22\u0091\3\2\2\2\24\u0094\3\2\2\2\26\30\5\4\3\2\27"+
		"\26\3\2\2\2\30\31\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\33\3\2\2\2\33"+
		"\34\7\2\2\3\34\3\3\2\2\2\35\36\5\6\4\2\36\37\7\34\2\2\37\5\3\2\2\2 !\b"+
		"\4\1\2!-\5\b\5\2\"#\7\31\2\2#$\5\6\4\2$%\7\32\2\2%-\3\2\2\2&(\5\n\6\2"+
		"\'&\3\2\2\2(+\3\2\2\2)\'\3\2\2\2)*\3\2\2\2*-\3\2\2\2+)\3\2\2\2, \3\2\2"+
		"\2,\"\3\2\2\2,)\3\2\2\2-8\3\2\2\2./\f\6\2\2/\60\7\22\2\2\60\67\5\6\4\7"+
		"\61\62\f\5\2\2\62\63\7\24\2\2\63\67\5\6\4\6\64\65\f\7\2\2\65\67\5\b\5"+
		"\2\66.\3\2\2\2\66\61\3\2\2\2\66\64\3\2\2\2\67:\3\2\2\28\66\3\2\2\289\3"+
		"\2\2\29\7\3\2\2\2:8\3\2\2\2;<\7\23\2\2<@\5\6\4\2=>\7\23\2\2>@\5\b\5\2"+
		"?;\3\2\2\2?=\3\2\2\2@\t\3\2\2\2AH\5\f\7\2BH\5\20\t\2CH\5\22\n\2DH\7\f"+
		"\2\2EH\5\16\b\2FH\7\33\2\2GA\3\2\2\2GB\3\2\2\2GC\3\2\2\2GD\3\2\2\2GE\3"+
		"\2\2\2GF\3\2\2\2H\13\3\2\2\2IM\7\3\2\2JL\13\2\2\2KJ\3\2\2\2LO\3\2\2\2"+
		"MN\3\2\2\2MK\3\2\2\2NP\3\2\2\2OM\3\2\2\2PZ\7\3\2\2QU\7\4\2\2RT\13\2\2"+
		"\2SR\3\2\2\2TW\3\2\2\2UV\3\2\2\2US\3\2\2\2VX\3\2\2\2WU\3\2\2\2XZ\7\4\2"+
		"\2YI\3\2\2\2YQ\3\2\2\2Z\r\3\2\2\2[]\7\26\2\2\\[\3\2\2\2]^\3\2\2\2^\\\3"+
		"\2\2\2^_\3\2\2\2_`\3\2\2\2`d\7\5\2\2ae\7\33\2\2be\5\f\7\2ce\5\22\n\2d"+
		"a\3\2\2\2db\3\2\2\2dc\3\2\2\2e\17\3\2\2\2fm\5\f\7\2gk\7\b\2\2hl\7\33\2"+
		"\2il\5\f\7\2jl\5\22\n\2kh\3\2\2\2ki\3\2\2\2kj\3\2\2\2ln\3\2\2\2mg\3\2"+
		"\2\2no\3\2\2\2om\3\2\2\2op\3\2\2\2p\u0088\3\2\2\2qx\5\22\n\2rv\7\b\2\2"+
		"sw\7\33\2\2tw\5\f\7\2uw\5\22\n\2vs\3\2\2\2vt\3\2\2\2vu\3\2\2\2wy\3\2\2"+
		"\2xr\3\2\2\2yz\3\2\2\2zx\3\2\2\2z{\3\2\2\2{\u0088\3\2\2\2|\u0083\7\33"+
		"\2\2}\u0081\7\b\2\2~\u0082\7\33\2\2\177\u0082\5\f\7\2\u0080\u0082\5\22"+
		"\n\2\u0081~\3\2\2\2\u0081\177\3\2\2\2\u0081\u0080\3\2\2\2\u0082\u0084"+
		"\3\2\2\2\u0083}\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0083\3\2\2\2\u0085"+
		"\u0086\3\2\2\2\u0086\u0088\3\2\2\2\u0087f\3\2\2\2\u0087q\3\2\2\2\u0087"+
		"|\3\2\2\2\u0088\21\3\2\2\2\u0089\u0092\7\16\2\2\u008a\u0092\7\17\2\2\u008b"+
		"\u0092\7\20\2\2\u008c\u0092\7\26\2\2\u008d\u0092\7\27\2\2\u008e\u0092"+
		"\7\30\2\2\u008f\u0092\5\24\13\2\u0090\u0092\7\21\2\2\u0091\u0089\3\2\2"+
		"\2\u0091\u008a\3\2\2\2\u0091\u008b\3\2\2\2\u0091\u008c\3\2\2\2\u0091\u008d"+
		"\3\2\2\2\u0091\u008e\3\2\2\2\u0091\u008f\3\2\2\2\u0091\u0090\3\2\2\2\u0092"+
		"\23\3\2\2\2\u0093\u0095\7\30\2\2\u0094\u0093\3\2\2\2\u0095\u0096\3\2\2"+
		"\2\u0096\u0094\3\2\2\2\u0096\u0097\3\2\2\2\u0097\u0098\3\2\2\2\u0098\u0099"+
		"\t\2\2\2\u0099\25\3\2\2\2\27\31),\668?GMUY^dkovz\u0081\u0085\u0087\u0091"+
		"\u0096";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}