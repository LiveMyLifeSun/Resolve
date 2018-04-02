package Demo;

import java.util.Comparator;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/6
 */
public class StudentCompare implements Comparator<Student>{


    public int compare(Student stu1, Student stu2) {
        if (stu1.getAge() < stu2.getAge()) {
            return 1;//需要交换顺序
        } else if (stu1.getAge() == stu2.getAge()) {
            return 0;
        } else {
            return -1;
        }
    }

}
