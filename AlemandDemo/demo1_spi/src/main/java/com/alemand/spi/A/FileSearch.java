package com.alemand.spi.A;

import com.alemand.spi.Search;


/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class FileSearch implements Search {
    @Override
    public String search(String keyword) {
        return "A"+keyword;
    }
}
