package com.alemand.spi.test;

import com.alemand.spi.Search;
import com.alemand.spi.SearchFactory;

import java.util.HashMap;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class SearchTest {
    public static void main(String[] args) {
        Search search = SearchFactory.getSearch();
        String s = search.search("查找文件");
        System.out.println(s);
        HashMap
    }
}
