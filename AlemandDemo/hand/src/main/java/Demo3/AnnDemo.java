package Demo3;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.annotation.Resource;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/13
 */
@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AnnDemo {
    String fitstName();
    Class value();
}
