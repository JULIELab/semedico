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
		RULE_question = 0, RULE_line = 1, RULE_query = 2, RULE_tokensequence = 3, 
		RULE_parenQuery = 4, RULE_bool = 5, RULE_negation = 6, RULE_token = 7, 
		RULE_quotes = 8, RULE_prefixed = 9, RULE_relation = 10, RULE_term = 11, 
		RULE_charged = 12;
	private static String[] makeRuleNames() {
		return new String[] {
			"question", "line", "query", "tokensequence", "parenQuery", "bool", "negation", 
			"token", "quotes", "prefixed", "relation", "term", "charged"
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
			setState(27); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(26);
				line();
				}
				}
				setState(29); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
			setState(31);
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
			setState(33);
			query();
			setState(34);
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
		public List<TokensequenceContext> tokensequence() {
			return getRuleContexts(TokensequenceContext.class);
		}
		public TokensequenceContext tokensequence(int i) {
			return getRuleContext(TokensequenceContext.class,i);
		}
		public List<BoolContext> bool() {
			return getRuleContexts(BoolContext.class);
		}
		public BoolContext bool(int i) {
			return getRuleContext(BoolContext.class,i);
		}
		public ParenQueryContext parenQuery() {
			return getRuleContext(ParenQueryContext.class,0);
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
			setState(49);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(36);
				tokensequence();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(44); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(38);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
					case 1:
						{
						setState(37);
						tokensequence();
						}
						break;
					}
					setState(40);
					bool(0);
					setState(42);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
					case 1:
						{
						setState(41);
						tokensequence();
						}
						break;
					}
					}
					}
					setState(46); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(48);
				parenQuery();
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

	public static class TokensequenceContext extends ParserRuleContext {
		public List<TokenContext> token() {
			return getRuleContexts(TokenContext.class);
		}
		public TokenContext token(int i) {
			return getRuleContext(TokenContext.class,i);
		}
		public TokensequenceContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tokensequence; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterTokensequence(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitTokensequence(this);
		}
	}

	public final TokensequenceContext tokensequence() throws RecognitionException {
		TokensequenceContext _localctx = new TokensequenceContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_tokensequence);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(52); 
			_errHandler.sync(this);
			_alt = 1;
			do {
				switch (_alt) {
				case 1:
					{
					{
					setState(51);
					token();
					}
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(54); 
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,5,_ctx);
			} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
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

	public static class ParenQueryContext extends ParserRuleContext {
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public QueryContext query() {
			return getRuleContext(QueryContext.class,0);
		}
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public ParenQueryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_parenQuery; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterParenQuery(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitParenQuery(this);
		}
	}

	public final ParenQueryContext parenQuery() throws RecognitionException {
		ParenQueryContext _localctx = new ParenQueryContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_parenQuery);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			match(LPAR);
			setState(57);
			query();
			setState(58);
			match(RPAR);
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
		public ParenQueryContext parenQuery() {
			return getRuleContext(ParenQueryContext.class,0);
		}
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
		int _startState = 10;
		enterRecursionRule(_localctx, 10, RULE_bool, _p);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				setState(61);
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
				setState(62);
				token();
				}
				break;
			case LPAR:
				{
				setState(63);
				parenQuery();
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(74);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(72);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
					case 1:
						{
						_localctx = new BoolContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bool);
						setState(66);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(67);
						match(AND);
						setState(68);
						bool(4);
						}
						break;
					case 2:
						{
						_localctx = new BoolContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bool);
						setState(69);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(70);
						match(OR);
						setState(71);
						bool(3);
						}
						break;
					}
					} 
				}
				setState(76);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
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
		enterRule(_localctx, 12, RULE_negation);
		try {
			setState(83);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(77);
				match(NOT);
				setState(78);
				token();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(79);
				match(NOT);
				setState(80);
				bool(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(81);
				match(NOT);
				setState(82);
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
		enterRule(_localctx, 14, RULE_token);
		try {
			setState(91);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(85);
				quotes();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
				relation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(87);
				term();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(88);
				match(IRI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(89);
				prefixed();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(90);
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
		enterRule(_localctx, 16, RULE_quotes);
		try {
			int _alt;
			setState(109);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(93);
				match(T__0);
				setState(97);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(94);
						matchWildcard();
						}
						} 
					}
					setState(99);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,11,_ctx);
				}
				setState(100);
				match(T__0);
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(101);
				match(T__1);
				setState(105);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(102);
						matchWildcard();
						}
						} 
					}
					setState(107);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
				}
				setState(108);
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
		enterRule(_localctx, 18, RULE_prefixed);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(112); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(111);
				match(ALPHA);
				}
				}
				setState(114); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHA );
			setState(116);
			match(T__2);
			setState(120);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPECIAL:
				{
				setState(117);
				match(SPECIAL);
				}
				break;
			case T__0:
			case T__1:
				{
				setState(118);
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
				setState(119);
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
		enterRule(_localctx, 20, RULE_relation);
		try {
			int _alt;
			setState(155);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(122);
				quotes();
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
				setState(133);
				term();
				setState(140); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(134);
						match(ARROW);
						setState(138);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(135);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(136);
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
							setState(137);
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
					setState(142); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case SPECIAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(144);
				match(SPECIAL);
				setState(151); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(145);
						match(ARROW);
						setState(149);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(146);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(147);
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
							setState(148);
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
					setState(153); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
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
		enterRule(_localctx, 22, RULE_term);
		try {
			setState(165);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,23,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(157);
				match(DASH);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(158);
				match(NUM);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(159);
				match(COMPOUND);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(160);
				match(ALPHA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(161);
				match(ABBREV);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(162);
				match(ALPHANUM);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(163);
				charged();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(164);
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
		enterRule(_localctx, 24, RULE_charged);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(168); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(167);
				match(ALPHANUM);
				}
				}
				setState(170); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHANUM );
			setState(172);
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
		case 5:
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\35\u00b1\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\6\2\36\n\2\r\2\16\2\37\3\2\3\2\3\3"+
		"\3\3\3\3\3\4\3\4\5\4)\n\4\3\4\3\4\5\4-\n\4\6\4/\n\4\r\4\16\4\60\3\4\5"+
		"\4\64\n\4\3\5\6\5\67\n\5\r\5\16\58\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3\7\5\7"+
		"C\n\7\3\7\3\7\3\7\3\7\3\7\3\7\7\7K\n\7\f\7\16\7N\13\7\3\b\3\b\3\b\3\b"+
		"\3\b\3\b\5\bV\n\b\3\t\3\t\3\t\3\t\3\t\3\t\5\t^\n\t\3\n\3\n\7\nb\n\n\f"+
		"\n\16\ne\13\n\3\n\3\n\3\n\7\nj\n\n\f\n\16\nm\13\n\3\n\5\np\n\n\3\13\6"+
		"\13s\n\13\r\13\16\13t\3\13\3\13\3\13\3\13\5\13{\n\13\3\f\3\f\3\f\3\f\3"+
		"\f\5\f\u0082\n\f\6\f\u0084\n\f\r\f\16\f\u0085\3\f\3\f\3\f\3\f\3\f\5\f"+
		"\u008d\n\f\6\f\u008f\n\f\r\f\16\f\u0090\3\f\3\f\3\f\3\f\3\f\5\f\u0098"+
		"\n\f\6\f\u009a\n\f\r\f\16\f\u009b\5\f\u009e\n\f\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\5\r\u00a8\n\r\3\16\6\16\u00ab\n\16\r\16\16\16\u00ac\3\16\3"+
		"\16\3\16\4ck\3\f\17\2\4\6\b\n\f\16\20\22\24\26\30\32\2\3\3\2\6\7\2\u00ce"+
		"\2\35\3\2\2\2\4#\3\2\2\2\6\63\3\2\2\2\b\66\3\2\2\2\n:\3\2\2\2\fB\3\2\2"+
		"\2\16U\3\2\2\2\20]\3\2\2\2\22o\3\2\2\2\24r\3\2\2\2\26\u009d\3\2\2\2\30"+
		"\u00a7\3\2\2\2\32\u00aa\3\2\2\2\34\36\5\4\3\2\35\34\3\2\2\2\36\37\3\2"+
		"\2\2\37\35\3\2\2\2\37 \3\2\2\2 !\3\2\2\2!\"\7\2\2\3\"\3\3\2\2\2#$\5\6"+
		"\4\2$%\7\34\2\2%\5\3\2\2\2&\64\5\b\5\2\')\5\b\5\2(\'\3\2\2\2()\3\2\2\2"+
		")*\3\2\2\2*,\5\f\7\2+-\5\b\5\2,+\3\2\2\2,-\3\2\2\2-/\3\2\2\2.(\3\2\2\2"+
		"/\60\3\2\2\2\60.\3\2\2\2\60\61\3\2\2\2\61\64\3\2\2\2\62\64\5\n\6\2\63"+
		"&\3\2\2\2\63.\3\2\2\2\63\62\3\2\2\2\64\7\3\2\2\2\65\67\5\20\t\2\66\65"+
		"\3\2\2\2\678\3\2\2\28\66\3\2\2\289\3\2\2\29\t\3\2\2\2:;\7\31\2\2;<\5\6"+
		"\4\2<=\7\32\2\2=\13\3\2\2\2>?\b\7\1\2?C\5\16\b\2@C\5\20\t\2AC\5\n\6\2"+
		"B>\3\2\2\2B@\3\2\2\2BA\3\2\2\2CL\3\2\2\2DE\f\5\2\2EF\7\22\2\2FK\5\f\7"+
		"\6GH\f\4\2\2HI\7\24\2\2IK\5\f\7\5JD\3\2\2\2JG\3\2\2\2KN\3\2\2\2LJ\3\2"+
		"\2\2LM\3\2\2\2M\r\3\2\2\2NL\3\2\2\2OP\7\23\2\2PV\5\20\t\2QR\7\23\2\2R"+
		"V\5\f\7\2ST\7\23\2\2TV\5\16\b\2UO\3\2\2\2UQ\3\2\2\2US\3\2\2\2V\17\3\2"+
		"\2\2W^\5\22\n\2X^\5\26\f\2Y^\5\30\r\2Z^\7\f\2\2[^\5\24\13\2\\^\7\33\2"+
		"\2]W\3\2\2\2]X\3\2\2\2]Y\3\2\2\2]Z\3\2\2\2][\3\2\2\2]\\\3\2\2\2^\21\3"+
		"\2\2\2_c\7\3\2\2`b\13\2\2\2a`\3\2\2\2be\3\2\2\2cd\3\2\2\2ca\3\2\2\2df"+
		"\3\2\2\2ec\3\2\2\2fp\7\3\2\2gk\7\4\2\2hj\13\2\2\2ih\3\2\2\2jm\3\2\2\2"+
		"kl\3\2\2\2ki\3\2\2\2ln\3\2\2\2mk\3\2\2\2np\7\4\2\2o_\3\2\2\2og\3\2\2\2"+
		"p\23\3\2\2\2qs\7\26\2\2rq\3\2\2\2st\3\2\2\2tr\3\2\2\2tu\3\2\2\2uv\3\2"+
		"\2\2vz\7\5\2\2w{\7\33\2\2x{\5\22\n\2y{\5\30\r\2zw\3\2\2\2zx\3\2\2\2zy"+
		"\3\2\2\2{\25\3\2\2\2|\u0083\5\22\n\2}\u0081\7\b\2\2~\u0082\7\33\2\2\177"+
		"\u0082\5\22\n\2\u0080\u0082\5\30\r\2\u0081~\3\2\2\2\u0081\177\3\2\2\2"+
		"\u0081\u0080\3\2\2\2\u0082\u0084\3\2\2\2\u0083}\3\2\2\2\u0084\u0085\3"+
		"\2\2\2\u0085\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u009e\3\2\2\2\u0087"+
		"\u008e\5\30\r\2\u0088\u008c\7\b\2\2\u0089\u008d\7\33\2\2\u008a\u008d\5"+
		"\22\n\2\u008b\u008d\5\30\r\2\u008c\u0089\3\2\2\2\u008c\u008a\3\2\2\2\u008c"+
		"\u008b\3\2\2\2\u008d\u008f\3\2\2\2\u008e\u0088\3\2\2\2\u008f\u0090\3\2"+
		"\2\2\u0090\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u009e\3\2\2\2\u0092"+
		"\u0099\7\33\2\2\u0093\u0097\7\b\2\2\u0094\u0098\7\33\2\2\u0095\u0098\5"+
		"\22\n\2\u0096\u0098\5\30\r\2\u0097\u0094\3\2\2\2\u0097\u0095\3\2\2\2\u0097"+
		"\u0096\3\2\2\2\u0098\u009a\3\2\2\2\u0099\u0093\3\2\2\2\u009a\u009b\3\2"+
		"\2\2\u009b\u0099\3\2\2\2\u009b\u009c\3\2\2\2\u009c\u009e\3\2\2\2\u009d"+
		"|\3\2\2\2\u009d\u0087\3\2\2\2\u009d\u0092\3\2\2\2\u009e\27\3\2\2\2\u009f"+
		"\u00a8\7\16\2\2\u00a0\u00a8\7\17\2\2\u00a1\u00a8\7\20\2\2\u00a2\u00a8"+
		"\7\26\2\2\u00a3\u00a8\7\27\2\2\u00a4\u00a8\7\30\2\2\u00a5\u00a8\5\32\16"+
		"\2\u00a6\u00a8\7\21\2\2\u00a7\u009f\3\2\2\2\u00a7\u00a0\3\2\2\2\u00a7"+
		"\u00a1\3\2\2\2\u00a7\u00a2\3\2\2\2\u00a7\u00a3\3\2\2\2\u00a7\u00a4\3\2"+
		"\2\2\u00a7\u00a5\3\2\2\2\u00a7\u00a6\3\2\2\2\u00a8\31\3\2\2\2\u00a9\u00ab"+
		"\7\30\2\2\u00aa\u00a9\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00aa\3\2\2\2"+
		"\u00ac\u00ad\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00af\t\2\2\2\u00af\33"+
		"\3\2\2\2\33\37(,\60\638BJLU]ckotz\u0081\u0085\u008c\u0090\u0097\u009b"+
		"\u009d\u00a7\u00ac";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}