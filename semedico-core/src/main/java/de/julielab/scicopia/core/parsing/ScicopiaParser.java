package de.julielab.scicopia.core.parsing;// Generated from /Users/faessler/Coding/git/semedico/semedico-core/src/main/resources/Scicopia.g4 by ANTLR 4.7.2
import de.julielab.scicopia.core.parsing.ScicopiaListener;
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
		AND=16, OR=17, NOT=18, DIGITS=19, ALPHA=20, ABBREV=21, ALPHANUM=22, LPAR=23, 
		RPAR=24, SPECIAL=25, WHITESPACE=26;
	public static final int
		RULE_phrase = 0, RULE_block = 1, RULE_part = 2, RULE_logical = 3, RULE_quotes = 4, 
		RULE_prefixed = 5, RULE_relation = 6, RULE_term = 7, RULE_charged = 8;
	private static String[] makeRuleNames() {
		return new String[] {
			"phrase", "block", "part", "logical", "quotes", "prefixed", "relation", 
			"term", "charged"
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
			"AND", "OR", "NOT", "DIGITS", "ALPHA", "ABBREV", "ALPHANUM", "LPAR", 
			"RPAR", "SPECIAL", "WHITESPACE"
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

	public static class PhraseContext extends ParserRuleContext {
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public PhraseContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_phrase; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener) ((ScicopiaListener)listener).enterPhrase(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitPhrase(this);
		}
	}

	public final PhraseContext phrase() throws RecognitionException {
		PhraseContext _localctx = new PhraseContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_phrase);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(19); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(18);
				block();
				}
				}
				setState(21); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
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

	public static class BlockContext extends ParserRuleContext {
		public List<PartContext> part() {
			return getRuleContexts(PartContext.class);
		}
		public PartContext part(int i) {
			return getRuleContext(PartContext.class,i);
		}
		public List<LogicalContext> logical() {
			return getRuleContexts(LogicalContext.class);
		}
		public LogicalContext logical(int i) {
			return getRuleContext(LogicalContext.class,i);
		}
		public TerminalNode LPAR() { return getToken(ScicopiaParser.LPAR, 0); }
		public TerminalNode RPAR() { return getToken(ScicopiaParser.RPAR, 0); }
		public TerminalNode AND() { return getToken(ScicopiaParser.AND, 0); }
		public TerminalNode OR() { return getToken(ScicopiaParser.OR, 0); }
		public List<BlockContext> block() {
			return getRuleContexts(BlockContext.class);
		}
		public BlockContext block(int i) {
			return getRuleContext(BlockContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitBlock(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_block);
		int _la;
		try {
			int _alt;
			setState(108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,14,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(24); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(23);
						part();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(26); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,1,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(29); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(28);
						logical(0);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(31); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(34); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(33);
					part();
					}
					}
					setState(36); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << SPECIAL))) != 0) );
				setState(38);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(39);
				match(LPAR);
				setState(41); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(40);
					block();
					}
					}
					setState(43); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				setState(45);
				match(RPAR);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(48); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(47);
					logical(0);
					}
					}
					setState(50); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << SPECIAL))) != 0) );
				setState(52);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(53);
				match(LPAR);
				setState(55); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(54);
					block();
					}
					}
					setState(57); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				setState(59);
				match(RPAR);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(61);
				match(LPAR);
				setState(63); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(62);
					block();
					}
					}
					setState(65); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				setState(67);
				match(RPAR);
				setState(68);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(70); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(69);
						part();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(72); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,8,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(74);
				match(LPAR);
				setState(76); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(75);
					block();
					}
					}
					setState(78); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				setState(80);
				match(RPAR);
				setState(81);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(83); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(82);
						logical(0);
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(85); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,10,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(87);
				match(LPAR);
				setState(89); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(88);
					block();
					}
					}
					setState(91); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				setState(93);
				match(RPAR);
				setState(94);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(96); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(95);
						block();
						}
						}
						break;
					default:
						throw new NoViableAltException(this);
					}
					setState(98); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(100);
				match(LPAR);
				setState(102); 
				_errHandler.sync(this);
				_la = _input.LA(1);
				do {
					{
					{
					setState(101);
					block();
					}
					}
					setState(104); 
					_errHandler.sync(this);
					_la = _input.LA(1);
				} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << IRI) | (1L << DASH) | (1L << NUM) | (1L << COMPOUND) | (1L << APOSTROPHE) | (1L << NOT) | (1L << ALPHA) | (1L << ABBREV) | (1L << ALPHANUM) | (1L << LPAR) | (1L << SPECIAL))) != 0) );
				setState(106);
				match(RPAR);
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
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterPart(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitPart(this);
		}
	}

	public final PartContext part() throws RecognitionException {
		PartContext _localctx = new PartContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_part);
		try {
			setState(116);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(110);
				quotes();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(111);
				relation();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(112);
				term();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(113);
				match(IRI);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(114);
				prefixed();
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(115);
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

	public static class LogicalContext extends ParserRuleContext {
		public TerminalNode NOT() { return getToken(ScicopiaParser.NOT, 0); }
		public List<PartContext> part() {
			return getRuleContexts(PartContext.class);
		}
		public PartContext part(int i) {
			return getRuleContext(PartContext.class,i);
		}
		public List<LogicalContext> logical() {
			return getRuleContexts(LogicalContext.class);
		}
		public LogicalContext logical(int i) {
			return getRuleContext(LogicalContext.class,i);
		}
		public TerminalNode AND() { return getToken(ScicopiaParser.AND, 0); }
		public TerminalNode OR() { return getToken(ScicopiaParser.OR, 0); }
		public LogicalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_logical; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).enterLogical(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ScicopiaListener ) ((ScicopiaListener)listener).exitLogical(this);
		}
	}

	public final LogicalContext logical() throws RecognitionException {
		return logical(0);
	}

	private LogicalContext logical(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		LogicalContext _localctx = new LogicalContext(_ctx, _parentState);
		LogicalContext _prevctx = _localctx;
		int _startState = 6;
		enterRecursionRule(_localctx, 6, RULE_logical, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(131);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,16,_ctx) ) {
			case 1:
				{
				setState(119);
				match(NOT);
				setState(120);
				part();
				}
				break;
			case 2:
				{
				setState(121);
				match(NOT);
				setState(122);
				logical(5);
				}
				break;
			case 3:
				{
				setState(123);
				part();
				setState(124);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(125);
				part();
				}
				break;
			case 4:
				{
				setState(127);
				part();
				setState(128);
				_la = _input.LA(1);
				if ( !(_la==AND || _la==OR) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(129);
				logical(3);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(141);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(139);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
					case 1:
						{
						_localctx = new LogicalContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_logical);
						setState(133);
						if (!(precpred(_ctx, 1))) throw new FailedPredicateException(this, "precpred(_ctx, 1)");
						setState(134);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==OR) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(135);
						logical(2);
						}
						break;
					case 2:
						{
						_localctx = new LogicalContext(_parentctx, _parentState);
						pushNewRecursionContext(_localctx, _startState, RULE_logical);
						setState(136);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(137);
						_la = _input.LA(1);
						if ( !(_la==AND || _la==OR) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(138);
						part();
						}
						break;
					}
					} 
				}
				setState(143);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,18,_ctx);
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
		enterRule(_localctx, 8, RULE_quotes);
		try {
			int _alt;
			setState(160);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
				enterOuterAlt(_localctx, 1);
				{
				setState(144);
				match(T__0);
				setState(148);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(145);
						matchWildcard();
						}
						} 
					}
					setState(150);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,19,_ctx);
				}
				setState(151);
				match(T__0);
				}
				break;
			case T__1:
				enterOuterAlt(_localctx, 2);
				{
				setState(152);
				match(T__1);
				setState(156);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				while ( _alt!=1 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
					if ( _alt==1+1 ) {
						{
						{
						setState(153);
						matchWildcard();
						}
						} 
					}
					setState(158);
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,20,_ctx);
				}
				setState(159);
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
		enterRule(_localctx, 10, RULE_prefixed);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(163); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(162);
				match(ALPHA);
				}
				}
				setState(165); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHA );
			setState(167);
			match(T__2);
			setState(171);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case SPECIAL:
				{
				setState(168);
				match(SPECIAL);
				}
				break;
			case T__0:
			case T__1:
				{
				setState(169);
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
				setState(170);
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
		enterRule(_localctx, 12, RULE_relation);
		try {
			int _alt;
			setState(206);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__0:
			case T__1:
				enterOuterAlt(_localctx, 1);
				{
				setState(173);
				quotes();
				setState(180); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(174);
						match(ARROW);
						setState(178);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(175);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(176);
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
							setState(177);
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
					setState(182); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,25,_ctx);
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
				setState(184);
				term();
				setState(191); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(185);
						match(ARROW);
						setState(189);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(186);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(187);
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
							setState(188);
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
					setState(193); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,27,_ctx);
				} while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER );
				}
				break;
			case SPECIAL:
				enterOuterAlt(_localctx, 3);
				{
				setState(195);
				match(SPECIAL);
				setState(202); 
				_errHandler.sync(this);
				_alt = 1;
				do {
					switch (_alt) {
					case 1:
						{
						{
						setState(196);
						match(ARROW);
						setState(200);
						_errHandler.sync(this);
						switch (_input.LA(1)) {
						case SPECIAL:
							{
							setState(197);
							match(SPECIAL);
							}
							break;
						case T__0:
						case T__1:
							{
							setState(198);
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
							setState(199);
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
					setState(204); 
					_errHandler.sync(this);
					_alt = getInterpreter().adaptivePredict(_input,29,_ctx);
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
		enterRule(_localctx, 14, RULE_term);
		try {
			setState(216);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,31,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(208);
				match(DASH);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(209);
				match(NUM);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(210);
				match(COMPOUND);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(211);
				match(ALPHA);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(212);
				match(ABBREV);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(213);
				match(ALPHANUM);
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(214);
				charged();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(215);
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
		enterRule(_localctx, 16, RULE_charged);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(218);
				match(ALPHANUM);
				}
				}
				setState(221); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( _la==ALPHANUM );
			setState(223);
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
			return logical_sempred((LogicalContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean logical_sempred(LogicalContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 1);
		case 1:
			return precpred(_ctx, 2);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\34\u00e4\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\6"+
		"\2\26\n\2\r\2\16\2\27\3\3\6\3\33\n\3\r\3\16\3\34\3\3\6\3 \n\3\r\3\16\3"+
		"!\3\3\6\3%\n\3\r\3\16\3&\3\3\3\3\3\3\6\3,\n\3\r\3\16\3-\3\3\3\3\3\3\6"+
		"\3\63\n\3\r\3\16\3\64\3\3\3\3\3\3\6\3:\n\3\r\3\16\3;\3\3\3\3\3\3\3\3\6"+
		"\3B\n\3\r\3\16\3C\3\3\3\3\3\3\6\3I\n\3\r\3\16\3J\3\3\3\3\6\3O\n\3\r\3"+
		"\16\3P\3\3\3\3\3\3\6\3V\n\3\r\3\16\3W\3\3\3\3\6\3\\\n\3\r\3\16\3]\3\3"+
		"\3\3\3\3\6\3c\n\3\r\3\16\3d\3\3\3\3\6\3i\n\3\r\3\16\3j\3\3\3\3\5\3o\n"+
		"\3\3\4\3\4\3\4\3\4\3\4\3\4\5\4w\n\4\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3\5\3"+
		"\5\3\5\3\5\3\5\3\5\5\5\u0086\n\5\3\5\3\5\3\5\3\5\3\5\3\5\7\5\u008e\n\5"+
		"\f\5\16\5\u0091\13\5\3\6\3\6\7\6\u0095\n\6\f\6\16\6\u0098\13\6\3\6\3\6"+
		"\3\6\7\6\u009d\n\6\f\6\16\6\u00a0\13\6\3\6\5\6\u00a3\n\6\3\7\6\7\u00a6"+
		"\n\7\r\7\16\7\u00a7\3\7\3\7\3\7\3\7\5\7\u00ae\n\7\3\b\3\b\3\b\3\b\3\b"+
		"\5\b\u00b5\n\b\6\b\u00b7\n\b\r\b\16\b\u00b8\3\b\3\b\3\b\3\b\3\b\5\b\u00c0"+
		"\n\b\6\b\u00c2\n\b\r\b\16\b\u00c3\3\b\3\b\3\b\3\b\3\b\5\b\u00cb\n\b\6"+
		"\b\u00cd\n\b\r\b\16\b\u00ce\5\b\u00d1\n\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t"+
		"\3\t\5\t\u00db\n\t\3\n\6\n\u00de\n\n\r\n\16\n\u00df\3\n\3\n\3\n\4\u0096"+
		"\u009e\3\b\13\2\4\6\b\n\f\16\20\22\2\4\3\2\22\23\3\2\6\7\2\u0112\2\25"+
		"\3\2\2\2\4n\3\2\2\2\6v\3\2\2\2\b\u0085\3\2\2\2\n\u00a2\3\2\2\2\f\u00a5"+
		"\3\2\2\2\16\u00d0\3\2\2\2\20\u00da\3\2\2\2\22\u00dd\3\2\2\2\24\26\5\4"+
		"\3\2\25\24\3\2\2\2\26\27\3\2\2\2\27\25\3\2\2\2\27\30\3\2\2\2\30\3\3\2"+
		"\2\2\31\33\5\6\4\2\32\31\3\2\2\2\33\34\3\2\2\2\34\32\3\2\2\2\34\35\3\2"+
		"\2\2\35o\3\2\2\2\36 \5\b\5\2\37\36\3\2\2\2 !\3\2\2\2!\37\3\2\2\2!\"\3"+
		"\2\2\2\"o\3\2\2\2#%\5\6\4\2$#\3\2\2\2%&\3\2\2\2&$\3\2\2\2&\'\3\2\2\2\'"+
		"(\3\2\2\2()\t\2\2\2)+\7\31\2\2*,\5\4\3\2+*\3\2\2\2,-\3\2\2\2-+\3\2\2\2"+
		"-.\3\2\2\2./\3\2\2\2/\60\7\32\2\2\60o\3\2\2\2\61\63\5\b\5\2\62\61\3\2"+
		"\2\2\63\64\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2\2\65\66\3\2\2\2\66\67\t\2"+
		"\2\2\679\7\31\2\28:\5\4\3\298\3\2\2\2:;\3\2\2\2;9\3\2\2\2;<\3\2\2\2<="+
		"\3\2\2\2=>\7\32\2\2>o\3\2\2\2?A\7\31\2\2@B\5\4\3\2A@\3\2\2\2BC\3\2\2\2"+
		"CA\3\2\2\2CD\3\2\2\2DE\3\2\2\2EF\7\32\2\2FH\t\2\2\2GI\5\6\4\2HG\3\2\2"+
		"\2IJ\3\2\2\2JH\3\2\2\2JK\3\2\2\2Ko\3\2\2\2LN\7\31\2\2MO\5\4\3\2NM\3\2"+
		"\2\2OP\3\2\2\2PN\3\2\2\2PQ\3\2\2\2QR\3\2\2\2RS\7\32\2\2SU\t\2\2\2TV\5"+
		"\b\5\2UT\3\2\2\2VW\3\2\2\2WU\3\2\2\2WX\3\2\2\2Xo\3\2\2\2Y[\7\31\2\2Z\\"+
		"\5\4\3\2[Z\3\2\2\2\\]\3\2\2\2][\3\2\2\2]^\3\2\2\2^_\3\2\2\2_`\7\32\2\2"+
		"`b\t\2\2\2ac\5\4\3\2ba\3\2\2\2cd\3\2\2\2db\3\2\2\2de\3\2\2\2eo\3\2\2\2"+
		"fh\7\31\2\2gi\5\4\3\2hg\3\2\2\2ij\3\2\2\2jh\3\2\2\2jk\3\2\2\2kl\3\2\2"+
		"\2lm\7\32\2\2mo\3\2\2\2n\32\3\2\2\2n\37\3\2\2\2n$\3\2\2\2n\62\3\2\2\2"+
		"n?\3\2\2\2nL\3\2\2\2nY\3\2\2\2nf\3\2\2\2o\5\3\2\2\2pw\5\n\6\2qw\5\16\b"+
		"\2rw\5\20\t\2sw\7\f\2\2tw\5\f\7\2uw\7\33\2\2vp\3\2\2\2vq\3\2\2\2vr\3\2"+
		"\2\2vs\3\2\2\2vt\3\2\2\2vu\3\2\2\2w\7\3\2\2\2xy\b\5\1\2yz\7\24\2\2z\u0086"+
		"\5\6\4\2{|\7\24\2\2|\u0086\5\b\5\7}~\5\6\4\2~\177\t\2\2\2\177\u0080\5"+
		"\6\4\2\u0080\u0086\3\2\2\2\u0081\u0082\5\6\4\2\u0082\u0083\t\2\2\2\u0083"+
		"\u0084\5\b\5\5\u0084\u0086\3\2\2\2\u0085x\3\2\2\2\u0085{\3\2\2\2\u0085"+
		"}\3\2\2\2\u0085\u0081\3\2\2\2\u0086\u008f\3\2\2\2\u0087\u0088\f\3\2\2"+
		"\u0088\u0089\t\2\2\2\u0089\u008e\5\b\5\4\u008a\u008b\f\4\2\2\u008b\u008c"+
		"\t\2\2\2\u008c\u008e\5\6\4\2\u008d\u0087\3\2\2\2\u008d\u008a\3\2\2\2\u008e"+
		"\u0091\3\2\2\2\u008f\u008d\3\2\2\2\u008f\u0090\3\2\2\2\u0090\t\3\2\2\2"+
		"\u0091\u008f\3\2\2\2\u0092\u0096\7\3\2\2\u0093\u0095\13\2\2\2\u0094\u0093"+
		"\3\2\2\2\u0095\u0098\3\2\2\2\u0096\u0097\3\2\2\2\u0096\u0094\3\2\2\2\u0097"+
		"\u0099\3\2\2\2\u0098\u0096\3\2\2\2\u0099\u00a3\7\3\2\2\u009a\u009e\7\4"+
		"\2\2\u009b\u009d\13\2\2\2\u009c\u009b\3\2\2\2\u009d\u00a0\3\2\2\2\u009e"+
		"\u009f\3\2\2\2\u009e\u009c\3\2\2\2\u009f\u00a1\3\2\2\2\u00a0\u009e\3\2"+
		"\2\2\u00a1\u00a3\7\4\2\2\u00a2\u0092\3\2\2\2\u00a2\u009a\3\2\2\2\u00a3"+
		"\13\3\2\2\2\u00a4\u00a6\7\26\2\2\u00a5\u00a4\3\2\2\2\u00a6\u00a7\3\2\2"+
		"\2\u00a7\u00a5\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a8\u00a9\3\2\2\2\u00a9\u00ad"+
		"\7\5\2\2\u00aa\u00ae\7\33\2\2\u00ab\u00ae\5\n\6\2\u00ac\u00ae\5\20\t\2"+
		"\u00ad\u00aa\3\2\2\2\u00ad\u00ab\3\2\2\2\u00ad\u00ac\3\2\2\2\u00ae\r\3"+
		"\2\2\2\u00af\u00b6\5\n\6\2\u00b0\u00b4\7\b\2\2\u00b1\u00b5\7\33\2\2\u00b2"+
		"\u00b5\5\n\6\2\u00b3\u00b5\5\20\t\2\u00b4\u00b1\3\2\2\2\u00b4\u00b2\3"+
		"\2\2\2\u00b4\u00b3\3\2\2\2\u00b5\u00b7\3\2\2\2\u00b6\u00b0\3\2\2\2\u00b7"+
		"\u00b8\3\2\2\2\u00b8\u00b6\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\u00d1\3\2"+
		"\2\2\u00ba\u00c1\5\20\t\2\u00bb\u00bf\7\b\2\2\u00bc\u00c0\7\33\2\2\u00bd"+
		"\u00c0\5\n\6\2\u00be\u00c0\5\20\t\2\u00bf\u00bc\3\2\2\2\u00bf\u00bd\3"+
		"\2\2\2\u00bf\u00be\3\2\2\2\u00c0\u00c2\3\2\2\2\u00c1\u00bb\3\2\2\2\u00c2"+
		"\u00c3\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c3\u00c4\3\2\2\2\u00c4\u00d1\3\2"+
		"\2\2\u00c5\u00cc\7\33\2\2\u00c6\u00ca\7\b\2\2\u00c7\u00cb\7\33\2\2\u00c8"+
		"\u00cb\5\n\6\2\u00c9\u00cb\5\20\t\2\u00ca\u00c7\3\2\2\2\u00ca\u00c8\3"+
		"\2\2\2\u00ca\u00c9\3\2\2\2\u00cb\u00cd\3\2\2\2\u00cc\u00c6\3\2\2\2\u00cd"+
		"\u00ce\3\2\2\2\u00ce\u00cc\3\2\2\2\u00ce\u00cf\3\2\2\2\u00cf\u00d1\3\2"+
		"\2\2\u00d0\u00af\3\2\2\2\u00d0\u00ba\3\2\2\2\u00d0\u00c5\3\2\2\2\u00d1"+
		"\17\3\2\2\2\u00d2\u00db\7\16\2\2\u00d3\u00db\7\17\2\2\u00d4\u00db\7\20"+
		"\2\2\u00d5\u00db\7\26\2\2\u00d6\u00db\7\27\2\2\u00d7\u00db\7\30\2\2\u00d8"+
		"\u00db\5\22\n\2\u00d9\u00db\7\21\2\2\u00da\u00d2\3\2\2\2\u00da\u00d3\3"+
		"\2\2\2\u00da\u00d4\3\2\2\2\u00da\u00d5\3\2\2\2\u00da\u00d6\3\2\2\2\u00da"+
		"\u00d7\3\2\2\2\u00da\u00d8\3\2\2\2\u00da\u00d9\3\2\2\2\u00db\21\3\2\2"+
		"\2\u00dc\u00de\7\30\2\2\u00dd\u00dc\3\2\2\2\u00de\u00df\3\2\2\2\u00df"+
		"\u00dd\3\2\2\2\u00df\u00e0\3\2\2\2\u00e0\u00e1\3\2\2\2\u00e1\u00e2\t\3"+
		"\2\2\u00e2\23\3\2\2\2#\27\34!&-\64;CJPW]djnv\u0085\u008d\u008f\u0096\u009e"+
		"\u00a2\u00a7\u00ad\u00b4\u00b8\u00bf\u00c3\u00ca\u00ce\u00d0\u00da\u00df";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}