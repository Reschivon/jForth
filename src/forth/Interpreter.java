package forth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Interpreter {
    public static boolean DEBUG = false;
    private static final String setPlainText = "\033[0;0m";
    private static final String setBoldText = "\033[0;1m";
    private static boolean profanity;

    static boolean immediate = true;
    //address of first pointer in the linked list that comprises the dictionary
    static int HERE = -1;
    //Address of initial opcode, or main() for c programmers. Input is written to here
    static int ENTRY_POINT = -1;

    static List<Integer> memory = new ArrayList<>();
    static Stack stack = new Stack();
    static Stack call_stack = new Stack();

    //address and name
    static HashMap<Integer, String> primitive_words = new HashMap<>();

    static void create(String name){
        //pointer to prev
        memory.add(HERE);
        HERE = memory.size();
        //name
        write_string(name, memory.size());
        //immediate?
        memory.add(0);
    }

    static void set_immediate(){
        memory.set(HERE + memory.get(HERE), 1);
    }

    static int search_word(String name){
        int here = HERE;
        while(here != -1){
            String word_name = read_string(here);
            if(name.equals(word_name)) return here;
            //move 'here' back
            here = memory.get(here-1);
        }
        return -1;
    }

    public static int addressToOp(int address){
        return address + memory.get(address);
    }

    static void declarePrimitive(String name){
        declarePrimitive(name, false);
    }

    static void declarePrimitive(String name, boolean immediate){
        create(name);
        primitive_words.put(HERE, name);
        if(immediate) memory.set(addressToOp(HERE), 1);
    }


    public static void main(String[] args) {
        //set up primitives
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
        declarePrimitive("lit");
        declarePrimitive("[lit]", true);
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


        try (Scanner scan = new Scanner(new File("start.f"))){
            System.out.println("Startup file found");
            repl(scan);
        } catch (FileNotFoundException ignored) {}

        try (Scanner scan = new Scanner(new File("test.f"))){
            repl(scan);
        } catch (FileNotFoundException ignored) {}

        repl(new Scanner(System.in));
    }

    public static void repl(Scanner scan) {
        //DEBUG = true;

        call_stack.add(ENTRY_POINT+1);
        memory.set(ENTRY_POINT, search_word("donothing"));
        memory.set(ENTRY_POINT+1, search_word("return"));

        while (true){
            int word_address = memory.get(call_stack.last());

            // Instructions within immediate word during compile time should be executed
            // This will result in call stack having 2 or more frames, so check for this
            if(immediate || memory.get(addressToOp(word_address)) == 1 || (call_stack.size()>=2)) {
                // execute word
                if (primitive_words.containsKey(word_address)) {

                    if(DEBUG) System.out.print(" r::" + read_string(word_address));

                    //execute primitive
                    switch (primitive_words.get(word_address)) {
                        case "donothing" -> System.out.print("");
                        case "print" -> System.out.println(stack.pop());
                        case "return" -> call_stack.remove(call_stack.size() - 1);
                        case "word" -> stack.add(search_word(scan.next()));
                        case "stack>mem" -> memory.add(stack.pop());
                        case "here" -> stack.add(HERE);
                        case "[" -> immediate = true;
                        case "]" -> immediate = false;
                        case "seestack" -> {
                            for (var tok : stack) System.out.print(tok + " ");
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
                        case "lit" -> stack.add(Integer.valueOf(scan.next()));
                        case "[lit]" -> {
                            memory.add(search_word("literal"));
                            memory.add(Integer.valueOf(scan.next()));
                        }
                        case "memposition" -> stack.add(memory.size());
                        case "create" -> {memory.add(HERE); HERE = memory.size();}
                        case "read" -> stack.add(memory.get(stack.pop()));
                        case "set" -> { //value, address <-- top of stack
                            int address = stack.pop();
                            if(address == memory.size()) memory.add(stack.pop());
                            else memory.set(address, stack.pop());
                        }
                        case "+" -> stack.add(stack.pop() + stack.pop());
                        case "=" -> {
                            stack.add(stack.pop() == stack.pop()? 1:0);
                        }
                        case "-" -> stack.add(-stack.pop() + stack.pop());
                        case "*" -> stack.add(stack.pop() * stack.pop());
                        case "not" -> stack.add(stack.pop()==0?1:0);
                        case "and" -> stack.add(stack.pop() & stack.pop());
                        case "or" -> stack.add(stack.pop() | stack.pop());
                        case "xor" -> stack.add(stack.pop() ^ stack.pop());
                        case "swap" -> {int p = stack.pop(); stack.add(stack.size()-1, p);}
                        case "dup" -> stack.add(stack.last());
                        case "drop" -> stack.remove(stack.size()-1);
                        case "branch" -> //advance pointer by instruction at current pointer position
                                call_stack.set(call_stack.size()-1, call_stack.last() + memory.get(call_stack.last() + 1));
                        case "branch?" -> {
                            if(stack.last() == 0) //advance pointer by instruction at current pointer position
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
                } else {

                    if(DEBUG)System.out.print(" rf::" + read_string(word_address));

                    //execute forth word
                    call_stack.add(word_address + memory.get(word_address));
                }
            }else{
                if(DEBUG)System.out.print(" c::" + read_string(word_address));

                //compile word
                memory.add(word_address);
            }

            // Check for empty call stack, indicates that the current word in the input stream
            // has been dealt with
            if (call_stack.size() == 0){
                // usually EOF when reading from file
                if(!scan.hasNext()) break;

                //restart from entry point
                call_stack.add(ENTRY_POINT);

                //get next token from input
                String nextWord = scan.next();

                if(DEBUG)System.out.println("\nPProgram pointer: " +nextWord);

                //see if word is valid
                int address = search_word(nextWord);
                if(search_word(nextWord) == -1){
                    System.out.print("word " + nextWord + " not found");
                    if(profanity) System.out.print("Try again, you " + Aggressor.getOffensiveSlur());
                    System.out.println();

                    //set empty instruction as entry
                    memory.set(ENTRY_POINT, search_word("donothing"));
                }else {
                    //set new instruction as entry
                    memory.set(ENTRY_POINT, address);
                }
                continue;
            }

            //advance code pointer
            call_stack.incrementLast();
        }
    }

    static void write_string(String name, int address){
        write_string(name, address, memory);
    }static void write_string(String name, int address, List<Integer> list){
        byte[] b = name.getBytes();
        list.add(address, b.length+1);
        for(int i = 0; i<b.length; i++){
            list.add(address+i+1, (int)b[i]);
        }
    }
    static String read_string(int address) {
        if(memory.get(address)-1<0)System.out.println("string length invalid: "+(memory.get(address)-1));
        byte[] str = new byte[memory.get(address)-1];
        int offset = address+1;
        for(int i = 0; i<str.length; i++){
            str[i] = (byte)((int)memory.get(offset + i));
        }
        return new String(str);
    }

    static void show_mem(){
        //make array of word addresses
        List<Integer> pointers = new ArrayList<>();
        int here = HERE;
        while(here != -1){
            pointers.add(here);
            //move 'here' back
            here = memory.get(here-1);
        }

        for(int i = pointers.size()-1;i>=0;i--){
            int add = pointers.get(i);

            String word_name = read_string(add);
            int immediate = memory.get(add + memory.get(add));

            System.out.format("[%s %d immediate:%d ", setBoldText + word_name + setPlainText, add, immediate);

            // get the names of instructions within word
            int op = add + memory.get(add) + 1;
            int nextop = i==0? memory.size():pointers.get(i-1) - 1;

            for(int j=op; j<nextop; j++){
                int mem = memory.get(j);
                String name = read_string(mem);
                if(name.strip().equals("return")) {
                    break;
                }
                System.out.print(name + " ");

                if(name.strip().equals("literal")) {
                    j++;
                    System.out.print(memory.get(j) + " ");
                }
                if(name.strip().equals("branch")) {
                    j++;
                    System.out.print(memory.get(j) + " ");
                }if(name.strip().equals("branch?")) {
                    j++;
                    System.out.print(memory.get(j) + " ");
                }
            }
            System.out.println("]");
        }

        System.out.println();
    }

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
