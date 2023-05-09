%%
"+"     { printf("PLUS\n"); }
"-"     { printf("MINUS\n"); }
":="    { printf("ASSIGN\n"); }
[a-z]   { printf("ID %s\n", yytext); }
\n      { printf("EOL\n"); }
%%