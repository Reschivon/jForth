
: iftest if [lit] 22 print else [lit] 11 print then ;

: whiletest begin [lit] 11 print [lit] 1 until ;

: unlesstest unless [lit] 11 print else [lit] 22 print then ;

lit 0 unlesstest
lit 1 unlesstest

lit 0 iftest
lit 1 iftest

whiletest

: trojan-print postpone print ; immediate

: troy [lit] 22 trojan-print [lit] 11 print ;

troy

lit 22 print ( lit 55 print ) lit 11 print

( commentation! ) ( exciting! )

( cannot nest parentheses )

( You can enable aggressive error messages with the word 'profanity' )

lit 22 constant burgers
burgers print

lit 11 variable pies
pies read print

lit 22 pies set
pies read print
