package com.alemand.resolve.excel.impl;


import com.alemand.resolve.excel.EXCEL_TYPE;
import com.alemand.resolve.excel.ResolveExcel;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/24
 */
@Service
public class ResolveExcelImpl implements ResolveExcel {


    @Override
    public List<List<Map<String, String>>> resolveExcelString(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        List<List<Map<String, String>>> list;
        if (file.getName().endsWith(".xls")) {
            list = resolveExcelString(fileInputStream, EXCEL_TYPE.SUFFIX_2003);
        } else {
            list = resolveExcelString(fileInputStream, EXCEL_TYPE.SUFFIX_2007);
        }
        return list;
    }

    @Override
    public List<List<Map<String, String>>> resolveExcelString(InputStream inputStream, EXCEL_TYPE excel) throws IOException {
        List<List<Map<String, String>>> result = new ArrayList<>();
        Workbook workbook = null;
        switch (excel) {
            case SUFFIX_2003:
                workbook = new HSSFWorkbook(inputStream);
                break;
            case SUFFIX_2007:
                workbook = new XSSFWorkbook(inputStream);
                break;
            default:
                break;
        }
        //获取说有的sheet
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            //获取第一行是列的说明
            Row firstRow = sheet.getRow(0);
            //或去这个sheet的之后一行
            int lastRowNum = sheet.getLastRowNum();
            //重第二行开始
            for (int j = 1; j <= lastRowNum; j++) {
                //获取行
                Row row = sheet.getRow(j);
                List<Map<String, String>> stringList = new ArrayList<>();
                //获取每个单元格
                short lastCellNum = row.getLastCellNum();
                for (int k = 0; k <= lastCellNum; k++) {
                    Cell rowCell = row.getCell(k);
                    Cell firstRowCell = firstRow.getCell(k);
                    if (firstRowCell != null) {
                        if (rowCell != null) {
                            //如果单元格不为空的话将里面的值添加到集合中
                            rowCell.setCellType(Cell.CELL_TYPE_STRING);
                            String stringCellValue = rowCell.getStringCellValue();
                            HashMap<String, String> StringMap = new HashMap<>(1);
                            StringMap.put(firstRowCell.getStringCellValue(), stringCellValue);
                            stringList.add(StringMap);
                        } else {
                            //如果为空的话需要将int的默认值double以及String的默认值添加进去
                            HashMap<String, String> StringMap = new HashMap<>(1);
                            StringMap.put(firstRowCell.getStringCellValue(), null);
                            stringList.add(StringMap);
                        }
                    }
                }
                result.add(stringList);
            }
        }
        return result;
    }


    @Override
    public List<List<Map<String, Integer>>> resolveExcelInt(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        List<List<Map<String, Integer>>> list;
        if (file.getName().contains(".xls")) {
            list = resolveExcelInt(fileInputStream, EXCEL_TYPE.SUFFIX_2003);
        } else {
            list = resolveExcelInt(fileInputStream, EXCEL_TYPE.SUFFIX_2007);
        }
        return list;
    }

    @Override
    public List<List<Map<String, Integer>>> resolveExcelInt(InputStream inputStream, EXCEL_TYPE excel) throws IOException {
        List<List<Map<String, Integer>>> result = new ArrayList<>();
        Workbook workbook = null;
        switch (excel) {
            case SUFFIX_2003:
                workbook = new HSSFWorkbook(inputStream);
                break;
            case SUFFIX_2007:
                workbook = new XSSFWorkbook(inputStream);
                break;
            default:
                break;
        }
        //获取说有的sheet
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            //获取第一行是列的说明
            Row firstRow = sheet.getRow(0);
            //或去这个sheet的之后一行
            int lastRowNum = sheet.getLastRowNum();
            //重第二行开始
            for (int j = 1; j <= lastRowNum; j++) {
                //获取行
                List<Map<String, Integer>> intList = new ArrayList<>();
                Row row = sheet.getRow(j);
                //获取每个单元格
                short lastCellNum = row.getLastCellNum();
                for (int k = 0; k <= lastCellNum; k++) {
                    Cell rowCell = row.getCell(k);
                    Cell firstRowCell = firstRow.getCell(k);
                    if (rowCell != null) {
                        //如果单元格不为空的话将里面的值添加到集合中
                        HashMap<String, Integer> integerMap = new HashMap<>(1);
                        int intCellValue = (int) rowCell.getNumericCellValue();
                        integerMap.put(firstRowCell.getStringCellValue(), intCellValue);
                        intList.add(integerMap);
                    } else {
                        //如果为空的话需要将int的默认值double以及String的默认值添加进去
                        HashMap<String, Integer> integerMap = new HashMap<>(1);
                        integerMap.put(firstRowCell.getStringCellValue(), 0);
                        intList.add(integerMap);
                    }
                }
                result.add(intList);
            }

        }
        return result;
    }

    @Override
    public List<List<Map<String, Double>>> resolveExcelDouble(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        List<List<Map<String, Double>>> list;
        if (file.getName().contains(".xls")) {
            list = resolveExcelDouble(fileInputStream, EXCEL_TYPE.SUFFIX_2003);
        } else {
            list = resolveExcelDouble(fileInputStream, EXCEL_TYPE.SUFFIX_2007);
        }
        return list;
    }

    @Override
    public List<List<Map<String, Double>>> resolveExcelDouble(InputStream inputStream, EXCEL_TYPE excel) throws IOException {
        List<List<Map<String, Double>>> result = new ArrayList<>();
        Workbook workbook = null;
        switch (excel) {
            case SUFFIX_2003:
                workbook = new HSSFWorkbook(inputStream);
                break;
            case SUFFIX_2007:
                workbook = new XSSFWorkbook(inputStream);
                break;
            default:
                break;
        }
        //获取说有的sheet
        int numberOfSheets = workbook.getNumberOfSheets();
        for (int i = 0; i < numberOfSheets; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            //获取第一行是列的说明
            Row firstRow = sheet.getRow(0);
            //或去这个sheet的之后一行
            int lastRowNum = sheet.getLastRowNum();
            //重第二行开始
            for (int j = 1; j <= lastRowNum; j++) {
                //获取行
                Row row = sheet.getRow(j);
                List<Map<String, Double>> doubleList = new ArrayList<>();
                //获取每个单元格
                short lastCellNum = row.getLastCellNum();
                for (int k = 0; k <= lastCellNum; k++) {
                    Cell rowCell = row.getCell(k);
                    Cell firstRowCell = firstRow.getCell(k);
                    if (rowCell != null) {
                        //如果单元格不为空的话将里面的值添加到集合中
                        HashMap<String, Double> doubleMap = new HashMap<>(1);
                        double numericCellValue = rowCell.getNumericCellValue();
                        doubleMap.put(firstRowCell.getStringCellValue(), numericCellValue);
                        doubleList.add(doubleMap);
                    } else {
                        //如果为空的话需要将int的默认值double以及String的默认值添加进去
                        HashMap<String, Double> doubleMap = new HashMap<>(1);
                        doubleMap.put(firstRowCell.getStringCellValue(), 0D);
                        doubleList.add(doubleMap);
                    }
                }
                result.add(doubleList);
            }

        }
        return result;
    }


}
