// Generated from /Users/faessler/Coding/git/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
package de.julielab.scicopia.core.parsing;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ScicopiaParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.7.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, ARROW=6, ARROWRIGHT=7, ARROWLEFT=8, 
		ARROWBOTH=9, IRI=10, HASHTAG=11, DASH=12, WILDCARD=13, NUM=14, COMPOUND=15, 
		APOSTROPHE=16, AND=17, NOT=18, OR=19, DIGITS=20, ALPHA=21, ABBREV=22, 
		ALPHANUM=23, LPAR=24, RPAR=25, SPECIAL=26, NL=27, WHITESPACE=28;
	public static final int
		RULE_question = 0, RULE_line = 1, RULE_query = 2, RULE_bool = 3, RULE_negation = 4, 
		RULE_token = 5, RULE_quotes = 6, RULE_doublequotes = 7, RULE_singlequotes = 8, 
		RULE_prefixed = 9, RULE_relation = 10, RULE_term = 11, RULE_charged = 12;
	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\36\u00bf\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\3\2\6\2\36\n\2\r\2\16\2\37\3\2\3\2\3\3"+
		"\3\3\3\3\3\4\6\4(\n\4\r\4\16\4)\3\4\3\4\3\4\3\4\3\4\6\4\61\n\4\r\4\16"+
		"\4\62\5\4\65\n\4\3\4\3\4\6\49\n\4\r\4\16\4:\5\4=\n\4\6\4?\n\4\r\4\16\4"+
		"@\5\4C\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\5\5L\n\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\7\5T\n\5\f\5\16\5W\13\5\3\6\3\6\3\6\3\6\3\6\3\6\5\6_\n\6\3\7\3\7\3"+
		"\7\3\7\3\7\3\7\5\7g\n\7\3\b\3\b\5\bk\n\b\3\t\3\t\7\to\n\t\f\t\16\tr\13"+
		"\t\3\t\3\t\3\n\3\n\7\nx\n\n\f\n\16\n{\13\n\3\n\3\n\3\13\6\13\u0080\n\13"+
		"\r\13\16\13\u0081\3\13\3\13\3\13\3\13\5\13\u0088\n\13\3\f\3\f\3\f\3\f"+
		"\3\f\5\f\u008f\n\f\6\f\u0091\n\f\r\f\16\f\u0092\3\f\3\f\3\f\3\f\3\f\5"+
		"\f\u009a\n\f\6\f\u009c\n\f\r\f\16\f\u009d\3\f\3\f\3\f\3\f\3\f\5\f\u00a5"+
		"\n\f\6\f\u00a7\n\f\r\f\16\f\u00a8\5\f\u00ab\n\f\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\r\3\r\3\r\5\r\u00b6\n\r\3\16\6\16\u00b9\n\16\r\16\16\16\u00ba\3\16"+
		"\3\16\3\16\2\3\b\17\2\4\6\b\n\f\16\20\22\24\26\30\32\2\5\3\2\3\3\3\2\4"+
		"\4\3\2\6\7\2\u00df\2\35\3\2\2\2\4#\3\2\2\2\6B\3\2\2\2\bK\3\2\2\2\n^\3"+
		"\2\2\2\ff\3\2\2\2\16j\3\2\2\2\20l\3\2\2\2\22u\3\2\2\2\24\177\3\2\2\2\26"+
		"\u00aa\3\2\2\2\30\u00b5\3\2\2\2\32\u00b8\3\2\2\2\34\36\5\4\3\2\35\34\3"+
		"\2\2\2\36\37\3\2\2\2\37\35\3\2\2\2\37 \3\2\2\2 !\3\2\2\2!\"\7\2\2\3\""+
		"\3\3\2\2\2#$\5\6\4\2$%\7\35\2\2%\5\3\2\2\2&(\5\f\7\2\'&\3\2\2\2()\3\2"+
		"\2\2)\'\3\2\2\2)*\3\2\2\2*C\3\2\2\2+,\7\32\2\2,-\5\6\4\2-.\7\33\2\2.C"+
		"\3\2\2\2/\61\5\f\7\2\60/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2"+
		"\2\2\63\65\3\2\2\2\64\60\3\2\2\2\64\65\3\2\2\2\65\66\3\2\2\2\66<\5\b\5"+
		"\2\679\5\f\7\28\67\3\2\2\29:\3\2\2\2:8\3\2\2\2:;\3\2\2\2;=\3\2\2\2<8\3"+
		"\2\2\2<=\3\2\2\2=?\3\2\2\2>\64\3\2\2\2?@\3\2\2\2@>\3\2\2\2@A\3\2\2\2A"+
		"C\3\2\2\2B\'\3\2\2\2B+\3\2\2\2B>\3\2\2\2C\7\3\2\2\2DE\b\5\1\2EL\5\n\6"+
		"\2FL\5\f\7\2GH\7\32\2\2HI\5\6\4\2IJ\7\33\2\2JL\3\2\2\2KD\3\2\2\2KF\3\2"+
		"\2\2KG\3\2\2\2LU\3\2\2\2MN\f\5\2\2NO\7\23\2\2OT\5\b\5\6PQ\f\4\2\2QR\7"+
		"\25\2\2RT\5\b\5\5SM\3\2\2\2SP\3\2\2\2TW\3\2\2\2US\3\2\2\2UV\3\2\2\2V\t"+
		"\3\2\2\2WU\3\2\2\2XY\7\24\2\2Y_\5\f\7\2Z[\7\24\2\2[_\5\b\5\2\\]\7\24\2"+
		"\2]_\5\n\6\2^X\3\2\2\2^Z\3\2\2\2^\\\3\2\2\2_\13\3\2\2\2`g\5\26\f\2ag\5"+
		"\30\r\2bg\7\f\2\2cg\5\24\13\2dg\7\34\2\2eg\5\16\b\2f`\3\2\2\2fa\3\2\2"+
		"\2fb\3\2\2\2fc\3\2\2\2fd\3\2\2\2fe\3\2\2\2g\r\3\2\2\2hk\5\20\t\2ik\5\22"+
		"\n\2jh\3\2\2\2ji\3\2\2\2k\17\3\2\2\2lp\7\3\2\2mo\n\2\2\2nm\3\2\2\2or\3"+
		"\2\2\2pn\3\2\2\2pq\3\2\2\2qs\3\2\2\2rp\3\2\2\2st\7\3\2\2t\21\3\2\2\2u"+
		"y\7\4\2\2vx\n\3\2\2wv\3\2\2\2x{\3\2\2\2yw\3\2\2\2yz\3\2\2\2z|\3\2\2\2"+
		"{y\3\2\2\2|}\7\4\2\2}\23\3\2\2\2~\u0080\7\27\2\2\177~\3\2\2\2\u0080\u0081"+
		"\3\2\2\2\u0081\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082\u0083\3\2\2\2\u0083"+
		"\u0087\7\5\2\2\u0084\u0088\7\34\2\2\u0085\u0088\5\16\b\2\u0086\u0088\5"+
		"\30\r\2\u0087\u0084\3\2\2\2\u0087\u0085\3\2\2\2\u0087\u0086\3\2\2\2\u0088"+
		"\25\3\2\2\2\u0089\u0090\5\16\b\2\u008a\u008e\7\b\2\2\u008b\u008f\7\34"+
		"\2\2\u008c\u008f\5\16\b\2\u008d\u008f\5\30\r\2\u008e\u008b\3\2\2\2\u008e"+
		"\u008c\3\2\2\2\u008e\u008d\3\2\2\2\u008f\u0091\3\2\2\2\u0090\u008a\3\2"+
		"\2\2\u0091\u0092\3\2\2\2\u0092\u0090\3\2\2\2\u0092\u0093\3\2\2\2\u0093"+
		"\u00ab\3\2\2\2\u0094\u009b\5\30\r\2\u0095\u0099\7\b\2\2\u0096\u009a\7"+
		"\34\2\2\u0097\u009a\5\16\b\2\u0098\u009a\5\30\r\2\u0099\u0096\3\2\2\2"+
		"\u0099\u0097\3\2\2\2\u0099\u0098\3\2\2\2\u009a\u009c\3\2\2\2\u009b\u0095"+
		"\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e"+
		"\u00ab\3\2\2\2\u009f\u00a6\7\34\2\2\u00a0\u00a4\7\b\2\2\u00a1\u00a5\7"+
		"\34\2\2\u00a2\u00a5\5\16\b\2\u00a3\u00a5\5\30\r\2\u00a4\u00a1\3\2\2\2"+
		"\u00a4\u00a2\3\2\2\2\u00a4\u00a3\3\2\2\2\u00a5\u00a7\3\2\2\2\u00a6\u00a0"+
		"\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00a6\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9"+
		"\u00ab\3\2\2\2\u00aa\u0089\3\2\2\2\u00aa\u0094\3\2\2\2\u00aa\u009f\3\2"+
		"\2\2\u00ab\27\3\2\2\2\u00ac\u00b6\7\16\2\2\u00ad\u00b6\7\20\2\2\u00ae"+
		"\u00b6\7\21\2\2\u00af\u00b6\7\27\2\2\u00b0\u00b6\7\30\2\2\u00b1\u00b6"+
		"\7\31\2\2\u00b2\u00b6\5\32\16\2\u00b3\u00b6\7\22\2\2\u00b4\u00b6\7\17"+
		"\2\2\u00b5\u00ac\3\2\2\2\u00b5\u00ad\3\2\2\2\u00b5\u00ae\3\2\2\2\u00b5"+
		"\u00af\3\2\2\2\u00b5\u00b0\3\2\2\2\u00b5\u00b1\3\2\2\2\u00b5\u00b2\3\2"+
		"\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b4\3\2\2\2\u00b6\31\3\2\2\2\u00b7\u00b9"+
		"\7\31\2\2\u00b8\u00b7\3\2\2\2\u00b9\u00ba\3\2\2\2\u00ba\u00b8\3\2\2\2"+
		"\u00ba\u00bb\3\2\2\2\u00bb\u00bc\3\2\2\2\u00bc\u00bd\t\4\2\2\u00bd\33"+
		"\3\2\2\2\35\37)\62\64:<@BKSU^fjpy\u0081\u0087\u008e\u0092\u0099\u009d"+
		"\u00a4\u00a8\u00aa\u00b5\u00ba";
	public static final String[] ruleNames = makeRuleNames();
	private static final String[] _LITERAL_NAMES = makeLiteralNames();

	private static String[] makeRuleNames() {
		return new String[] {
			"question", "line", "query", "bool", "negation", "token", "quotes", "doublequotes",
			"singlequotes", "prefixed", "relation", "term", "charged"
		};
	}

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'\"'", "'''", "':'", "'+'", "'-'", null, null, null, null, null,
			null, null, "'*'", null, null, null, null, null, null, null, null, null,
			null, "'('", "')'"
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

	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, "ARROW", "ARROWRIGHT", "ARROWLEFT",
			"ARROWBOTH", "IRI", "HASHTAG", "DASH", "WILDCARD", "NUM", "COMPOUND",
			"APOSTROPHE", "AND", "NOT", "OR", "DIGITS", "ALPHA", "ABBREV", "ALPHANUM",
			"LPAR", "RPAR", "SPECIAL", "NL", "WHITESPACE"
		};
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
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << WILDCARD) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
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

	public final QueryContext query() throws RecognitionException {
		QueryContext _localctx = new QueryContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_query);
		int _la;
		try {
			int _alt;
			setState(64);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,7,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(37);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(36);
					token();
					}
					}
					setState(39);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << WILDCARD) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << SPECIAL))) != 0) );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(41);
				match(LPAR);
				setState(42);
				query();
				setState(43);
				match(RPAR);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(60);
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(50);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,3,_ctx) ) {
					case 1:
						{
						setState(46);
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(45);
								token();
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(48);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					setState(52);
					bool(0);
					setState(58);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,5,_ctx) ) {
					case 1:
						{
						setState(54);
						_errHandler.sync(this);
						_alt = 1;
						do {
							switch (_alt) {
							case 1:
								{
								{
								setState(53);
								token();
								}
								}
								break;
							default:
								throw new NoViableAltException(this);
							}
							setState(56);
							_errHandler.sync(this);
							_alt = getInterpreter().adaptivePredict(_input,4,_ctx);
						} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
						}
						break;
					}
					}
					}
					setState(62);
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << WILDCARD) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
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
			setState(73);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case NOT:
				{
				setState(67);
				negation();
				}
				break;
			case T__0:
			case T__1:
			case IRI:
			case DASH:
			case WILDCARD:
			case NUM:
			case COMPOUND:
			case APOSTROPHE:
			case ALPHA:
			case ABBREV:
			case ALPHANUM:
			case SPECIAL:
				{
				setState(68);
				token();
				}
				break;
			case LPAR:
				{
				setState(69);
				match(LPAR);
				setState(70);
				query();
				setState(71);
				match(RPAR);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(83);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(81);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,9,_ctx) ) {
					case 1:
						{
						_localctx = new BoolContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bool);
						setState(75);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(76);
						match(AND);
						setState(77);
						bool(4);
						}
						break;
					case 2:
						{
						_localctx = new BoolContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_bool);
						setState(78);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(79);
						match(OR);
						setState(80);
						bool(3);
						}
						break;
					}
					} 
				}
				setState(85);
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

	public final NegationContext negation() throws RecognitionException {
		NegationContext _localctx = new NegationContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_negation);
		try {
			setState(92);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(86);
				match(NOT);
				setState(87);
				token();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(88);
				match(NOT);
				setState(89);
				bool(0);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(90);
				match(NOT);
				setState(91);
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

	public final TokenContext token() throws RecognitionException {
		TokenContext _localctx = new TokenContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_token);
		try {
			setState(100);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,12,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(94);
				relation();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(95);
				term();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(96);
				match(IRI);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(97);
				prefixed();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(98);
				match(SPECIAL);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(99);
				quotes();
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

	public final QuotesContext quotes() throws RecognitionException {
		QuotesContext _localctx = new QuotesContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_quotes);
		try {
			setState(104);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(102);
				doublequotes();
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(103);
				singlequotes();
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

	public final DoublequotesContext doublequotes() throws RecognitionException {
		DoublequotesContext _localctx = new DoublequotesContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_doublequotes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(106);
			match(T__0);
			setState(110);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__1) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << ARROW) | (1L << ARROWRIGHT) | (1L << ARROWLEFT) | (1L << ARROWBOTH) | (1L << IRI) | (1L << HASHTAG) | (1L << DASH) | (1L << WILDCARD) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << AND) | (1L << NOT) | (1L << OR) | (1L << DIGITS) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << RPAR) | (1L << SPECIAL) | (1L << NL) | (1L << WHITESPACE))) != 0)) {
				{
				{
				setState(107);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==T__0) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(112);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(113);
			match(T__0);
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

	public final SinglequotesContext singlequotes() throws RecognitionException {
		SinglequotesContext _localctx = new SinglequotesContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_singlequotes);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(115);
			match(T__1);
			setState(119);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << ARROW) | (1L << ARROWRIGHT) | (1L << ARROWLEFT) | (1L << ARROWBOTH) | (1L << IRI) | (1L << HASHTAG) | (1L << DASH) | (1L << WILDCARD) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << AND) | (1L << NOT) | (1L << OR) | (1L << DIGITS) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << RPAR) | (1L << SPECIAL) | (1L << NL) | (1L << WHITESPACE))) != 0)) {
				{
				{
				setState(116);
				_la = _input.LA(1);
				if ( _la <= 0 || (_la==T__1) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
				}
				setState(121);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(122);
			match(T__1);
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

	public final PrefixedContext prefixed() throws RecognitionException {
		PrefixedContext _localctx = new PrefixedContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_prefixed);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(124);
				match(ALPHA);
				}
				}
				setState(127);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHA );
			setState(129);
			match(T__2);
			setState(133);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPECIAL:
				{
				setState(130);
				match(SPECIAL);
				}
				break;
			case T__0:
			case T__1:
				{
				setState(131);
				quotes();
				}
				break;
			case DASH:
			case WILDCARD:
			case NUM:
			case COMPOUND:
			case APOSTROPHE:
			case ALPHA:
			case ABBREV:
			case ALPHANUM:
				{
				setState(132);
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

	public final RelationContext relation() throws RecognitionException {
		RelationContext _localctx = new RelationContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_relation);
		try {
			int _alt;
			setState(168);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(135);
				quotes();
				setState(142);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(136);
						match(ARROW);
						setState(140);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(137);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(138);
							quotes();
							}
							break;
						case DASH:
						case WILDCARD:
						case NUM:
						case COMPOUND:
						case APOSTROPHE:
						case ALPHA:
						case ABBREV:
						case ALPHANUM:
							{
							setState(139);
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
					setState(144);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case DASH:
			case WILDCARD:
			case NUM:
			case COMPOUND:
			case APOSTROPHE:
			case ALPHA:
			case ABBREV:
			case ALPHANUM:
				enterOuterAlt(_localctx, 2);
				{
				setState(146);
				term();
				setState(153);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(147);
						match(ARROW);
						setState(151);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(148);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(149);
							quotes();
							}
							break;
						case DASH:
						case WILDCARD:
						case NUM:
						case COMPOUND:
						case APOSTROPHE:
						case ALPHA:
						case ABBREV:
						case ALPHANUM:
							{
							setState(150);
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
					setState(155);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,21,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case SPECIAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(157);
				match(SPECIAL);
				setState(164);
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(158);
						match(ARROW);
						setState(162);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(159);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(160);
							quotes();
							}
							break;
						case DASH:
						case WILDCARD:
						case NUM:
						case COMPOUND:
						case APOSTROPHE:
						case ALPHA:
						case ABBREV:
						case ALPHANUM:
							{
							setState(161);
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
					setState(166);
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

	public final TermContext term() throws RecognitionException {
		TermContext _localctx = new TermContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_term);
		try {
			setState(179);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(170);
				match(DASH);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(171);
				match(NUM);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(172);
				match(COMPOUND);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(173);
				match(ALPHA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(174);
				match(ABBREV);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(175);
				match(ALPHANUM);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(176);
				charged();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(177);
				match(APOSTROPHE);
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(178);
				match(WILDCARD);
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

	public final ChargedContext charged() throws RecognitionException {
		ChargedContext _localctx = new ChargedContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_charged);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(182);
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(181);
				match(ALPHANUM);
				}
				}
				setState(184);
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHANUM );
			setState(186);
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

	public static class BoolContext extends ParserRuleContext {
		public BoolContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		public TokenContext token() {
			return getRuleContext(TokenContext.class,0);
		}

		public NegationContext negation() {
			return getRuleContext(NegationContext.class,0);
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

	public static class NegationContext extends ParserRuleContext {
		public NegationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

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

	public static class TokenContext extends ParserRuleContext {
		public TokenContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
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

		public QuotesContext quotes() {
			return getRuleContext(QuotesContext.class,0);
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

	public static class QuotesContext extends ParserRuleContext {
		public DoublequotesContext doublequotes() {
			return getRuleContext(DoublequotesContext.class,0);
		}
		public SinglequotesContext singlequotes() {
			return getRuleContext(SinglequotesContext.class,0);
		}
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

	public static class DoublequotesContext extends ParserRuleContext {
		public DoublequotesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_doublequotes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterDoublequotes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitDoublequotes(this);
		}
	}

	public static class SinglequotesContext extends ParserRuleContext {
		public SinglequotesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_singlequotes; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterSinglequotes(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitSinglequotes(this);
		}
	}

	public static class TermContext extends ParserRuleContext {
		public TermContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

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

		public TerminalNode WILDCARD() { return getToken(ScicopiaParser.WILDCARD, 0); }

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

	public static class ChargedContext extends ParserRuleContext {
		public ChargedContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}

		public List<TerminalNode> ALPHANUM() { return getTokens(ScicopiaParser.ALPHANUM); }

		public TerminalNode ALPHANUM(int i) {
			return getToken(ScicopiaParser.ALPHANUM, i);
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
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}