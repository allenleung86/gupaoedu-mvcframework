package com.gupaoedu.demo.service.impl;

import com.gupaoedu.demo.service.IDemoService;
import com.gupaoedu.mvcframework.annotation.GPService;

/**
 * @author liangzhaolin
 * @date 2019/7/7 9:48
 */
@GPService
public class DemoService implements IDemoService {
    @Override
    public String get(String name) {
        return "My name is " + name;
    }
}
