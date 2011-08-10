package de.julielab.lucene;

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
*/
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import java_cup.runtime.Symbol;

%%

%class QueryTokenizerImpl
%unicode
%pack
%char
%function getNextToken
%type Symbol

%{

/** 
sym is another auto generated class, created by CUP (parser generator)
*/
public static final int ALPHANUM          = QueryTokenizer.ALPHANUM;
public static final int APOSTROPHE        = QueryTokenizer.APOSTROPHE;
public static final int NUM               = QueryTokenizer.NUM;
public static final int CJ                = QueryTokenizer.CJ;
public static final int PHRASE            = QueryTokenizer.PHRASE;
public static final int LEFT_PARENTHESIS  = QueryTokenizer.LEFT_PARENTHESIS;
public static final int RIGHT_PARENTHESIS = QueryTokenizer.RIGHT_PARENTHESIS;
public static final int AND		  		  = QueryTokenizer.AND;
public static final int OR		  		  = QueryTokenizer.OR;


public static final String [] TOKEN_TYPES = QueryTokenizer.TOKEN_TYPES;

public final int yychar()
{
    return yychar;
}

/**
 * Fills a Lucene token with the current token text.
 */
final void getText(CharTermAttribute termAtt) {
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

//AND and OR
AND = ("And"|"and"|"AND"|"&"+){WHITESPACE}
OR  = ("And"|"and"|"OR"|"|"+){WHITESPACE}

//edge cases
PAR_OR  = ( ")"( "OR" | "|"+ ))
PAR_AND = ( ")" ( "AND" | "&"+ ))
OR_PAR  = (( "OR" | "|"+ ) "("  )
AND_PAR = (( "AND"| "&"+ ) "("  )



// basic word: a sequence of digits & letters
ALPHANUM   = ({LETTER}|{DIGIT}|{KOREAN}|"-")+

// internal apostrophes: O'Reilly, you're, O'Reilly's
// use a post-filter to remove possessives
APOSTROPHE =  {ALPHA} ("'" {ALPHA})+


PHRASE_PART = ({AND}|{OR}|{ALPHANUM}|{APOSTROPHE}|{RIGHT_PARENTHESIS}|{LEFT_PARENTHESIS})
PHRASE 	= "\""{PHRASE_PART} ({WHITESPACE} {PHRASE_PART})*"\"" 


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

// at least one digit
HAS_DIGIT  =
    ({LETTER}|{DIGIT})*
    {DIGIT}
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

%%
{ALPHANUM}                                                     { return new Symbol(ALPHANUM, yytext()); }
{APOSTROPHE}                                                   { return new Symbol(APOSTROPHE, yytext()); }
{NUM}                                                          { return new Symbol(NUM, yytext()); }
{CJ}                                                           { return new Symbol(CJ, yytext()); }
{PHRASE}													   { return new Symbol(PHRASE, yytext()); }
{LEFT_PARENTHESIS}											   { return new Symbol(LEFT_PARENTHESIS); }
{RIGHT_PARENTHESIS}											   { return new Symbol(RIGHT_PARENTHESIS); }
//pushing stuff back on the input stack
{AND}													       {yypushback(1); return new Symbol(AND); }
{OR}													       {yypushback(1); return new Symbol(OR); }
{PAR_OR}													   {yypushback(2); return new Symbol(RIGHT_PARENTHESIS);}
{PAR_AND}													   {yypushback(3); return new Symbol(RIGHT_PARENTHESIS);}
{OR_PAR}													   {yypushback(1); return new Symbol(OR);}
{AND_PAR}													   {yypushback(1); return new Symbol(AND);}

/** Ignore the rest */
. | {WHITESPACE}                                               { /* ignore */ }
