package com.alemand;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;


/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/21
 */

@Controller
public class controller {

    /**
     *文件的上传与文件的下载
     */
    @RequestMapping(value ="/upload.do" , method = RequestMethod.POST)
    public String upload(@RequestParam("uploadfile") MultipartFile file, HttpServletRequest request){
        //获取文件的类型
        String type = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        UUID uuid = UUID.randomUUID();
        String fileName = uuid + "." + type;



        return null;
    }

}
