package Demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/8
 */
public class Demo3 {
    public static void main(String[] args) throws Exception {
        long l = System.currentTimeMillis();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("C:\\Users\\Administrator\\Desktop\\2017\\2017\\11\\30\\CN101340204.json")));
        int line = 1;
        String context = null;
        while ((context = bufferedReader.readLine()) != null) {
            context="{" +
                    "HeWeather5:["+context+"]}";
            System.out.println(context);
            line++;
        }
        bufferedReader.close();
        BufferedReader bufferedReader1 = new BufferedReader(new FileReader(new File("C:\\Users\\Administrator\\Desktop\\2017\\2017\\11\\30\\CN101340204.json")));
        int line1 = 1;
        String context1 = null;
        while ((context1 = bufferedReader1.readLine()) != null) {
            context1="{" +
                    "HeWeather5:["+context1+"]}";
            System.out.println(context1);
            line1++;
        }
        bufferedReader.close();
        long l1 = System.currentTimeMillis();
        System.out.println(l1-l);

    }
}
