package com.alemand.resolve;

import com.alemand.resolve.excel.ResolveExcel;
import com.xiaoleilu.hutool.collection.CollectionUtil;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ResolveApplicationTests {

    @Autowired
    private ResolveExcel resolveExcel;


    /**
     * 测试用例
     *
     * @throws IOException 业务异常,统一处理
     */
    @Test
    public void contextLoads() throws IOException {

        File file = new File("D:\\新建 Microsoft Office Excel 2007 工作表.xlsx");
        List<Map<String, String>> maps = resolveExcel.resolveExcelString(file);
        if (CollectionUtil.isNotEmpty(maps)) {
            for (Map<String, String> map : maps) {
                Set<Map.Entry<String, String>> entries = map.entrySet();
                if (CollectionUtil.isNotEmpty(entries)) {
                    for (Map.Entry<String, String> entry : entries) {
                        System.out.println(entry.getKey() + ":" + entry.getValue());
                    }
                }
            }
        }
    }

}
