# JForth
A Forth implementation in Java that sticks to the elegance of a bare metal Forth interpreter as much as possible.

Also useful as a reference for programmers from higher-level languages learning the in & outs of the Forth interpreter. (If you are brand-new to Forth, [read up](http://galileo.phys.virginia.edu/classes/551.jvn.fall01/primer.htm) on the syntax before getting into the implementation)


## Motivation
The need to speed up the development process of FIRST Tech Challenge robotics code. Deploying a single edit requires 20s of build time, manual download thru a cable, repositioning the robot, and restarting the OpMode. 
However with Forth this can be reduced to nil.
- Wirelessly send Forth commands to interpreter running on robot (no touch!)
- No need to restart OpMode
- No need to build -- system maintains state between edits
To interoperate with the robot's Java code this interpreter must be in Java.

### Interpreter Info
To imitate the "genuine" experience of making a Forth on bare metal (which is where its elegance really shines) I've forgone the fancy data structures/libraries of Java.
Because mostly high-level control code will be written in Forth, it's OK to sacrifice speed for elegance. The compute-intensive portions are in Java and C++

The memory is one big integer array -- strings are stored here as Unicode characters, not String objects.
I have a HashMap serving as lookup table for primitive Java words; this is the only complicated Java library I use.

Here is the structure of one word in memory

    +-------------------+-----------+----------------- - - - - +-------------- - - - -
    | POINTER TO        | LENGTH OF | NAME CHARACTERS          | ADDRESSES OF 
    | PREVIOUS WORD	    | NAME      |     	                   | INSTRUCTIONS
    +--- Integer -------+- Integer -+- n Integers as - - - - - +-------------- - - - -
                               ^         Unicode Points
                     next pointer points here
                     
Pretty much everything here is heavily inspired by JonesForth

### Code Info
Interpreter2.java is the actual code. I'll make an effort to comment it up soon.

`start.f` is the Forth code read by the interpreter on startup when `exec` is provided with a Scanner initialized to the file

Interpreter.java is an older iteration of the interpreter. You can see that I'm over reliant on the Java type system and implement words directly in Java without understanding compiling words or memory. I suspect this is a common issue for all C-derivative programmers doing low-level for the first time. But what a learning experience this was!

There is an even earlier iteration that I made impulsively in the dark of night -- an even more naive python interpreter with words like IF that worked by scanning the source file for THEN . . .


## Credits
- [JonesForth](https://github.com/nornagon/jonesforth/blob/master/jonesforth.f) for the excellent step-by-step commentary of building an interpreter in assembly
- [eForth](http://www.exemark.com/FORTH/eForthOverviewv5.pdf) for the elegant, minimalistic primitive word set and source code + explanation of high level forth words
- [StackExchange](https://softwareengineering.stackexchange.com/questions/339283/forth-how-do-create-and-does-work-exactly) article on CREATE and DOES> which got me curious about the Forth interpreter in the first place
- Wise men from the Silicon Valley Forth Interest Group
- More wise men from the Forth2020 Users group

