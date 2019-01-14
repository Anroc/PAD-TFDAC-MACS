grammar Policy;

TRUE : 'true';
FALSE : 'false';
AND : 'and';
OR : 'or';
LT : '<';
GT : '>';
EQ : '=';
NEQ : '!=';
LPAREN : '(';
RPAREN : ')';
COLON  : ':';
DOT    : '.';


policy: expression EOF;

expression : or_expression | (LPAREN or_expression RPAREN);

or_expression : and_expression (OR and_expression)*;

and_expression : attr_value_id | (LPAREN attr_value_id (AND attr_value_id)+ RPAREN);

attr_value_id: ID (DOT ID)* COLON atom;

atom : INT | FLOAT | STRING | TRUE | FALSE;

// currently not suppoerted
operator : LT | GT | EQ | NEQ;

INT : '0'..'9'+;
FLOAT : ('0'..'9')+ '.' ('0'..'9')+;
STRING : ('a'..'z'|'A'..'Z'|'_'|'@'|'.'|'-')+;
ID : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
WS : [ \t\r\n]+ -> skip ;
