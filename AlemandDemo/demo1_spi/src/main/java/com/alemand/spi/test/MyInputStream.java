package com.alemand.spi.test;

import java.io.FileDescriptor;
import java.io.FileInputStream;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/14
 */
public class MyInputStream extends FileInputStream {
    public MyInputStream(FileDescriptor fdObj) {
        super(fdObj);
    }
}
