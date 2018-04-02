package com.alemand;

import com.alemand.entity.User;

import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;


/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/19
 */
public class Demo {

    public static void main(String[] args) {
        //
        ClassPathXmlApplicationContext classPathXmlApplicationContext = new ClassPathXmlApplicationContext("test.xml");
        User user = (User) classPathXmlApplicationContext.getBean("dateFormat");
        System.out.println(user.getUserName());

    }
}
