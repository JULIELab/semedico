// Generated from /Users/faessler/Coding/git/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
package de.julielab.scicopia.core.parsing;
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
		RULE_question = 0, RULE_line = 1, RULE_query = 2, RULE_bool = 3, RULE_negation = 4, 
		RULE_token = 5, RULE_quotes = 6, RULE_prefixed = 7, RULE_relation = 8, 
		RULE_term = 9, RULE_charged = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"question", "line", "query", "bool", "negation", "token", "quotes", "prefixed", 
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterQuestion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitQuestion(this);
		}
	}

	public final QuestionContext question() throws RecognitionException {
		QuestionContext _localctx = new QuestionContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_question);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(23); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(22);
				line();
				}
				}
				setState(25); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
			setState(27);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitLine(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		LineContext _localctx = new LineContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_line);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(29);
			query();
			setState(30);
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
		public List<TokenContext> token() {
			return getRuleContexts(TokenContext.class);
		}
		public TokenContext token(int i) {
			return getRuleContext(TokenContext.class,i);
		}
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public List<BoolContext> bool() {
			return getRuleContexts(BoolContext.class);
		}
		public BoolContext bool(int i) {
			return getRuleContext(BoolContext.class,i);
		}
		public QueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_query; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitQuery(this);
		}
	}

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_query);
		int _la;
		try {
			int _alt;
			setState(60);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(33); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(32);
					token();
					}
					}
					setState(35); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << SPECIAL))) != 0) );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(37);
				match(LPAR);
				setState(38);
				query();
				setState(39);
				match(RPAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(56); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(46);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						setState(42); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(41);
								token();
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(44); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					setState(48);
					bool(0);
					setState(54);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
					case 1:
						{
						setState(50); 
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(49);
								token();
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(52); 
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					}
					}
					setState(58); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
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

	public static class BoolContext extends ParserRuleContext {
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public TokenContext token() {
			return getRuleContext(TokenContext.class,0);
		}
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public List<BoolContext> bool() {
			return getRuleContexts(BoolContext.class);
		}
		public BoolContext bool(int i) {
			return getRuleContext(BoolContext.class,i);
		}
		public TerminalNode AND() { return getToken(ScicopiaParser.AND, 0); }
		public TerminalNode OR() { return getToken(ScicopiaParser.OR, 0); }
		public BoolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_bool; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterBool(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitBool(this);
		}
	}

	public final BoolContext bool() throws RecognitionException {
		return bool(0);
	}

	private BoolContext bool(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		BoolContext _localctx = new BoolContext(_ctx, _parentState);
		BoolContext _prevctx = _localctx;
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_bool, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				setState(63);
				negation();
				}
				break;
			case T__0:
			case T__1:
			case IRI:
			case DASH:
			case NUM:
			case COMPOUND:
			case APOSTROPHE:
			case ALPHA:
			case ABBREV:
			case ALPHANUM:
			case SPECIAL:
				{
				setState(64);
				token();
				}
				break;
			case LPAR:
				{
				setState(65);
				match(LPAR);
				setState(66);
				query();
				setState(67);
				match(RPAR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(79);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(77);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new BoolContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bool);
						setState(71);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(72);
						match(AND);
						setState(73);
						bool(4);
						}
						break;
					case 2:
						{
						_localctx = new BoolContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bool);
						setState(74);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(75);
						match(OR);
						setState(76);
						bool(3);
						}
						break;
					}
					} 
				}
				setState(81);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
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
		public BoolContext bool() {
			return getRuleContext(BoolContext.class,0);
		}
		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
		}
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_negation; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterNegation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitNegation(this);
		}
	}

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_negation);
		try {
			setState(88);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(82);
				match(NOT);
				setState(83);
				token();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(84);
				match(NOT);
				setState(85);
				bool(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(86);
				match(NOT);
				setState(87);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterToken(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitToken(this);
		}
	}

	public final TokenContext token() throws RecognitionException {
		TokenContext _localctx = new TokenContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_token);
		try {
			setState(96);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(90);
				quotes();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(91);
				relation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(92);
				term();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(93);
				match(IRI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(94);
				prefixed();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(95);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterQuotes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitQuotes(this);
		}
	}

	public final QuotesContext quotes() throws RecognitionException {
		QuotesContext _localctx = new QuotesContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_quotes);
		try {
			int _alt;
			setState(114);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(98);
				match(T__0);
				setState(102);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(99);
						matchWildcard();
						}
						} 
					}
					setState(104);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,13,_ctx);
				}
				setState(105);
				match(T__0);
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(106);
				match(T__1);
				setState(110);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(107);
						matchWildcard();
						}
						} 
					}
					setState(112);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,14,_ctx);
				}
				setState(113);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterPrefixed(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitPrefixed(this);
		}
	}

	public final PrefixedContext prefixed() throws RecognitionException {
		PrefixedContext _localctx = new PrefixedContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_prefixed);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(117); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(116);
				match(ALPHA);
				}
				}
				setState(119); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHA );
			setState(121);
			match(T__2);
			setState(125);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPECIAL:
				{
				setState(122);
				match(SPECIAL);
				}
				break;
			case T__0:
			case T__1:
				{
				setState(123);
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
				setState(124);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterRelation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitRelation(this);
		}
	}

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_relation);
		try {
			int _alt;
			setState(160);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(127);
				quotes();
				setState(134); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(128);
						match(ARROW);
						setState(132);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(129);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(130);
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
							setState(131);
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
					setState(136); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
				setState(138);
				term();
				setState(145); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(139);
						match(ARROW);
						setState(143);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(140);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(141);
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
							setState(142);
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
					setState(147); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case SPECIAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(149);
				match(SPECIAL);
				setState(156); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(150);
						match(ARROW);
						setState(154);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(151);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(152);
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
							setState(153);
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
					setState(158); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,23,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterTerm(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitTerm(this);
		}
	}

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_term);
		try {
			setState(170);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(162);
				match(DASH);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(163);
				match(NUM);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(164);
				match(COMPOUND);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(165);
				match(ALPHA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(166);
				match(ABBREV);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(167);
				match(ALPHANUM);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(168);
				charged();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(169);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterCharged(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitCharged(this);
		}
	}

	public final ChargedContext charged() throws RecognitionException {
		ChargedContext _localctx = new ChargedContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_charged);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(173); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(172);
				match(ALPHANUM);
				}
				}
				setState(175); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHANUM );
			setState(177);
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
		case 3:
			return bool_sempred((BoolContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean bool_sempred(BoolContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 3);
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\35\u00b6\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\3\2\6\2\32\n\2\r\2\16\2\33\3\2\3\2\3\3\3\3\3\3\3\4\6\4$"+
		"\n\4\r\4\16\4%\3\4\3\4\3\4\3\4\3\4\6\4-\n\4\r\4\16\4.\5\4\61\n\4\3\4\3"+
		"\4\6\4\65\n\4\r\4\16\4\66\5\49\n\4\6\4;\n\4\r\4\16\4<\5\4?\n\4\3\5\3\5"+
		"\3\5\3\5\3\5\3\5\3\5\5\5H\n\5\3\5\3\5\3\5\3\5\3\5\3\5\7\5P\n\5\f\5\16"+
		"\5S\13\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6[\n\6\3\7\3\7\3\7\3\7\3\7\3\7\5\7"+
		"c\n\7\3\b\3\b\7\bg\n\b\f\b\16\bj\13\b\3\b\3\b\3\b\7\bo\n\b\f\b\16\br\13"+
		"\b\3\b\5\bu\n\b\3\t\6\tx\n\t\r\t\16\ty\3\t\3\t\3\t\3\t\5\t\u0080\n\t\3"+
		"\n\3\n\3\n\3\n\3\n\5\n\u0087\n\n\6\n\u0089\n\n\r\n\16\n\u008a\3\n\3\n"+
		"\3\n\3\n\3\n\5\n\u0092\n\n\6\n\u0094\n\n\r\n\16\n\u0095\3\n\3\n\3\n\3"+
		"\n\3\n\5\n\u009d\n\n\6\n\u009f\n\n\r\n\16\n\u00a0\5\n\u00a3\n\n\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13\u00ad\n\13\3\f\6\f\u00b0\n\f\r"+
		"\f\16\f\u00b1\3\f\3\f\3\f\4hp\3\b\r\2\4\6\b\n\f\16\20\22\24\26\2\3\3\2"+
		"\6\7\2\u00d7\2\31\3\2\2\2\4\37\3\2\2\2\6>\3\2\2\2\bG\3\2\2\2\nZ\3\2\2"+
		"\2\fb\3\2\2\2\16t\3\2\2\2\20w\3\2\2\2\22\u00a2\3\2\2\2\24\u00ac\3\2\2"+
		"\2\26\u00af\3\2\2\2\30\32\5\4\3\2\31\30\3\2\2\2\32\33\3\2\2\2\33\31\3"+
		"\2\2\2\33\34\3\2\2\2\34\35\3\2\2\2\35\36\7\2\2\3\36\3\3\2\2\2\37 \5\6"+
		"\4\2 !\7\34\2\2!\5\3\2\2\2\"$\5\f\7\2#\"\3\2\2\2$%\3\2\2\2%#\3\2\2\2%"+
		"&\3\2\2\2&?\3\2\2\2\'(\7\31\2\2()\5\6\4\2)*\7\32\2\2*?\3\2\2\2+-\5\f\7"+
		"\2,+\3\2\2\2-.\3\2\2\2.,\3\2\2\2./\3\2\2\2/\61\3\2\2\2\60,\3\2\2\2\60"+
		"\61\3\2\2\2\61\62\3\2\2\2\628\5\b\5\2\63\65\5\f\7\2\64\63\3\2\2\2\65\66"+
		"\3\2\2\2\66\64\3\2\2\2\66\67\3\2\2\2\679\3\2\2\28\64\3\2\2\289\3\2\2\2"+
		"9;\3\2\2\2:\60\3\2\2\2;<\3\2\2\2<:\3\2\2\2<=\3\2\2\2=?\3\2\2\2>#\3\2\2"+
		"\2>\'\3\2\2\2>:\3\2\2\2?\7\3\2\2\2@A\b\5\1\2AH\5\n\6\2BH\5\f\7\2CD\7\31"+
		"\2\2DE\5\6\4\2EF\7\32\2\2FH\3\2\2\2G@\3\2\2\2GB\3\2\2\2GC\3\2\2\2HQ\3"+
		"\2\2\2IJ\f\5\2\2JK\7\22\2\2KP\5\b\5\6LM\f\4\2\2MN\7\24\2\2NP\5\b\5\5O"+
		"I\3\2\2\2OL\3\2\2\2PS\3\2\2\2QO\3\2\2\2QR\3\2\2\2R\t\3\2\2\2SQ\3\2\2\2"+
		"TU\7\23\2\2U[\5\f\7\2VW\7\23\2\2W[\5\b\5\2XY\7\23\2\2Y[\5\n\6\2ZT\3\2"+
		"\2\2ZV\3\2\2\2ZX\3\2\2\2[\13\3\2\2\2\\c\5\16\b\2]c\5\22\n\2^c\5\24\13"+
		"\2_c\7\f\2\2`c\5\20\t\2ac\7\33\2\2b\\\3\2\2\2b]\3\2\2\2b^\3\2\2\2b_\3"+
		"\2\2\2b`\3\2\2\2ba\3\2\2\2c\r\3\2\2\2dh\7\3\2\2eg\13\2\2\2fe\3\2\2\2g"+
		"j\3\2\2\2hi\3\2\2\2hf\3\2\2\2ik\3\2\2\2jh\3\2\2\2ku\7\3\2\2lp\7\4\2\2"+
		"mo\13\2\2\2nm\3\2\2\2or\3\2\2\2pq\3\2\2\2pn\3\2\2\2qs\3\2\2\2rp\3\2\2"+
		"\2su\7\4\2\2td\3\2\2\2tl\3\2\2\2u\17\3\2\2\2vx\7\26\2\2wv\3\2\2\2xy\3"+
		"\2\2\2yw\3\2\2\2yz\3\2\2\2z{\3\2\2\2{\177\7\5\2\2|\u0080\7\33\2\2}\u0080"+
		"\5\16\b\2~\u0080\5\24\13\2\177|\3\2\2\2\177}\3\2\2\2\177~\3\2\2\2\u0080"+
		"\21\3\2\2\2\u0081\u0088\5\16\b\2\u0082\u0086\7\b\2\2\u0083\u0087\7\33"+
		"\2\2\u0084\u0087\5\16\b\2\u0085\u0087\5\24\13\2\u0086\u0083\3\2\2\2\u0086"+
		"\u0084\3\2\2\2\u0086\u0085\3\2\2\2\u0087\u0089\3\2\2\2\u0088\u0082\3\2"+
		"\2\2\u0089\u008a\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b"+
		"\u00a3\3\2\2\2\u008c\u0093\5\24\13\2\u008d\u0091\7\b\2\2\u008e\u0092\7"+
		"\33\2\2\u008f\u0092\5\16\b\2\u0090\u0092\5\24\13\2\u0091\u008e\3\2\2\2"+
		"\u0091\u008f\3\2\2\2\u0091\u0090\3\2\2\2\u0092\u0094\3\2\2\2\u0093\u008d"+
		"\3\2\2\2\u0094\u0095\3\2\2\2\u0095\u0093\3\2\2\2\u0095\u0096\3\2\2\2\u0096"+
		"\u00a3\3\2\2\2\u0097\u009e\7\33\2\2\u0098\u009c\7\b\2\2\u0099\u009d\7"+
		"\33\2\2\u009a\u009d\5\16\b\2\u009b\u009d\5\24\13\2\u009c\u0099\3\2\2\2"+
		"\u009c\u009a\3\2\2\2\u009c\u009b\3\2\2\2\u009d\u009f\3\2\2\2\u009e\u0098"+
		"\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0\u009e\3\2\2\2\u00a0\u00a1\3\2\2\2\u00a1"+
		"\u00a3\3\2\2\2\u00a2\u0081\3\2\2\2\u00a2\u008c\3\2\2\2\u00a2\u0097\3\2"+
		"\2\2\u00a3\23\3\2\2\2\u00a4\u00ad\7\16\2\2\u00a5\u00ad\7\17\2\2\u00a6"+
		"\u00ad\7\20\2\2\u00a7\u00ad\7\26\2\2\u00a8\u00ad\7\27\2\2\u00a9\u00ad"+
		"\7\30\2\2\u00aa\u00ad\5\26\f\2\u00ab\u00ad\7\21\2\2\u00ac\u00a4\3\2\2"+
		"\2\u00ac\u00a5\3\2\2\2\u00ac\u00a6\3\2\2\2\u00ac\u00a7\3\2\2\2\u00ac\u00a8"+
		"\3\2\2\2\u00ac\u00a9\3\2\2\2\u00ac\u00aa\3\2\2\2\u00ac\u00ab\3\2\2\2\u00ad"+
		"\25\3\2\2\2\u00ae\u00b0\7\30\2\2\u00af\u00ae\3\2\2\2\u00b0\u00b1\3\2\2"+
		"\2\u00b1\u00af\3\2\2\2\u00b1\u00b2\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3\u00b4"+
		"\t\2\2\2\u00b4\27\3\2\2\2\35\33%.\60\668<>GOQZbhpty\177\u0086\u008a\u0091"+
		"\u0095\u009c\u00a0\u00a2\u00ac\u00b1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}