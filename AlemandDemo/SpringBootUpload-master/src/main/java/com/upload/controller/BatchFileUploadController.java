/**
 *
 * @author : liuxiaoqiang
 * @date   :Mar 8, 2016 1:19:55 PM 
 * @version 1.0 
 *
 */
package com.upload.controller;

import org.springframework.stereotype.Controller;  
import org.springframework.web.bind.annotation.RequestMapping;  
import org.springframework.web.bind.annotation.RequestMethod;  
import org.springframework.web.bind.annotation.ResponseBody;  
import org.springframework.web.multipart.MultipartFile;  
import org.springframework.web.multipart.MultipartHttpServletRequest;  
   

import javax.servlet.http.HttpServletRequest;  

import java.io.BufferedOutputStream;  
import java.io.File;  
import java.io.FileOutputStream;  
import java.io.UnsupportedEncodingException;
import java.util.List;  
   
@Controller  
public class BatchFileUploadController {  
   
    @RequestMapping(value="/batch/upload", method= RequestMethod.POST)  
    public @ResponseBody  
    String handleFileUpload(HttpServletRequest request) throws UnsupportedEncodingException{  
    	request.setCharacterEncoding("UTF-8");
    	String rootPath = request.getServletContext().getRealPath("/");
    	System.out.println(rootPath);
        List<MultipartFile> files = ((MultipartHttpServletRequest)request).getFiles("file");  
        for (int i =0; i< files.size(); ++i) {  
            MultipartFile file = files.get(i);  
            String name = file.getOriginalFilename(); 
            if (!file.isEmpty()) {  
                try {  
                    byte[] bytes = file.getBytes();  
                    BufferedOutputStream stream =  
                            new BufferedOutputStream(new FileOutputStream(new File(name)));  
                    stream.write(bytes);  
                    stream.close();  
                } catch (Exception e) {  
                    return "You failed to upload " + name + " => " + e.getMessage();  
                }  
            } else {  
                return "You failed to upload " + name + " because the file was empty.";  
            }  
        }  
        return "upload successful";  
    }  
}  
