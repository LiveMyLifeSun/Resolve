package Demo7;

/**
 * <p> 装饰者的抽象 </p>
 *
 * @author Alemand
 * @since 2018/1/25
 */
public class Decorator implements Person {

    /**
     *通过组合的方式
     */
    private Person person;
    /**
     *构造实例
     */
    public Decorator(Person person){
        this.person = person;
    }
    @Override
    public void sing() {
        person.sing();
    }
}
