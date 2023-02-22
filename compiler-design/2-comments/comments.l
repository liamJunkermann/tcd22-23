%{
%} 

%%
\"(.|\n)*\"     {printf("%s", yytext);}
"**".*     	    {} //ignore line of comments starting with **
"{"[^\}]*"}"    {} //ignore comments in curly braces
\}|\{	 	    {printf("syntax error\n"); return 0;}
%%

int main()
{
  yylex();
  return 0;
}