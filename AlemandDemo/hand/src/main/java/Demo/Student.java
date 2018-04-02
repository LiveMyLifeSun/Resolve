package Demo;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/12/6
 */
public class Student {

    private String name;
    private String sex;
    private int age;
    public Student(){

    }

    public Student(String name, String sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public int getAge() {
        return age;
    }

    @Override
    public String toString() {
        return "Student{" +
                "name='" + name + '\'' +
                ", sex='" + sex + '\'' +
                ", age=" + age +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {return true;}
        if (!(o instanceof Student)) {return false;}

        Student student = (Student) o;

        if (age != student.age) {return false;}
        if (!name.equals(student.name)) {return false;}
        return sex.equals(student.sex);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + sex.hashCode();
        result = 31 * result + age;
        return result;
    }
}
