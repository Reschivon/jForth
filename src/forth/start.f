: double [lit] 2 print ;

: [compile] word stack>mem ; immediate

: words seemem ;

: [word] word ; immediate

: ll [word] literal [word] literal stack>mem stack>mem ; immediate
: token>stack ll stack>mem word stack>mem ; immediate

: if
    token>stack branch? stack>mem
    memposition
    [lit] 0 stack>mem
; immediate

: unless token>stack not stack>mem [compile] if ; immediate

: then
    dup
    memposition swap -
    swap set
; immediate

: else
	token>stack branch stack>mem
	memposition
	[lit] 0 stack>mem
	swap
	dup
	memposition swap -
	swap set
; immediate


: begin
	memposition
; immediate

: until
	token>stack branch? stack>mem
	memposition -
	stack>mem
; immediate

: again
	token>stack branch stack>mem
	memposition -
	stack>mem
; immediate

: while
	token>stack branch? stack>mem
	memposition
	[lit] 0 stack>mem
; immediate

: repeat
	token>stack branch stack>mem
	swap
	memposition - stack>mem
	dup
	memposition swap -
	swap set
; immediate


: iftest if [lit] 22 print else [lit] 11 print then ;

: whiletest begin [lit] 11 print [lit] 1 until ;

: unlesstest unless [lit] 11 print else [lit] 22 print then ;

lit 0 unlesstest
lit 1 unlesstest

lit 0 iftest
lit 1 iftest

whiletest

