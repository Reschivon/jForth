package forth;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Stack;

public class Interpreter {
    //the working stack
    static Stack<String> data = new Stack<>();
    //holds pointers to functions in memory
    static Stack<Integer> call = new Stack<>( );
    //holds word names, bodies, variables, constants, etc
    static List<String> mem  = new ArrayList<>();
    //word metadata lookup. Either address in mem or function
    static HashMap<String, Object> names = new HashMap<>();
    //TODO dirty word immediacy lookup
    static HashSet<String> immediates = new HashSet<>();

    //when immediate mode is True, run instantly
    //else compile
    static boolean immediate = true;

    static Scanner s;

    static String nextToken(){
        //exec inside a word definition (next token exists in memory)
        if(call.size() > 1) {
            int ret = call.peek();
            call.add(call.pop() + 1);
            return mem.get(ret);
            //exec in interpreter(next token has yet to be read)
        }else{
            String next = null;
            try{ next = s.next();}
            catch(NoSuchElementException e){call.pop();}
            return next;
        }
    }

    static {
        names.put("push1", (Runnable)() -> data.add(String.valueOf(1)));
        names.put("print", (Runnable)() -> System.out.println(data.pop()));
        names.put("return", (Runnable)() -> call.pop());
        names.put("[", (Runnable)() -> {
            int startIndex = mem.size();
            data.add(String.valueOf(startIndex));
            immediate = false;
        });immediates.add("[");
        names.put("]", (Runnable)() -> {
            mem.add("return");
            immediate = true;
        });immediates.add("]");
        /*names.put("bind:", (Runnable)() -> {
            names.put(nextToken(), data.pop());
        });*/
        names.put("push", (Runnable)() -> {
            data.add(nextToken());
        });
        names.put(":", (Runnable)() -> {
            data.add(nextToken());
            ((Runnable)names.get("[")).run();
        });
        names.put(";", (Runnable)() -> {
            ((Runnable)names.get("]")).run();
            int loc = Integer.parseInt(data.pop());
            names.put(data.pop(), loc);
        });immediates.add(";");

        names.put("dup", (Runnable)() -> data.add(data.peek()));
        names.put("drop", (Runnable)() -> data.pop());
        names.put("+", (Runnable)() -> data.add(String.valueOf(Integer.parseInt(data.pop()) + Integer.parseInt(data.pop()))));
        names.put("-", (Runnable)() -> data.add(String.valueOf(-Integer.parseInt(data.pop()) + Integer.parseInt(data.pop()))));
        names.put("*", (Runnable)() -> data.add(String.valueOf(Integer.parseInt(data.pop()) * Integer.parseInt(data.pop()))));
        names.put("/", (Runnable)() -> data.add(String.valueOf(Integer.parseInt(data.pop()) / Integer.parseInt(data.pop()))));
        names.put("%", (Runnable)() -> data.add(String.valueOf(Integer.parseInt(data.pop()) % Integer.parseInt(data.pop()))));

        names.put("exit", (Runnable)() -> call.clear());
        names.put("leave", (Runnable)() -> System.exit(0));

        names.put("seemem", (Runnable)() -> {for(String m:mem) System.out.println(m);});
        names.put("seedata", (Runnable)() -> {for(String m:data) System.out.println(m);});
        names.put("seewords", (Runnable)() -> {for(String m:names.keySet()) System.out.println(m + " " + names.get(m));});
    }

    static void loop(){
        while (call.size() > 0){
            String tokName = nextToken();
            Object func = names.get(tokName); //could be pointer to body or native

            //valid function
            if(func != null) {
                if(immediate || immediates.contains(tokName)) {
                    //System.out.println("exec "+tokName);
                    //primitive function
                    if (func instanceof Runnable) {
                        ((Runnable) func).run();
                    }//forth function
                    if (func instanceof Integer) {
                        call.add((int) func);
                    }
                }else{
                    mem.add(tokName);
                    //System.out.println("compile word "+ mem.get(mem.size()-1));
                }
            }//literal
            else{
                if (immediate) {
                    //System.out.println("add to stack lit " + tokName);
                    data.add(tokName);
                } else {
                    //System.out.println("compile literal "+ tokName);
                    mem.add("push");
                    mem.add(tokName);
                }
            }
        }
    }

    static void inputLoop(){
        s = new Scanner(System.in);

        call.add(0);
        loop();

        s.close();
    }
    static void inputLoop(String file){
        File f = new File(System.getProperty("user.dir") +file);
        if(!f.exists()) return;

        System.out.println("Startup file " + file + " found");

        try {
        s = new Scanner(f);
        } catch (FileNotFoundException e) {}

        call.add(0);
        loop();

        s.close();
    }

    public static void main(String[] args) {
        //inputLoop("start.f");

        inputLoop();
    }
}
