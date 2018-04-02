package Demo8;

/**
 * <p> 遥控器(外观角色) </p>
 *
 * @author Alemand
 * @since 2018/1/30
 */
public class Telecontrol {
    /**
     * dvd
     */
    private DVD dvd;
    /**
     * 投影仪
     */
    private Screen screen;
    /**
     * 音响
     */
    private Voice voice;

    public Telecontrol(DVD dvd, Screen screen, Voice voice) {
        this.dvd = dvd;
        this.screen = screen;
        this.voice = voice;
    }
    /**
     * 遥控器控制打开
     */
    public void  turnOn(){
        dvd.play();
        screen.drop();
        voice.adjust();
    }

}
