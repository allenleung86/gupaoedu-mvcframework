package com.gupaoedu.mvcframework.servlet;

import com.gupaoedu.mvcframework.annotation.GPAutowired;
import com.gupaoedu.mvcframework.annotation.GPController;
import com.gupaoedu.mvcframework.annotation.GPService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

/**
 * @author liangzhaolin
 * @date 2019/7/6 21:49
 */
public class GPDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();

    private List<String> classNames = new ArrayList<String>();

    private Map<String, Object> ioc = new HashMap<String, Object>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doGet(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 6.等待请求阶段
        req.getRequestURI();
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1.加载配置文件
//        System.out.println("==============");
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        // 2.扫描所有相关联的类
        doScanner(contextConfig.getProperty("scanPackage"));

        // 3.初始化所有相关联的类,并且将其保存到IOC容器中
        doInstance();

        // 4.执行依赖注入
        doAutowired();

        // 5.构造HandlerMapping,将url和method进行关联
        initHandlerMapping();

        System.out.println("GupaoEDU MVC framework is init.");
    }

    /*
     * 1.加载配置文件
     */
    private void doLoadConfig(String location) {
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(location);
        try {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 2.扫描所有相关联的类
     */
    private void doScanner(String basePackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(basePackage + "." + file.getName());
            } else {
                String className = basePackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
                System.out.println(className);
            }
        }
    }

    /*
     * 3.初始化所有相关联的类,并且将其保存到IOC容器中
     */
    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }

        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                // key规则
                // 1.默认类名首字母小写
                // 2.自定义命名,优先使用自定义命名
                // 3.自动类型匹配(例如:将实现类赋值给接口)

                // 如果没有加注解,则不用初始化
                if (clazz.isAnnotationPresent(GPController.class)) {
                    Object instance = clazz.newInstance();
                    String beanName = lowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(GPController.class)) {
                    GPService service = clazz.getAnnotation(GPService.class);

                    // 2.自定义命名,优先使用自定义命名
                    String beanName = service.value();

                    // 1.默认类名首字母小写
                    if ("".equals(beanName.trim())) {
                        beanName = lowerFirstCase(clazz.getSimpleName());
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName, instance);

                    // 3.自动类型匹配(例如:将实现类赋值给接口)
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> i: interfaces) {
                        ioc.put(i.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * 4.执行依赖注入
     */
    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry: ioc.entrySet()) {
            // 注入的意思就是把所有的IOC容器中加了@GPAutowired注解的字段全部赋值
            // 包括私有的字段
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field: fields) {
                if (!field.isAnnotationPresent(GPAutowired.class)) {
                    continue;
                }

                GPAutowired autowired = field.getAnnotation(GPAutowired.class);
                String beanName = autowired.value().trim();

                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                // 如果这个字段是私有字段的话,那么,要强制访问
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * 5.构造HandlerMapping,将url和method进行关联
     */
    private void initHandlerMapping() {
    }

    /**
     * 首字母小写,利用ASCII码的差值
     *
     * @param str
     * @return java.lang.String
     * @author liangzhaolin
     * @date 2019/7/7 15:18
     */
    private String lowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
