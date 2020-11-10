
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
        dup word =
    until
    drop
; immediate

( TODO: functionality to nest parentheses )

: constant ( initial_value '' constant_name -- )
    create              ( set up a new word )
    stringliteral
    [lit] 0 stack>mem

    postpone literal    ( add literal instruction to variable definition )
    stack>mem           ( append initial value to memory )
    postpone return     ( add return instruction to constant definition )
;

: variable ( initial_value '' variable_name -- )
    memposition         ( push memory address to stack )
    swap
    memposition set     ( append top of stack to memory )

    create              ( set up a new word )
    stringliteral
    [lit] 0 stack>mem

    postpone literal    ( add literal instruction to variable definition )
    stack>mem           ( append pointer to memory )
    postpone return     ( add return instruction to variable definition )
;

