%{
#include "roman.tab.h"
#include <string.h>
void yyerror(char *s);
%}

ERR_VALS (([^IVXLCDM\n]*)|(I[^I\n]+I)|(V[^V(IV)\n]+V)|(X[^X(IX)\n]+X)|(L[^L(XL)\n]+L)|(C[^C(XC)\n]+C)|(D[^D(CD)\n]+D)|(M[^M(CM)\n]+M))
    /* this absolute monstrosity ^^ ensures that all chars which are not roman numerals are caught (the first bit), and ensures that invalid strings eg CMCC or IXII or XCXX are caught (the second, far less legible bit) */
%%
IV      {yylval.num = 4; return NUMBER;}
IX      {yylval.num = 9; return NUMBER;}
XL      {yylval.num = 40; return NUMBER;}
XC      {yylval.num = 90; return NUMBER;}
CD      {yylval.num = 400; return NUMBER;}
CM      {yylval.num = 900; return NUMBER;}
I{1,3}  {yylval.num = strlen(yytext); return NUMBER;}
V       {yylval.num = 5; return NUMBER;}
X       {yylval.num = 10; return NUMBER;}
L       {yylval.num = 50; return NUMBER;}
C       {yylval.num = 100; return NUMBER;}
D       {yylval.num = 500; return NUMBER;}
M       {yylval.num = 1000; return NUMBER;}
\n      {return EOL;}
{ERR_VALS} {
            printf("syntax error\n");
            exit(0);
            }
%%