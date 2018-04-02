package Demo3;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/13
 */
public class Demo {
    public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
        //注解中的内容如何获取
        final Something oldAnnotation = (Something) Foobar.class.getAnnotations()[0];
        System.out.println("oldAnnotation = " + oldAnnotation.someProperty());
        Annotation newAnnotation = new Something() {

            @Override
            public String someProperty() {
                return "another value";
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return oldAnnotation.annotationType();
            }
        };
        Field field = Class.class.getDeclaredField("annotations");
        field.setAccessible(true);
        Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(Foobar.class);
        Annotation put = annotations.put(Something.class, newAnnotation);

        Something modifiedAnnotation = (Something) Foobar.class.getAnnotations()[0];
        System.out.println("modifiedAnnotation = " + modifiedAnnotation.someProperty());
    }

    @Something(someProperty = "some value")
    public static class Foobar {
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Something {

        String someProperty();
    }


    @SuppressWarnings("unchecked")
    public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue) throws Exception {
        Object handler = Proxy.getInvocationHandler(annotation);

        Field f;

        f = handler.getClass().getDeclaredField("memberValues");

        f.setAccessible(true);

        Map<String, Object> memberValues;

            memberValues = (Map<String, Object>) f.get(handler);


        Object oldValue = memberValues.get(key);

        if (oldValue == null || oldValue.getClass() != newValue.getClass()) {

            throw new IllegalArgumentException();
        }

        memberValues.put(key, newValue);

        return oldValue;
    }
}
