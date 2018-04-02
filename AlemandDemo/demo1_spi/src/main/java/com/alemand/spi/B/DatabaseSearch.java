package com.alemand.spi.B;

import com.alemand.spi.Search;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class DatabaseSearch implements Search{

    @Override
    public String search(String keyword) {
        return "B"+keyword;
    }
}
