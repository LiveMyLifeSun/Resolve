package com.alemand.service;

import com.alemand.aop.AopAnnotation;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
public interface TargetServcie {

    @AopAnnotation
    void update(int id);
}
