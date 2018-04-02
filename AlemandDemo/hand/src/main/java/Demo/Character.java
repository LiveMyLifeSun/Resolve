package Demo;

/**
 * <p> 角色 </p>
 *
 * @author Alemand
 * @since 2017/12/4
 */
public class Character {
    /**
     *共同拥有的属性
     */
    private String name;
    private String age;
    private String sex;


    public void setName(String name) {
        this.name = name;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }


    public void fight(){
        System.out.println("战斗");
    }


}
