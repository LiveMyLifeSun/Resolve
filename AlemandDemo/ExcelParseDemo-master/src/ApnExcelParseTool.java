import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ApnExcelParseTool {
    private String mFilePath;

    void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    private static final String SUFFIX_2003 = ".xls";
    private static final String SUFFIX_2007 = ".xlsx";

    Workbook initWorkBook() throws IOException {
        File file = new File(mFilePath);
        InputStream is = new FileInputStream(file);

        Workbook workbook = null;
        if (mFilePath.endsWith(SUFFIX_2003)) {
            workbook = new HSSFWorkbook(is);
        } else if (mFilePath.endsWith(SUFFIX_2007)) {
            workbook = new XSSFWorkbook(is);
        }

        return workbook;
    }

    void parseWorkbook(Workbook workbook, List<ApnModel> apnModelList) {
        int numOfSheet = workbook.getNumberOfSheets();

        for (int i = 0; i < numOfSheet; ++i) {
            Sheet sheet = workbook.getSheetAt(i);
            parseSheet(sheet, apnModelList);
        }
    }

    private List<Method> mUsedMethod;

    private void parseSheet(Sheet sheet, List<ApnModel> apnModelList) {
        Row row;

        int count = 0;

        Iterator<Row> iterator = sheet.iterator();
        while(iterator.hasNext()) {
            row = iterator.next();

            if (count == 0) {
                mUsedMethod = new ArrayList<>();
                parseRowAndFindMethod(row);
            } else {
                parseRowAndFillData(row, apnModelList);
            }

            ++count;
        }
    }

    private void parseRowAndFindMethod(Row row) {
        List<String> rst = parseRow(row);

        String methodName;
        try {
            for (String str : rst) {
                methodName = "set" + str;
                mUsedMethod.add(
                        ApnModel.class.getDeclaredMethod(methodName, String.class));
            }
        } catch (NoSuchMethodException e) {
            System.out.println(e.toString());
        }

    }

    private void parseRowAndFillData(Row row, List<ApnModel> apnModelList) {
        List<String> rst = parseRow(row);

        ApnModel apnModel = new ApnModel();

        if (mUsedMethod.size() != rst.size()) {
            System.out.println("WTF, size not right");
        } else {
            try {
                for (int i = 0; i < mUsedMethod.size(); ++i) {
                    mUsedMethod.get(i).invoke(apnModel, rst.get(i));
                }

                apnModelList.add(apnModel);
            } catch (Exception e) {
                System.out.println(e.toString());
            }
        }
    }

    private List<String> parseRow(Row row) {
        List<String> rst = new ArrayList<>();

        Cell cell;

        Iterator<Cell> iterator = row.iterator();
        while (iterator.hasNext()) {
            cell = iterator.next();
            cell.setCellType(CellType.STRING);
            rst.add(cell.getStringCellValue());
        }

        return rst;
    }
}
