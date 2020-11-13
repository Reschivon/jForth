import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionMachine {
    static Object getByString(Object actor, String name){
        Field f = null;
        try {
            f = actor.getClass().getField(name);
        } catch (NoSuchFieldException e) {
        }

        Method m = null;
        for(Method p:actor.getClass().getMethods()){
            if(p.getName().equals(name)){
                m = p;
                break;
            }
        }

        if(m != null) return m;
        if(f != null) return f;
        return null;
    }
}
