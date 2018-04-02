package com.alemand.entity;


import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import java.text.SimpleDateFormat;

/**
 * <p>
 * 类说明
 * </p>
 *
 * @author Alemand
 * @since 2018/3/21
 */

public class AlemandParser extends AbstractSingleBeanDefinitionParser {


    /**
     * @param element 元素
     * @return Class 对象
     */
    @Override
    protected Class getBeanClass(Element element) {
        return SimpleDateFormat.class;
    }

    /**
     * @param element 用来解析标签
     * @param bean    用来注册的bean
     */
    @Override
    protected void doParse(Element element, BeanDefinitionBuilder bean) {
        // this will never be null since the schema explicitly requires that a value be supplied
        String pattern = element.getAttribute("pattern");
        bean.addConstructorArgValue(pattern);

        // this however is an optional property
        String lenient = element.getAttribute("lenient");
        if (StringUtils.hasText(lenient)) {
            bean.addPropertyValue("lenient", Boolean.valueOf(lenient));
        }

    }


}
