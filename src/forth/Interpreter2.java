package forth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Interpreter2 {
    public static boolean DEBUG = false;
    private static final String setPlainText = "\033[0;0m";
    private static final String setBoldText = "\033[0;1m";

    static boolean immediate = true;
    //address of first pointer in the linked list that comprises the dictionary
    static int HERE = -1;
    //Address of initial opcode, or main() for c programmers. Input is written to here
    static int ENTRY_POINT = -1;

    static Stack<Integer> memory = new Stack<>();
    static Stack<Integer> stack = new Stack<>();
    static Stack<Integer> call_stack = new Stack<>();

    //address and name
    static HashMap<Integer, String> primitive_words = new HashMap<>();

    static void create(String name){
        //pointer to prev
        memory.add(HERE);
        //meta pointer
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


    static void declarePrimitive(String name){
        declarePrimitive(name, false);
    }

    static void declarePrimitive(String name, boolean immediate){
        create(name);
        primitive_words.put(HERE, name);
        if(immediate) set_immediate();
    }


    public static void main(String[] args) {
        //set up primitives
        declarePrimitive("seestack");
        declarePrimitive("seemem");
        declarePrimitive("seerawmem");
        declarePrimitive("memposition");
        declarePrimitive("print");
        declarePrimitive("return", true);
        declarePrimitive("immediate");
        declarePrimitive("word");
        //declarePrimitive("token>stack", true);
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
        declarePrimitive("dup");
        declarePrimitive("swap");
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
        declarePrimitive("read-mem");
        declarePrimitive("create");

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

        try (Scanner scan = new Scanner(new File(
                System.getProperty("user.dir") + "/src/forth/start.f"))){

            System.out.println("Startup file found");
            exec(scan);
        } catch (FileNotFoundException ignored) {}

        exec(new Scanner(System.in));
    }

    public static void exec(Scanner scan) {
        //DEBUG = true;

        call_stack.add(ENTRY_POINT+1);
        memory.set(ENTRY_POINT, search_word("donothing"));
        memory.set(ENTRY_POINT+1, search_word("return"));

        while (true){
            int word_address = memory.get(call_stack.last());

            if(immediate || memory.get(word_address + memory.get(word_address)) == 1 || (!immediate && call_stack.size()>=2)) {
                if (primitive_words.containsKey(word_address)) {
                    //execute primitive
                    if(DEBUG)System.out.print(" r::" + read_string(word_address));
                    switch (primitive_words.get(word_address)) {
                        case "donothing" -> System.out.print("");
                        case "print" -> System.out.println(stack.pop());
                        case "return" -> call_stack.remove(call_stack.size() - 1);
                        case "word" -> stack.add(search_word(scan.next()));
//                        case "token>stack" -> {
//                            memory.add(search_word("literal"));
//                            memory.add(search_word(scan.next()));
//                        }
                        case "stack>mem" -> memory.add(stack.pop());
                        case "[" -> immediate = true;
                        case "]" -> immediate = false;
                        case "immediate" -> set_immediate();
                        case "seestack" -> {
                            for (var tok : stack) System.out.print(tok + " ");
                            System.out.println("<-");
                        }
                        case "seemem" -> show_mem();
                        case "seerawmem" -> System.out.println(memory);
                        case "stringliteral" -> write_string(scan.next(), memory.size());
                        case "read-string" -> System.out.println(read_string(stack.pop()));
                        case "literal" -> {
                            call_stack.add(call_stack.pop() + 1);
                            stack.add(memory.get(call_stack.last()));
                        }
                        case "lit" -> stack.add(Integer.valueOf(scan.next()));
                        case "[lit]" -> {
                            memory.add(search_word("literal"));
                            memory.add(Integer.valueOf(scan.next()));
                        }
                        case "memposition" -> stack.add(memory.size());
                        case "read-mem" -> stack.add(memory.get(stack.pop()));
                        case "create" -> {memory.add(HERE); HERE = memory.size();}
                        case "read" -> stack.add(memory.get(stack.pop()));
                        case "set" -> memory.set(stack.pop(), stack.pop());
                        case "+" -> stack.add(stack.pop() + stack.pop());
                        case "-" -> stack.add(-stack.pop() + stack.pop());
                        case "*" -> stack.add(stack.pop() * stack.pop());
                        case "not" -> stack.add(stack.pop()==0?1:0);
                        case "and" -> stack.add(stack.pop() & stack.pop());
                        case "or" -> stack.add(stack.pop() | stack.pop());
                        case "xor" -> stack.add(stack.pop() ^ stack.pop());
                        case "swap" -> {int p = stack.pop(); stack.add(stack.size()-1, p);}
                        case "dup" -> stack.add(stack.last());
                        case "branch" -> //advance pointer by instruction at current pointer position
                                call_stack.set(call_stack.size()-1, call_stack.last() + memory.get(call_stack.last() + 1));
                        case "branch?" -> {
                            //System.out.println(" branching "+stack.last()+" off "+ memory.get(call_stack.last() + 1));
                            //IF stack top is nonzero
                            if(stack.last() == 0) {
                                //advance pointer by instruction at current pointer position
                                call_stack.set(call_stack.size() - 1, call_stack.last() + memory.get(call_stack.last() + 1));
                            }else{
                                //jump over the offset by 1
                                call_stack.set(call_stack.size() - 1, call_stack.last() + 1);
                            }

                            stack.pop();
                        }
                        case "alloc" -> {for(int i=0;i<stack.pop();i++)memory.add(0);}
                    }
                } else {
                    //execute forth word
                    if(DEBUG)System.out.print(" rf::" + read_string(word_address));

                    call_stack.add(word_address + memory.get(word_address));
                }
            }else{
                if(DEBUG)System.out.print(" c::" + read_string(word_address));
                memory.add(word_address);
            }

            if (call_stack.size() == 0){
                // no more input, quit
                if(!scan.hasNext()) break;

                String nextWord = scan.next();
                int w_addr = search_word(nextWord);

                if(DEBUG)System.out.println("\nPProgram pointer: " +nextWord);

                // if word found
                if(w_addr == -1){
                    System.out.println("word " + nextWord + " not found");
                    //set empty instruction as entry
                    memory.set(ENTRY_POINT, search_word("donothing"));
                }else {
                    //set new instruction as entry
                    memory.set(ENTRY_POINT, w_addr);
                }
                //restart from entry point
                call_stack.add(ENTRY_POINT);
                continue;
            }

            //advance
            int next_stack_frame = call_stack.pop() + 1;
            call_stack.add(next_stack_frame);

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
        List<Integer> pointers = new ArrayList<>();
        int here = HERE;
        while(here != -1){
            pointers.add(here);
            //move 'here' back
            here = memory.get(here-1);
        }
        /////////////
        if(true) {
            int add = pointers.get(0);
            String word_name = read_string(add);

            int immediate = memory.get(add + memory.get(add));
            System.out.format("[%s immediate:%d ", word_name, immediate);

            int op = add + memory.get(add) + 1;
            int nextop = memory.size();

            //System.out.println("op-nextop " + op + " " + nextop);

            for (int j = op; j < nextop; j++) {
                //System.out.print("D:"+memory.get(j) +  " ");
                int mem = memory.get(j);
                String name = read_string(mem);
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
        ////////////////

        for(int i = 1;i<pointers.size();i++){
            int add = pointers.get(i);
            String word_name = read_string(add);

            int immediate = memory.get(add + memory.get(add));
            System.out.format("[%s immediate:%d ", setBoldText + word_name + setPlainText, immediate);

            int op = add + memory.get(add) + 1;
            int nextop = pointers.get(i-1) - 1;

            //System.out.println("op-nextop " + op + " " + nextop);

            for(int j=op; j<nextop; j++){
                //System.out.print("D:"+memory.get(j) +  " ");
                int mem = memory.get(j);
                String name = read_string(mem);
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

    public static class Stack <E> extends ArrayList<E> {
        public E pop(){
            return remove(size()-1);
        }

        public E last(){
            return get(size()-1);
        }
    }
}
