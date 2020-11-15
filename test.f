
: iftest if 22 print else 11 print then ;

: whiletest begin 11 print 1 until ;

: unlesstest unless 11 print else 22 print then ;

 0 unlesstest
 1 unlesstest

 0 iftest
 1 iftest

whiletest

: trojan-print postpone print ; immediate

: troy 22 trojan-print 11 print ;

troy

 22 print ( 55 print ) 11 print

( commentation! ) ( exciting! )

( cannot nest parentheses )

( You can enable aggressive error messages with the word 'profanity' )

 22 constant burgers
burgers print

 11 variable pies
pies read print

 22 pies set
pies read print

new testNative = canvas
30 30 200 200 new bluerect = rect

rect canvas / addthing
