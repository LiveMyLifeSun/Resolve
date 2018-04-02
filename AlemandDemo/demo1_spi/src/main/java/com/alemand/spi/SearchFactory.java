package com.alemand.spi;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class SearchFactory {
    private SearchFactory(){}

    public static Search getSearch(){
        Search search = null;
        ServiceLoader<Search> searchloader = ServiceLoader.load(Search.class);
        Iterator<Search> searchs = searchloader.iterator();
        if(searchs.hasNext()){
            search=searchs.next();
        }
        return search;
    }
}
