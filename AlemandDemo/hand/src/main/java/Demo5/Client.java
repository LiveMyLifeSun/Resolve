package Demo5;

/**
 * <p> 客户 机器的类 </p>
 *
 * @author Alemand
 * @since 2018/1/24
 */
public class Client {

    /**
     * 只可以拿圆
     *
     * @param round 圆的抽象
     */
    public void getRound(Round round) {
      round.sayHello();
    }

    public static void main(String[] args) {
        Client client = new Client();
        //拿圆
        Round round = new RoundImpl();
        client.getRound(round);
        //通过适配器让正方形也放进去
        Adapter adapter = new Adapter(new SquareImpl());
        client.getRound(adapter);
    }
}
