package de.julielab.semedico.core.services.query;

/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 _       __                 _             __
| |     / /___ __________  (_)___  ____ _/ /
| | /| / / __ `/ ___/ __ \/ / __ \/ __ `/ / 
| |/ |/ / /_/ / /  / / / / / / / / /_/ /_/  
|__/|__/\__,_/_/  /_/ /_/_/_/ /_/\__, (_)   
                                /____/      

The following code is auto generated.
Please change the .lex file and run jflex if you want to change it!

Your find the .lex file at
/semedico-core/src/main/java/de/julielab/semedico/core/services/query/QueryTokenizerImpl.lex
It should be compiled into
/semedico-core/src/main/java/de/julielab/semedico/core/services/query/QueryTokenizerImpl.java

by running java -jar src/main/resources/JFlex.jar.
*/
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java_cup.runtime.Symbol;

%%

%class QueryTokenizerImpl
%public
%unicode
%pack
%char
%function getNextToken
%type Symbol

%{

/** 
sym is another auto generated class, created by CUP (parser generator)
*/
public static final int ALPHANUM          = 0;
public static final int APOSTROPHE        = 1;
public static final int NUM               = 6;
public static final int CJ                = 7;
public static final int PHRASE            = 14;
public static final int LEFT_PARENTHESIS  = 15;
public static final int RIGHT_PARENTHESIS = 16;
public static final int AND_ALPHANUM	  = 17;
public static final int AND_OPERATOR	  = 18;
public static final int OR_ALPHANUM		  = 19;
public static final int OR_OPERATOR		  = 20;
public static final int NOT_OPERATOR	  = 21;
@Deprecated
public static final int NOT_WS_OPERATOR	  = 22;
public static final int NOT_ALPHANUM 	  = 23;
public static final int RIGHT_PAREN_OR 	  = 24;
public static final int RIGHT_PAREN_AND	  = 25;
public static final int WILDCARD_TOKEN = 26;
public static final int DASH = 27;
public static final int ALPHANUM_EMBEDDED_PAR = 28;
public static final int UNARY_EVENT		  = -1;
public static final int BINARY_EVENT	  = -2;
public static final int UNARY_OR_BINARY_EVENT = -3;




public static final String [] TOKEN_TYPES = new String[] { "<ALPHANUM>",
			"<APOSTROPHE>", "<ACRONYM>", "<COMPANY>", "<EMAIL>", "<HOST>",
			"<NUM>", "<CJ>", "<ACRONYM_DEP>", "<SOUTHEAST_ASIAN>",
			"<IDEOGRAPHIC>", "<HIRAGANA>",
			"<KATAKANA>",
			"<HANGUL>",
			// SEMEDICO: added these token types
			"<PHRASE>", "<LEFT_PARENTHESIS>", "<RIGHT_PARENTHESIS>",
			"<AND_ALPHANUM>", "<AND_OPERATOR>", "<OR_ALPHANUM>",
			"<OR_OPERATOR>", "<NOT_OPERATOR>", "<NOT_WS_OPERATOR>",
			"<NOT_ALPHANUM>", "<WILDCARD_TOKEN>" };

public final int yychar()
{
    return yychar;
}

/**
 * Fills a Lucene token with the current token text.
 */
final public void getText(CharTermAttribute termAtt) {
	termAtt.setEmpty();
	termAtt.append(new String(zzBuffer), zzStartRead, zzMarkedPos);
}
%}


//*****************************************************************************
//**************************** TOKENIZER & TAGGER *****************************
//*****************************************************************************

//Parentheses
LEFT_PARENTHESIS  = "("
RIGHT_PARENTHESIS = ")"

//Negation
NOT_OPERATOR = {WHITESPACE}("-" | "!")({LETTER}|{DIGIT}{KOREAN}|{LEFT_PARENTHESIS})
NOT_ALPHANUM = ("Not"|"not"|"NOT")({WHITESPACE}|{LEFT_PARENTHESIS})

//AND and OR
AND_ALPHANUM = {WHITESPACE} ("And" | "and" | "AND" ) {WHITESPACE}
AND_OPERATOR_SINGLE = "&"
AND_OPERATOR = "&&"|{AND_OPERATOR_SINGLE}
OR_ALPHANUM  = {WHITESPACE} ("Or" | "or" | "OR") {WHITESPACE}
OR_OPERATOR_SINGLE  = "|"
OR_OPERATOR  = "||"|{OR_OPERATOR_SINGLE}
AND = ({AND_ALPHANUM} | {AND_OPERATOR})
OR  = ({OR_ALPHANUM} | {OR_OPERATOR})
RIGHT_PAREN_OR = {RIGHT_PARENTHESIS} ("Or" | "or" | "OR") {WHITESPACE}
RIGHT_PAREN_AND = {RIGHT_PARENTHESIS} ("And" | "and" | "AND" ) {WHITESPACE}


// basic word: a sequence of digits & letters
ALPHANUM   = ({LETTER}|{DIGIT}|{KOREAN})({LETTER}|{DIGIT}|{KOREAN})*

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+
// internal dashes for compounds; those overlap with NUM. We enforce that at least
// one word between dashes completely consists of letters because otherwise its more
// a numerical expression than a compound
DASH =  ( {HAS_LETTER} ("-" ({ALPHANUM}|{PAR_ALPHANUM}))+ )
      | ( ({ALPHANUM}|{PAR_ALPHANUM})+ ("-" ({ALPHANUM}|{PAR_ALPHANUM}))* "-" {HAS_LETTER} ("-" ({ALPHANUM}|{PAR_ALPHANUM}))* )

PHRASE_PART = ({AND}|{OR}|{ALPHANUM}|{APOSTROPHE}|{RIGHT_PARENTHESIS}|{LEFT_PARENTHESIS}|{DASH})
PHRASE 	= \"{PHRASE_PART} ({WHITESPACE} {PHRASE_PART})*\" 

// some expression enclosed in parenthesis
//PAR_ALPHANUM = ( ( "(" | "[" | "{" ) {ALPHANUM} ( ")" | "]" | "}" ) )
PAR_ALPHANUM = ( "(" {ALPHANUM}+ ")" )
             | ( "[" {ALPHANUM}+ "]" ) 
             | ( "{" {ALPHANUM}+ "}" )
             
ALPHANUM_EMBEDDED_PAR =  ( {ALPHANUM} {PAR_ALPHANUM}+ {ALPHANUM}* )
                       | ( {PAR_ALPHANUM} {ALPHANUM}+ )
                       
// floating point, serial, model numbers, ip addresses, etc.
// every other segment must have at least one digit
NUM        = ({ALPHANUM} {P} {HAS_DIGIT}
           | {HAS_DIGIT} {P} {ALPHANUM}
           | {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+
           | {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {ALPHANUM} {P} {HAS_DIGIT} ({P} {ALPHANUM} {P} {HAS_DIGIT})+
           | {HAS_DIGIT} {P} {ALPHANUM} ({P} {HAS_DIGIT} {P} {ALPHANUM})+)


// punctuation
P	         = ("_"|"-"|"/"|"."|",")


WILDCARD_TOKEN = ("*"|"?")

// at least one digit
HAS_DIGIT  =
    ({LETTER}|{DIGIT})*
    {DIGIT}
    ({LETTER}|{DIGIT})*
    
// at least one letter
HAS_LETTER  =
    ({LETTER}|{DIGIT})*
    {LETTER}
    ({LETTER}|{DIGIT})*

ALPHA      = ({LETTER})+


LETTER     = [\u0041-\u005a\u0061-\u007a\u00c0-\u00d6\u00d8-\u00f6\u00f8-\u00ff\u0100-\u1fff\uffa0-\uffdc]

DIGIT      = [\u0030-\u0039\u0660-\u0669\u06f0-\u06f9\u0966-\u096f\u09e6-\u09ef\u0a66-\u0a6f\u0ae6-\u0aef\u0b66-\u0b6f\u0be7-\u0bef\u0c66-\u0c6f\u0ce6-\u0cef\u0d66-\u0d6f\u0e50-\u0e59\u0ed0-\u0ed9\u1040-\u1049]

KOREAN     = [\uac00-\ud7af\u1100-\u11ff]

// Chinese, Japanese
CJ         = [\u3040-\u318f\u3100-\u312f\u3040-\u309F\u30A0-\u30FF\u31F0-\u31FF\u3300-\u337f\u3400-\u4dbf\u4e00-\u9fff\uf900-\ufaff\uff65-\uff9f]

WHITESPACE = \r\n | [ \r\n\t\f]


//*****************************************************************************
//***************************** Reaction on tokens ****************************
//*****************************************************************************


// this is a priority ranking: the nearer the top, the higher the priority
%%
{ALPHANUM}                                                     { return new Symbol(ALPHANUM, yytext()); }
{APOSTROPHE}                                                   { return new Symbol(APOSTROPHE, yytext()); }
{DASH}                                                         { return new Symbol(DASH, yytext()); }
{NUM}                                                          { return new Symbol(NUM, yytext()); }
{CJ}                                                           { return new Symbol(CJ, yytext()); }
{PHRASE}													   { return new Symbol(PHRASE, yytext()); }
{ALPHANUM_EMBEDDED_PAR}                                        { return new Symbol(ALPHANUM_EMBEDDED_PAR, yytext()); }
{LEFT_PARENTHESIS}											   { return new Symbol(LEFT_PARENTHESIS, yytext());}
{RIGHT_PARENTHESIS}											   { return new Symbol(RIGHT_PARENTHESIS, yytext()); }
{AND_OPERATOR}											       { return new Symbol(AND_OPERATOR, yytext()); }
{OR_OPERATOR}											       { return new Symbol(OR_OPERATOR, yytext()); }
{WILDCARD_TOKEN}										       { return new Symbol(WILDCARD_TOKEN, yytext()); }
//pushing whitespaces/parentheses/following letters back on the input stack
{AND_ALPHANUM}											       { yypushback(1); return new Symbol(AND_ALPHANUM, yytext().substring(1)); }
{OR_ALPHANUM}											       { yypushback(1); return new Symbol(OR_ALPHANUM, yytext().substring(1)); }
{NOT_ALPHANUM}										    	   { yypushback(1); return new Symbol(NOT_ALPHANUM, yytext()); }
{RIGHT_PAREN_OR}									    	   { yypushback(1); return new Symbol(RIGHT_PAREN_OR, yytext()); }
{RIGHT_PAREN_AND}									    	   { yypushback(1); return new Symbol(RIGHT_PAREN_AND, yytext()); }
// the NOT_OPERATOR has as condition that is must follow a whitespace; however,
// we don't want to return the matched whitespace itself. Thus we only return
// operator character be accessing the last matched character after
// we pushed back the following 
{NOT_OPERATOR}												   { yypushback(1); return new Symbol(NOT_OPERATOR, String.valueOf(yycharat(yylength()-1))); }

/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
