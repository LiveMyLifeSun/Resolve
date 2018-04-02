package Demo4;

/**
 * <p> 宝马继承Car有共同的属性要创建的宝马 </p>
 *
 * @author Alemand
 * @since 2018/1/11
 */
public class Bwm extends Car{

    /**
     * 用来测试表示汽车造好了
     */
    public void remind(){
        System.out.println(color+type+"宝马造好了");
    }
}
