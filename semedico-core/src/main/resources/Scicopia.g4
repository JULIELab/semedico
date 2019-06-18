grammar Scicopia;

phrase: block+ ;
block: ( part ) +
     | ( logical ) +
     | ( part ) + ( AND | OR ) LPAR block+ RPAR
     | ( logical ) + ( AND | OR ) LPAR block+ RPAR
     | LPAR block+ RPAR ( AND | OR ) ( part ) +
     | LPAR block+ RPAR ( AND | OR ) ( logical ) +
     | LPAR block+ RPAR ( AND | OR ) block+
     | LPAR block+ RPAR
     ;

part: quotes | relation | term | IRI | prefixed | SPECIAL ;

logical: NOT part
       | NOT logical
       | part ( AND | OR ) part
       | part ( AND | OR ) logical
       | logical ( AND | OR ) part
       | logical ( AND | OR ) logical
       ;

ARROW: ARROWRIGHT | ARROWLEFT | ARROWBOTH ;
ARROWRIGHT: '-'+ '>' ;
ARROWLEFT: '<' '-'+ ;
ARROWBOTH: '<' '-'+ '>' ;

quotes: '"' .*? '"'
      |  '\'' .*? '\''
      ;

IRI: ('http' | 'https') (':' | '%3A') ('/' | '%2F') ('/' | '%2F') ALPHANUM ('.' ALPHANUM)+ (('/' | '%2F') ALPHANUM)+ (FILEPCT ALPHANUM)* (('#' | '%23') ALPHANUM)?;

prefixed: ( ALPHA )+ ':' ( SPECIAL | quotes | term );

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
OR: 'Or' | 'or' | 'OR' | '|'+ ;
NOT: 'Not' | 'not' | 'NOT' | '!' ;

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

WHITESPACE: [ \r\n\t\f]+ -> skip;

