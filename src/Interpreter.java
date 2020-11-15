import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Interpreter {
    public static boolean DEBUG = false;
    private static final String setPlainText = "\033[0;0m";
    private static final String setBoldText = "\033[0;1m";
    private static boolean profanity;

    // state of interpreter
    static boolean immediate = true;

    // address of first pointer in the linked list that comprises the dictionary
    static int HERE = -1;

    // address of initial opcode, or main() for c programmers.
    // will be populated later
    static int ENTRY_POINT = -1;

    //input buffer
    static Scanner scan;
    // memory. Double array
    static List<Integer> memory = new ArrayList<>();
    // native java objects, extension of memory (get with negative addresses on stack)
    static List<Object> objects = new ArrayList<>();
    // starting point for native java methods and fields
    static Object nativeRoot = null;
    public static void setNativeRoot(Object o){nativeRoot = o;}
    // data stack
    static Stack stack = new Stack();
    // call stack for nested execution
    static Stack call_stack = new Stack();

    // link up names of primitives to their java code
    static HashMap<Integer, String> primitive_words = new HashMap<>();

    /**
     * Interpreter use only, do not confuse with Forth word
     * Appends a new a word definition stub to memory array (no instructions)
     */
    static void create(String name){
        // add link in linked list to prev word
        memory.add(HERE);
        // update head of linked list
        HERE = memory.size();
        // add name
        write_string(name, memory.size());
        // add immediate flag (default non immediate)
        memory.add(0);
    }

    /**
     * Interpreter use only. Sets the most recently defined word as immediate
     */
    static void set_immediate(){
        memory.set(HERE + memory.get(HERE), 1);
    }

    /**
     * Interpreter use only. Searches through dictionary
     * for a word that matches the name in the string
     * @return Address of word, if found. -1 if not found
     */
    static int search_word(String name)
    {
        var here = HERE;
        while(here != -1)
        {
            String word_name = read_string(here);
            if(name.equals(word_name))
                return here;
            //move 'here' back
            here = memory.get(here-1);
        }
        return -1;
    }

    /**
     * Convert word address to address of its immediate flag
     */
    public static int addressToFlag(int address)
    {
        return address + memory.get(address);
    }
    /**
     * Convert word address to address of its first instruction
     */
    public static int addressToOp(int address)
    {
        return address + memory.get(address) + 1;
    }


    /**
     * See other definition of declare primitive. Defaults
     * to non-immediate
     */
    static void declarePrimitive(String name)
    {
        declarePrimitive(name, false);
    }

    /**
     * Word only for interpreter use. Creates primitive word definition stubs in memory
     * @param name name of word
     * @param immediate Flags for immediate word
     */
    static void declarePrimitive(String name, boolean immediate)
    {   // add link in linked list
        create(name);

        // register word as primitive
        // allow the relevant java code to be found
        primitive_words.put(HERE, name);

        if(immediate)
            memory.set(addressToFlag(HERE), 1);

    }

    static {
        //allow primitive words to be found in dictionary
        declarePrimitive("new");
        declarePrimitive("/");
        declarePrimitive("fields");
        declarePrimitive("methods");
        declarePrimitive("seeobj");
        declarePrimitive("native");
        declarePrimitive("seestack");
        declarePrimitive("seemem");
        declarePrimitive("seerawmem");
        declarePrimitive("memposition");
        declarePrimitive("here");
        declarePrimitive("print");
        declarePrimitive("return", true);
        declarePrimitive("word");
        declarePrimitive("stack>mem");
        declarePrimitive("[", true);
        declarePrimitive("]");
        declarePrimitive("literal", true);
        declarePrimitive("read");
        declarePrimitive("donothing");
        declarePrimitive("set");
        declarePrimitive("+");
        declarePrimitive("-");
        declarePrimitive("*");
        declarePrimitive("=");
        declarePrimitive("dup");
        declarePrimitive("swap");
        declarePrimitive("drop");
        declarePrimitive("not");
        declarePrimitive("and");
        declarePrimitive("or");
        declarePrimitive("xor");
        declarePrimitive("branch");
        declarePrimitive("branch?");
        declarePrimitive("stringliteral");
        declarePrimitive("read-string");
        declarePrimitive("create");
        declarePrimitive("profanity");
        declarePrimitive("quit");

        create(":");
        memory.add(search_word("create"));
        memory.add(search_word("stringliteral"));
        memory.add(search_word("literal"));
        memory.add(0);
        memory.add(search_word("stack>mem"));
        memory.add(search_word("]"));
        memory.add(search_word("return"));

        create(";");
        memory.add(search_word("["));
        memory.add(search_word("literal"));
        memory.add(search_word("return"));
        memory.add(search_word("stack>mem"));
        memory.add(search_word("return"));
        set_immediate();

        ENTRY_POINT = memory.size();
        memory.add(search_word("donothing"));
        memory.add(search_word("return"));
    }

    public static void main(String[] args) {
        setNativeRoot(memory);

        try
        {   // run file start.f
            scan = new Scanner(new File("start.f"));
            System.out.println("Startup file found");
            repl();
        } catch (FileNotFoundException ignored) { }


        try
        {   // run file test.f
            scan = new Scanner(new File("test.f"));
            repl();
        } catch (FileNotFoundException ignored) { }

        // run from terminal input
        scan = new Scanner(System.in);
        repl();
    }

    public static void repl() {
        //DEBUG = true;

        /* All instructions must be stored in memory, even the
         * uncompiled immediate mode stuff. ENTRY_POINT is a pointer
         * to an integer of reserved space for instructions
         * executed directly from the input stream. At ENTRY_POINT + 1
         * is a return which will clear the pointer to ENTRY_POINT
         * after the instruction there executes
         */

        // set call stack to execute the reserved instruction address
        call_stack.add(ENTRY_POINT+1);

        // set the reserved address to nothing; the program will populate this
        // accordingly as it parses the input stream
        memory.set(ENTRY_POINT, search_word("donothing"));

        // set the instruction after to return
        memory.set(ENTRY_POINT+1, search_word("return"));

        while (true){
            var word_address = memory.get(call_stack.last());

            /* Instructions within immediate word during compile time should be executed,
             * but the design of the REPL loop compiles the instructions anyway
             * Check for when the call stack has 2 or more frames, then enforce immediate mode
             */

            // immediate words and immediate mode will cause the current instruction to be executed
            if(immediate || memory.get(addressToFlag(word_address)) == 1 || (call_stack.size()>=2))
            {   // primitive or forth word?
                if(primitive_words.containsKey(word_address))
                {
                    if(DEBUG)
                        System.out.print(" r::" + read_string(word_address));

                    // execute primitive
                        try {
                    primitive(primitive_words.get(word_address));
                        } catch (InvocationTargetException | ClassNotFoundException | NoSuchMethodException | InstantiationException e) {e.printStackTrace();} catch (IllegalAccessException e) { e.printStackTrace();}

                }else{
                    if(DEBUG)
                        System.out.print(" rf::" + read_string(word_address));

                    // execute forth word
                    call_stack.add(word_address + memory.get(word_address));
                }
            }else{
                if(DEBUG)
                    System.out.print(" c::" + read_string(word_address));

                // compile word
                memory.add(word_address);
            }

            // check for empty call stack, if so get the next instruction
            if (call_stack.size() == 0)
            {
                String next_word = nextInstruction();

                // end of input stream
                if(next_word == null) return;

                if(DEBUG)
                    System.out.println("\nNext word: " + next_word);

                // do not allow the call stack to be incremented since we just reset the call stack
                continue;
            }

            //advance code pointer
            call_stack.incrementLast();
        }
    }

    /**
     * executes the relevant primitive based on its name
     */
    static void primitive(String word) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        switch (word) {
            case "fields" -> ReflectionMachine.fields(getObject());
            case "methods" -> ReflectionMachine.methods(getObject());
            case "new" -> newOperator();
            case "native" -> addObject(nativeRoot);
            case "/" -> dotOperator();
            case "donothing" -> System.out.print("");
            case "print" -> System.out.println(stack.pop());
            case "return" -> call_stack.remove(call_stack.size() - 1);
            case "word" -> stack.add(search_word(scan.next()));
            case "stack>mem" -> memory.add(stack.pop());
            case "here" -> stack.add(HERE);
            case "[" -> immediate = true;
            case "]" -> immediate = false;
            case "seestack" -> {
                for (var tok : stack)
                    System.out.print(tok + " ");
                System.out.println("<-");
            }
            case "seeobj" -> {
                for (var tok : objects)
                    System.out.print(tok.getClass().getName() + " ");
                System.out.println("<-");
            }
            case "seemem" -> show_mem();
            case "seerawmem" -> System.out.println(memory);
            case "stringliteral" -> write_string(scan.next(), memory.size());
            case "read-string" -> System.out.println(read_string(stack.pop()));
            case "literal" -> {
                call_stack.incrementLast();
                stack.add(memory.get(call_stack.last()));
            }
            case "memposition" -> stack.add(memory.size());
            case "create" -> {
                memory.add(HERE);
                HERE = memory.size();
            }
            case "read" -> stack.add(memory.get(stack.pop()));
            case "set" -> { //value, address <-- top of stack
                int address = stack.pop();
                if (address == memory.size()) memory.add(stack.pop());
                else memory.set(address, stack.pop());
            }
            case "+" -> stack.add(stack.pop() + stack.pop());
            case "=" -> stack.add(stack.pop() == stack.pop() ? 1 : 0);
            case "-" -> stack.add(-stack.pop() + stack.pop());
            case "*" -> stack.add(stack.pop() * stack.pop());
            case "not" -> stack.add(stack.pop() == 0 ? 1 : 0);
            case "and" -> stack.add(stack.pop() & stack.pop());
            case "or" -> stack.add(stack.pop() | stack.pop());
            case "xor" -> stack.add(stack.pop() ^ stack.pop());
            case "swap" -> {
                int p = stack.pop();
                stack.add(stack.size() - 1, p);
            }
            case "dup" -> stack.add(stack.last());
            case "drop" -> stack.remove(stack.size() - 1);
            case "branch" -> //advance pointer by instruction at current pointer position
                    call_stack.set(call_stack.size() - 1, call_stack.last() + memory.get(call_stack.last() + 1));
            case "branch?" -> {
                if (stack.last() == 0) //advance pointer by instruction at current pointer position
                    call_stack.setLast(call_stack.last() + memory.get(call_stack.last() + 1));
                else //jump over the offset by 1
                    call_stack.incrementLast();
                stack.pop();
            }
            case "profanity" -> {
                profanity = true;
                System.out.println("You actually turned it on. Is this your kink, " + Aggressor.getOffensiveSlur() + "?");
            }
            case "quit" -> System.exit(0);
        }
    }
    static void addObject(Object o){
        objects.add(o);
        stack.add(-objects.size());
    }
    static Object getObject(){
        return objects.get(-stack.pop()-1);
    }

    static void newOperator() throws IllegalAccessException, InvocationTargetException, InstantiationException {
        String classname = scan.next();
        Constructor[] constructors;
            try {
        constructors = Class.forName(classname).getConstructors();
            } catch (ClassNotFoundException e){
        System.out.println(classname + " was not found. Fully qualified names required");
        return;
            }

        /*Class[] o = constructors[0].getParameterTypes();
        for(var i:o){
            System.out.println(i);
        }*/

        Object[] params = new Object[constructors[0].getParameterCount()];
        for(int i=0; i<params.length; i++){
            params[i] = stack.pop();
        }
        addObject(constructors[0].newInstance(params));
    }

    // (caller object '' name of attribute -- return value or address of returned object)
    static void dotOperator() throws InvocationTargetException, IllegalAccessException {
        // the calling object
        Object actor = getObject();
        // the name of the object's field or method
        String fieldOrClass = scan.next();
        // get actual type of attribute
        Object attribute = ReflectionMachine.getByString(actor, fieldOrClass);

        if (attribute == null) {
            System.out.println("field or class " + fieldOrClass + " not found as attribute");
        }

        // call a method and push return to stack
        else if(attribute instanceof Method){
            Method themethod = ((Method)attribute);

            // get parameters
            Object[] params = new Object[themethod.getParameterCount()];

            for(int i=0; i<params.length;i++){
                int stackElem = stack.pop();
                // stack has object address or integer?
                params[i] = (stackElem < 0)? objects.get(-stackElem-1):stackElem;
            }
            // invoke
            Object returnval = themethod.invoke(actor, params);

            // manage return as object or integer
            if(returnval instanceof Integer){
                stack.add((int)returnval);
            } else {// is object
                objects.add(returnval);
                stack.add(-objects.size());
            }

        }
        // get the value of field
        else if(attribute instanceof Field) {
            Field thefield = ((Field)attribute);
            if (thefield.getType() == int.class || thefield.getType() == Integer.class) {
                stack.add(thefield.getInt(actor));
            }
            else {//is object
                addObject(thefield.get(actor));
            }
        }
    }

    /**
     * Take next instruction from input stream and prepare it
     * for execution by placing the relevant opcode in memory
     * and reinitializing the call stack
     */
    static String nextInstruction()
    {
        // usually EOF when reading from file
        if(!scan.hasNext())
            return null;


        // get next token from input
        var next_Word = scan.next();

        // if it's a number, then deal with the number and skip to next
            try{
        int val = Integer.valueOf(next_Word);
        if(immediate) {
            stack.add(val);
        }else{
            memory.add(search_word("literal"));
            memory.add(val);
        }
        return nextInstruction();
            }catch(NumberFormatException e){}

        // reset call stack to execute from ENTRY_POINT
        call_stack.add(ENTRY_POINT);

        // find address of word identified by token
        var address = search_word(next_Word);

        // does address correspond to existing word?
        if(search_word(next_Word) == -1)
        {   // word not found
            System.out.print("word " + next_Word + " not found");
            if(profanity)
                System.out.print(". Try again, you " + Aggressor.getOffensiveSlur());
            System.out.println();

            //set empty instruction at ENTRY_POINT
            memory.set(ENTRY_POINT, search_word("donothing"));

        }else{
            // word found, set new instruction at ENTRY_POINT
            memory.set(ENTRY_POINT, address);
        }
        return next_Word;
    }

    /**
     * See the other definition of write_string
     * This defaults to writing to memory array
     */
    static void write_string(String name, int address)
    {
        write_string(name, address, memory);
    }

    /**
     * Writes a java object string to the provided array as
     * Unicode Points. First Integer is chars in String + 1
     * @param name The Java Object string
     * @param address The index of the first integer written
     * @param list The array to be written to
     */
    static void write_string(String name, int address, List<Integer> list)
    {
        byte[] b = name.getBytes();
        // length integer
        list.add(address, b.length+1);

        // write Unicode Codepoints
        for(int i = 0; i<b.length; i++){
            list.add(address+i+1, (int)b[i]);
        }
    }

    /**
     * Reads a list of Unicode Points, with the first integer
     * being the length of the string + 1
     * @param address address in the mem array of first integer
     * @return String in java object format
     */
    static String read_string(int address)
    {
        if(memory.get(address)-1<0)
            System.out.println("string length invalid: "+(memory.get(address)-1));

        // read length of string
        byte[] str = new byte[memory.get(address)-1];
        int offset = address+1;

        // read codepoints
        for(int i = 0; i<str.length; i++){
            str[i] = (byte)(int)memory.get(offset + i);
        }

        // convert codepoints to string object
        return new String(str);
    }

    /**
     * Scans the memory and prints each word, in order of
     * declaration, along with its definition
     * Ignores other non-word data, like variable values
     * Should only be used for debugging; assumptions made
     */
    static void show_mem()
    {   //make array of word addresses
        List<Integer> pointers = new ArrayList<>();

        // read and store word addresses
        var here = HERE;
        while(here != -1){
            pointers.add(here);
            //move to next word in linked list
            here = memory.get(here-1);
        }

        for(int i = pointers.size()-1;i>=0;i--)
        {
            var word_address = pointers.get(i);

            var word_name = read_string(word_address);
            var immediate = memory.get(word_address + memory.get(word_address));

            System.out.format("%-25s %d %s ", setBoldText + word_name + setPlainText, word_address, immediate==1?"immdt":"     ");

            // first opcode of the word
            var instruction_address = addressToOp(word_address);

            // hard stop for instructions
            // prevent infinite run-on if there is memory corruption
            int nextop = i==0? memory.size():pointers.get(i-1) - 1;

            // iterate through instructions
            for(int j=instruction_address; j<nextop; j++)
            {   // derive name from address
                var mem = memory.get(j);
                String name = read_string(mem);

                //stop iterating if return encountered
                if(name.equals("return"))
                    break;

                System.out.print(name + " ");

                // special condition if next element is read as literal
                if(name.equals("literal") || name.equals("branch") || name.equals("branch?"))
                {
                    j++;
                    System.out.print(memory.get(j) + " ");
                }
            }
            System.out.println("");
        }

        System.out.println();
    }

    /**
     * Basically an Integer Arraylist but with convenient methods
     */
    public static class Stack extends ArrayList<Integer> {
        public int pop(){
            return remove(size()-1);
        }

        public int last(){
            return get(size()-1);
        }

        public void incrementLast(){
            add(pop() + 1);
        }

        public void setLast(int val){
            set(size()-1, val);
        }
    }

}
