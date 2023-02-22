%{
int evenNums = 0;
%}

%%

[24680]+[\40|\n]	{ evenNums++; }
[.]+[24680][\40|\n]	{ evenNums++; }
\n			{ }
.			{ }
%%

int main()
{
  yylex();
  printf("%d\n", evenNums);
  return 0;
}
