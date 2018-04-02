package com.alemand.text;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
@Persion(name = "haha",age = 23,hobby = {"test1" , "test2"})
public class Student {

    @StudentGender(gender = StudentGender.Gender.BOY)
    private String stuGender;

    public String getStuGender(){
        return stuGender;
    }

    public void setStuGender(String stuGender){
        this.stuGender=stuGender;
    }

}
