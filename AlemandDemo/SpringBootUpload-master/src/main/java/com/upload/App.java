package com.upload;

import javax.servlet.MultipartConfigElement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.MultipartConfigFactory;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;



@SpringBootApplication
public class App extends SpringBootServletInitializer
{
	/**
	 * 设置上传文件的大小
	 */
	@Bean  
    public MultipartConfigElement multipartConfigElement() {  
        MultipartConfigFactory factory = new MultipartConfigFactory();  
        factory.setMaxFileSize("12MB");  
        factory.setMaxRequestSize("12MB");  
        return factory.createMultipartConfig();  
    }  
	
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }
}
