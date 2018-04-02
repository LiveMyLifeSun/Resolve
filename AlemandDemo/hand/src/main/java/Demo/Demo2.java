package Demo;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/6
 */
public class Demo2 {
    public static void main(String[] args) throws IOException {
        // find("C:\\Users\\Administrator\\Desktop\\2017", depth);
        //getFiles("C:\\Users\\Administrator\\Desktop\\2017");
        /*BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("C:\\Users\\Administrator\\Desktop\\2017\\2017\\11\\30\\CN101340204.json")));
        int line = 1;
        String context = null;
        while ((context = bufferedReader.readLine()) != null) {
            context="{" +
                    "HeWeather5:["+context+"]}";
            System.out.println(context);
            line++;
       }
       bufferedReader.close();*/
        //2996
        long l = System.currentTimeMillis();
        //getFile("C:\\Users\\Administrator\\Desktop\\2017");
        getFiles("C:\\Users\\Administrator\\Desktop\\2017");
        long l2 = System.currentTimeMillis();
        System.err.println(l2-l);
    }

    private static int depth = 1;

    public static void find(String pathName, int depth) throws IOException {
        int filecount = 0;
        //获取pathName的File对象
        File dirFile = new File(pathName);
        //判断该文件或目录是否存在，不存在时在控制台输出提醒
        if (!dirFile.exists()) {
            System.out.println("do not exit");
            return;
        }
        //判断如果不是一个目录，就判断是不是一个文件，是文件则输出文件路径
        if (!dirFile.isDirectory()) {
            if (dirFile.isFile()) {
                System.out.println(dirFile.getCanonicalFile());
            }
            return;
        }

        for (int j = 0; j < depth; j++) {
            System.out.print("  ");
        }
        System.out.print("|--");
        System.out.println(dirFile.getName());
        //获取此目录下的所有文件名与目录名
        String[] fileList = dirFile.list();
        int currentDepth = depth + 1;
        for (int i = 0; i < fileList.length; i++) {
            //遍历文件目录
            String string = fileList[i];
            //File("documentName","fileName")是File的另一个构造器
            File file = new File(dirFile.getPath(), string);
            String name = file.getName();
            //如果是一个目录，搜索深度depth++，输出目录名后，进行递归
            if (file.isDirectory()) {
                //递归
                find(file.getCanonicalPath(), currentDepth);
            } else {
                //如果是文件，则直接输出文件名
                for (int j = 0; j < currentDepth; j++) {
                    System.out.print("   ");
                }
                System.out.print("|--");
                System.out.println(name);

            }
        }
    }

    public static void getFiles(String filePath) {
        File root = new File(filePath);
        if (root.isDirectory()) {
            File[] files = root.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath());
                } else {
                    if (file.getAbsolutePath().toString().contains("json")) {
                        System.out.println(file.getAbsolutePath());
                    }
                }
            }
        } else {
            if (root.getAbsolutePath().toString().contains("json")) {
                System.out.println(root.getAbsolutePath());
            }
        }

    }
    //使用循环来遍历文件夹
    public static void getFile(String filepath){
        File file = new File(filepath);
        //判断给该文件是否存在
        if(file.exists()){
            //判断是否为文件夹
            if(file.isDirectory()){
                //如果是文件夹的话获得给文件夹下的所有文件
                File[] files = file.listFiles();
                //创建LinkList用来存放文件夹
                LinkedList<File> list = new LinkedList<File>();
                //对文件数组进行遍历
                for (File file1:files){
                    //如果是文件夹的话添加到集合当中
                    if(file1.isDirectory()){
                        list.add(file1);
                    }else{
                        //是文件
                        if(file1.getAbsolutePath().contains("json")){
                            System.out.println(file1.getAbsolutePath());
                        }
                    }
                }
                File nextFile = null;
                while(!list.isEmpty()){
                    //将第一个移除
                    nextFile = list.removeFirst();
                    //获得第一文件夹的file集合
                    File[] files1 = nextFile.listFiles();
                    //遍历
                    for (File file2:files1) {
                        if(file2.isDirectory()){
                            //如果是文件夹在进行添加
                            list.add(file2);
                        }else{
                            //是文件
                            if(file2.getAbsolutePath().contains("json")){
                                System.out.println(file2.getAbsolutePath());
                            }
                        }
                    }
                }
            }else{

            }
        }
    }
}
