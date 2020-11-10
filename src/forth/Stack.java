package forth;

import java.util.ArrayList;

public class Stack <E> extends ArrayList<E> {
    public E pop(){
        return remove(size()-1);
    }

    public E last(){
        return get(size()-1);
    }
}
