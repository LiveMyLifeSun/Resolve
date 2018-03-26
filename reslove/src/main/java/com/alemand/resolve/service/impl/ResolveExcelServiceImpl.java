package com.alemand.resolve.service.impl;

import com.alemand.resolve.entity.ReqImportClient;
import com.alemand.resolve.response.BusinessException;
import com.alemand.resolve.response.ReturnCode;
import com.alemand.resolve.service.ResolveExcelService;
import com.xiaoleilu.hutool.log.Log;
import com.xiaoleilu.hutool.log.LogFactory;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/19
 */
@Service("resolveExcelServiceImpl")
public class ResolveExcelServiceImpl implements ResolveExcelService {

    private static final Log logger = LogFactory.get();
    /**
     * 注册url
     */
    private static final String SUFFIX_2003 = ".xls";
    private static final String SUFFIX_2007 = ".xlsx";
    /**
     * 电话的正则
     */
    public static final String PHONE_NUMBER_REG = "^(13[0-9]|14[579]|15[0-3,5-9]|16[6]|17[01356789]|18[0-9]|19[89])\\d{8}$";
    /**
     * 密码长度
     */
    public static final int passWardLength = 6;

    @Override
    public List<ReqImportClient> resolveExcel(MultipartFile file) throws BusinessException {


        List<ReqImportClient> list = new ArrayList<>();
        if (file == null) {
            throw new BusinessException(ReturnCode.CODE_FAIL, "对象不能为空");
        }
        //获取名字
        String originalFilename = file.getOriginalFilename();
        Workbook workbook = null;
        try {
            if (originalFilename.endsWith(SUFFIX_2003)) {
                workbook = new HSSFWorkbook(file.getInputStream());
            } else if (originalFilename.endsWith(SUFFIX_2007)) {
                workbook = new XSSFWorkbook(file.getInputStream());
            }
        } catch (Exception e) {
            logger.info(originalFilename);
            e.printStackTrace();
            throw new BusinessException(ReturnCode.CODE_FAIL, "格式错误");
        }
        if (workbook == null) {
            logger.info(originalFilename);
            throw new BusinessException(ReturnCode.CODE_FAIL, "格式错误");
        } else {
            //获取所有的工作表的的数量
            int numOfSheet = workbook.getNumberOfSheets();
            //遍历这个这些表
            for (int i = 0; i < numOfSheet; i++) {
                //获取一个sheet也就是一个工作簿
                Sheet sheet = workbook.getSheetAt(i);
                int lastRowNum = sheet.getLastRowNum();
                //从第一行开始第一行一般是标题
                for (int j = 1; j <= lastRowNum; j++) {
                    Row row = sheet.getRow(j);
                    ReqImportClient reqImportClient = new ReqImportClient();
                    //获取电话单元格
                    if (row.getCell(0) != null) {
                        row.getCell(0).setCellType(Cell.CELL_TYPE_STRING);
                        String longName = row.getCell(0).getStringCellValue();
                        //todo 正则比对
                        boolean matche = Pattern.matches(PHONE_NUMBER_REG, longName);
                        if (!matche) {
                            throw new BusinessException(ReturnCode.CODE_FAIL, "电话格式错误");
                        }
                        reqImportClient.setLoginName(longName);
                    }
                    //密码
                    if (row.getCell(1) != null) {
                        row.getCell(1).setCellType(Cell.CELL_TYPE_STRING);
                        String passWard = row.getCell(1).getStringCellValue();
                        if (passWard.replace("", "").length() < passWardLength) {
                            throw new BusinessException(ReturnCode.CODE_FAIL, "密码的格式有误");
                        }
                        reqImportClient.setPassword(passWard);
                    }
                    //姓名
                    if (row.getCell(2) != null) {
                        row.getCell(2).setCellType(Cell.CELL_TYPE_STRING);
                        String userName = row.getCell(2).getStringCellValue();
                        reqImportClient.setUserName(userName);
                    }
                    if (row.getCell(3) != null) {
                        row.getCell(3).setCellType(Cell.CELL_TYPE_STRING);
                        String groupID = row.getCell(3).getStringCellValue();
                        reqImportClient.setGroupID(groupID);
                    }
                    list.add(reqImportClient);
                }
            }
        }
        return list;
    }


}
