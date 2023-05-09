%{ 
#  include <stdio.h>
#  include <stdlib.h>
void yyerror(char *s);
int yylex();
int yyparse();
%}

%output "sample.tab.c"

%token PLUS MINUS ASSIGN ID
%token <num> ID
%%

$accept: S $end;
S: stmt EOL
;
stmt: ID ASSIGN expr
;
expr: expr PLUS ID
 | expr MINUS ID
 | ID
;

%%

int main() {
    yyparse();
    return 0;
}

void yyerror