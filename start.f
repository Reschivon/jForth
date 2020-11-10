
: immediate
    here read
    here +
    [lit] 1 swap
    set
;

: [compile]
    word stack>mem
; immediate

: words seemem ;

: [word] word ; immediate

: ll [word] literal [word] literal stack>mem stack>mem ; immediate

: token>stack ll stack>mem word stack>mem ; immediate

: postpone
    [compile] token>stack
    token>stack stack>mem stack>mem
; immediate

: if
    token>stack branch? stack>mem
    memposition
    [lit] 0 stack>mem
; immediate

: unless
    postpone not
    [compile] if
; immediate

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

: ) ;

: (
    [compile] literal [word] ) [ stack>mem ]
    begin
        dup
        word
        =
    until
    drop
; immediate

: constant
    create
    stringliteral
    [lit] 0 stack>mem

    postpone literal
    stack>mem
    postpone return
;

: variable
    memposition         ( push address of memory to stack )
    swap
    memposition set     ( append top of stack to memory )

    create              ( set up a new word )
    stringliteral
    [lit] 0 stack>mem

    postpone literal    ( add literal instruction to variable definition )
    stack>mem           ( append pointer to memory )
    postpone return     ( add return instruction to variable definition )
;

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

lit 22 constant burgers
burgers print

lit 11 variable pies
pies read print

lit 22 pies set
pies read print

