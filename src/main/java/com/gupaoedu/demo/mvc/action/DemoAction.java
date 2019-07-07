package com.gupaoedu.demo.mvc.action;

import com.gupaoedu.demo.service.IDemoService;
import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPRequestMapping;
import com.gupaoedu.mvcframework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author liangzhaolin
 * @date 2019/7/7 9:35
 */
@GPController
@GPRequestMapping("/demo")
public class DemoAction {
    @GPAutowired
    private IDemoService demoService;

    @GPRequestMapping("/query.json")
    public void query(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("name") String name) {
        String result = demoService.get(name);
        try {
            resp.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GPRequestMapping("/add.json")
    public void add(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("a") Integer a, @GPRequestParam("b") Integer b) {
        try {
            resp.getWriter().write(a + "+" + b + "=" + (a + b));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @GPRequestMapping("/remove.json")
    public void remove(HttpServletRequest req, HttpServletResponse resp, @GPRequestParam("id") Integer id) {

    }
}
