package com.alemand.resolve.controller;

import com.alemand.resolve.response.ApiResponse;
import com.alemand.resolve.response.BusinessException;
import com.alemand.resolve.service.ResolveExcelService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Resource;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/19
 */
@RestController
@RequestMapping("/resolve")
public class ResolveExcelController {


    @Resource(name = "resolveExcelServiceImpl")
    private ResolveExcelService resolveExcelService;

    /**
     * 文件上传
     */
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ResponseBody
    public ApiResponse uploadExcel(@RequestParam("file") MultipartFile file) {
        Object result;
        try {
            result = resolveExcelService.resolveExcel(file);
            //如果需要将文件放到服务其中加以下代码
           /* try {
                BufferedOutputStream out = new BufferedOutputStream(
                        new FileOutputStream(new File(file.getOriginalFilename())));
                out.write(file.getBytes());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        } catch (BusinessException e) {
            e.printStackTrace();
            return ApiResponse.failOf(-1, e.getErrMsg());
        }
        return ApiResponse.successOf(result);
    }


}
