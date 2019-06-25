grammar Scicopia;

question
@init {System.out.println("Question last update 1213");}
    :   line+ EOF
    ;

line
    :   query NL
    ;

query
    : tokensequence
    | ((tokensequence | binaryBoolean)* negation (tokensequence | binaryBoolean)*)+
    | (tokensequence* binaryBoolean tokensequence*)+
    | parenQuery
    ;

tokensequence
    : token+
    ;

parenQuery: LPAR query RPAR;

binaryBoolean
    : binaryBoolean AND binaryBoolean
    | binaryBoolean OR binaryBoolean
    | operand
    ;

operand
    : token
    | parenQuery
    | negation
    ;

negation
    : NOT token
    | NOT LPAR query RPAR
    | NOT negation
    ;

token: quotes | relation | term | IRI | prefixed | SPECIAL;

ARROW: ARROWRIGHT | ARROWLEFT | ARROWBOTH ;
ARROWRIGHT: '-'+ '>' ;
ARROWLEFT: '<' '-'+ ;
ARROWBOTH: '<' '-'+ '>' ;

quotes: '"' .*? '"'
      |  '\'' .*? '\''
      ;

IRI: ('http' | 'https') (':' | '%3A') ('/' | '%2F') ('/' | '%2F') ALPHANUM ('.' ALPHANUM)+ (('/' | '%2F') ALPHANUM)+ (FILEPCT ALPHANUM)* (('#' | '%23') ALPHANUM)?;

prefixed: ( ALPHA )+ ':' ( SPECIAL | quotes | term );

HASHTAG: '#' ALPHANUM;

relation: quotes ( ARROW ( SPECIAL | quotes | term ) )+
        | term ( ARROW ( SPECIAL | quotes | term ) )+
        | SPECIAL ( ARROW ( SPECIAL | quotes | term ) )+
        ;

term: DASH | NUM | COMPOUND | ALPHA | ABBREV | ALPHANUM | charged | APOSTROPHE ;

// internal dashes for compound words
DASH:  ALPHA ('-' ALPHANUM )+
    |  ( ALPHANUM '-' )+ ALPHA ('-' ALPHANUM )*
    ;

// floating point, serial, model numbers, ip addresses, IUPAC names etc.
NUM: ALPHANUM ( PCT ALPHANUM )+ ;

// terms like (E)-2-epi-beta-Caryophyllene or cis-Muurola-4(14),5-diene
COMPOUND: LPAR ( '-' | '+' | 'E' | 'S' ) RPAR ('-' ( ALPHA | DIGITS ) )+
        | ( ALPHA | DIGITS ) ('-' ( ALPHA | DIGITS (',' DIGITS )* ) | LPAR DIGITS RPAR (',' DIGITS )* )+
        ;

// internal apostrophes: O'Reilly, you're, O'Reilly's
APOSTROPHE: ALPHA ('\'' ALPHA)+ ;

AND: 'And' | 'and' | 'AND' | ('&')+ ;
NOT: 'Not' | 'not' | 'NOT' | '!' ;
OR: 'Or' | 'or' | 'OR' | '|'+ ;

DIGITS:   ( DIGIT )+ ;
ALPHA:    ( LETTER )+ ;
ABBREV:   ( LETTER )+ '.' ;
charged:  ( ALPHANUM )+ ( '+' | '-' ) ;
ALPHANUM: ( LETTER | DIGIT )+ ;

LPAR: '(' ;
RPAR: ')' ;

SPECIAL: '\u2328' DIGITS '\u2328' ;

fragment LETTER:   [\p{L}] [\p{M}]* ;
fragment DIGIT:    [\p{Nd}] ;
fragment FILEPCT:  '_' | '-' | '.' | ',' ;
fragment PCT:      '_' | '-' | '/' | '.' | ',' ;

NL        : [\r\n] ;
WHITESPACE: [ \t\f]+ -> skip;

