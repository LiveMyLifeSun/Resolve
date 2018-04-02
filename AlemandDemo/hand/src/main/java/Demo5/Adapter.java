package Demo5;

/**
 * <p> 适配器  </p>
 *
 * @author Alemand
 * @since 2018/1/24
 */
public class Adapter implements Round {
    /**
     * 通过组合的方式
     */
    Square square;
    /**
     *构造方法要有正方形
     */
    public Adapter(Square square) {
        this.square = square;
    }

    @Override
    public void sayHello() {
        //在这里调用正方形的方法
        square.sayHello();
    }
}
