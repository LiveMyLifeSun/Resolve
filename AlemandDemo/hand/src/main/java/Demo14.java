import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.Buffer;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/22
 */
public class Demo14 {


    public static void main(String[] args) throws Exception {
        File file = new File("D:\\0.Project\\a.txt");
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        while (!(bufferedReader.readLine() == null)) {
            String s = bufferedReader.readLine();
            System.out.println(s);
        }

    }
}
