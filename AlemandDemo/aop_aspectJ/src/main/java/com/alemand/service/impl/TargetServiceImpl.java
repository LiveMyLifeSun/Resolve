package com.alemand.service.impl;

import com.alemand.service.TargetServcie;

import org.springframework.stereotype.Service;

/**
 * <p> 类说明 </p>
 *
 * @author Alemand
 * @since 2017/11/20
 */
@Service
public class TargetServiceImpl implements TargetServcie {


    @Override
    public void update(int id) {
        System.out.println(id);
    }
}
