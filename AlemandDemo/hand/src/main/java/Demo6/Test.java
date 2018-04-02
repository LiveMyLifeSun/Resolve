package Demo6;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2018/1/25
 */
public class Test {
    public static void main(String[] args) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        B b = new B();
        Method f = b.getClass().getDeclaredMethod("f");
        Object invoke = f.invoke(b);
    }
}
