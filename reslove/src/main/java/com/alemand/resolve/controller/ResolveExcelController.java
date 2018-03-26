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
        } catch (BusinessException e) {
            e.printStackTrace();
            return ApiResponse.failOf(-1, e.getErrMsg());
        }
        return ApiResponse.successOf(result);
    }


}
