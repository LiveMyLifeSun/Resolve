package Demo;


/**
 * <p> 国王 </p>
 *
 * @author Alemand
 * @since 2017/12/4
 */
public class King extends Character {
    //定义武器的接口
    private Weapon weapon;

    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public void userWeapon(){
        weapon.useWeapon();
    }

}
