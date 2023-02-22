Write a flex program that reads a whitespace (space tab or newline) seperated 
list of Irish car registration numbers and outputs the number of years since 
registration followed by a new line for each one. 

If an invalid registration is detected the program should output the word 
"INVALID" followed by a newline character.


The current specification for number plates is the format YYY-CC-SSSSSS. 
Those issued from 1987 to 2012 had the format YY-CC-SSSSSS. 

The components are:
YYY - a 3-digit year (e.g. 131 for January to June 2013; 132 for July to December 2013) 
YY from 1987-2012 - a 2-digit year (e.g. 87 for 1987; 05 for 2005)
CC - a 1- or 2-character County/City identifier (e.g. L for Limerick City and County; SO for County Sligo).
SSSSSS - a 1- to 6-digit sequence number, starting with the first vehicle registered in the county/city that year/period.

2014-present
C 	Cork 	
CE 	Clare 	
CN 	Cavan 	
CW 	Carlow 	
D 	Dublin 
DL 	Donegal 
G 	Galway 	
KE 	Kildare 	
KK 	Kilkenny 	
KY 	Kerry 	
L 	Limerick 
LD 	Longford 
LH 	Louth 	
LM 	Leitrim 
LS 	Laois 	
MH 	Meath 
MN 	Monaghan 	
MO 	Mayo 
OY 	Offaly 	
RN 	Roscommon 	
SO 	Sligo 	
T 	Tipperary 	
W 	Waterford 	
WH 	Westmeath 	
WX 	Wexford 	
WW 	Wicklow 	
Differences 1987-2013
L 	Limerick City
LK 	County Limerick

TN 	North Tipperary
TS 	South Tipperary
T	INVALID

W       Waterford City
WD 	County Waterford

Note you can define sub-parts of regular expressions in the definitions section and then
use these in the rule:
NUMBER  [0-9]...
COUNTY  KK|... 
YEAR  [0-9]...
%%
{YEAR}{COUNTY}{NUMBER}   { do stuff... }
