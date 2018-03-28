package com.alemand.resolve.excel;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
public interface ResolveExcel {


    /**
     * 单元格的格式为String
     *
     * @param file 文件
     * @return 解析到的Excel结果
     * @throws IOException IO异常
     */
    List<List<Map<String, String>>> resolveExcelString(File file) throws IOException;


    /**
     * 单元格的格式为String
     *
     * @param inputStream 输入流
     * @param excel       excel 的格式
     * @return 解析到的Excel结果
     * @throws IOException IO异常
     */
    List<List<Map<String, String>>> resolveExcelString(InputStream inputStream, EXCEL_TYPE excel) throws IOException;


    /**
     * 单元格的全部格式为Int
     *
     * @param file 文件
     * @return 解析到的Excel结果
     * @throws IOException IO异常
     */
    List<List<Map<String, Integer>>> resolveExcelInt(File file) throws IOException;

    /**
     * 单元格的全部格式为int
     *
     * @param inputStream 输入流
     * @param excel       excel 的格式
     * @return 解析到的Excel结果
     * @throws IOException IO异常
     */
    List<List<Map<String, Integer>>> resolveExcelInt(InputStream inputStream, EXCEL_TYPE excel) throws IOException;


    /**
     * 单元格的全部格式为double
     *
     * @param file 文件
     * @return 解析到的Excel结果
     * @throws IOException IO异常
     */
    List<List<Map<String, Double>>> resolveExcelDouble(File file) throws IOException;

    /**
     * 单元格的全部格式为double
     *
     * @param inputStream 输入流
     * @param excel       excel 的格式
     * @return 解析到的Excel结果
     * @throws IOException IO异常
     */
    List<List<Map<String, Double>>> resolveExcelDouble(InputStream inputStream, EXCEL_TYPE excel) throws IOException;


}
