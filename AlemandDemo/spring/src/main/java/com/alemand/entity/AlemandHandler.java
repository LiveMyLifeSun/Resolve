package com.alemand.entity;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/21
 */
public class AlemandHandler extends NamespaceHandlerSupport{

    public void init() {
        registerBeanDefinitionParser("user",new AlemandParser());
    }
}
