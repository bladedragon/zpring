package bladedragon.core.ioc;

import bladedragon.aop.annotation.Aspect;
import bladedragon.core.BeanFactory;
import bladedragon.core.annotation.*;
import bladedragon.util.ClassUtil;
import bladedragon.util.StrUtil;
import javafx.beans.property.Property;
import lombok.extern.slf4j.Slf4j;
import sun.awt.windows.WPrinterJob;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.*;


@Slf4j
public class IocContext {
    private static BeanFactory beanFactory = BeanFactory.getInstance();
    private static Properties config = new Properties();
    private static List<String> classNames = new ArrayList<>();
    private static final List<Class<? extends Annotation>> BEAN_ANNOTATIONS
            = Arrays.asList(Component.class, Controller.class, Repository.class, Service.class, Aspect.class);


    static {
        loadConfig("/application.properties");
        doScanner(config.getProperty("scanPackage"));
        doInstance();

    }

//    public IocContext() {
//        beanFactory = BeanFactory.getInstance();
//    }

    private static void loadConfig(String configLocation) {
        InputStream inputStream = Object.class.getResourceAsStream(configLocation);
        try {
            config.load(inputStream);
        } catch (IOException e) {
            log.error("cannot load property file: {}", configLocation);
            throw new RuntimeException("cannot load property file");
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        log.info("==>loadConfig success!");
    }


    public static void doScanner(String basePackage) {

        if (beanFactory.isLoad()) {
            log.warn("Beans have been load");
            return;
        }
        List<String> list = ClassUtil.getClassNamesByPath(basePackage);
        classNames = new ArrayList<>(list);
        log.info("classNames: "+ classNames);
        log.info("==> load doScanner success!");

    }

    public static void doInstance() {
        if(null == classNames){
            log.error("cannot find classes");
            return;
        }
        for(String name : classNames){
            try {
                Class<?> clazz = ClassUtil.loadClass(name);
                for(Class<? extends  Annotation> annotation : BEAN_ANNOTATIONS){
                    //TODO:目前有bug，只要有注解就可以加载bean
                    //Bug：为什么要注入首字母为小写的bean？导致实例化的时候经常出问题
                    if(clazz.isAnnotationPresent(annotation)){

                        if(annotation == Service.class){
                            Service service = clazz.getAnnotation(Service.class);
                            String beanName = service.value();
                            if("".equals(beanName)){
                                beanName = StrUtil.lowerFirstCase(clazz.getSimpleName());
                            }
                            Object instance = clazz.newInstance();
                            beanFactory.addBean(beanName,instance);
                            Class<?>[] interfaces = clazz.getInterfaces();
                            for(Class<?> inter: interfaces) {
                                beanFactory.addBean(inter.getSimpleName(), instance);
                            }
                        }else if(annotation == Repository.class){
                            Repository service = clazz.getAnnotation(Repository.class);
                            String beanName = service.value();
                            if("".equals(beanName)){
                                beanName = StrUtil.lowerFirstCase(clazz.getSimpleName());
                            }
                            Object instance = clazz.newInstance();
                            beanFactory.addBean(beanName,instance);
                            Class<?>[] interfaces = clazz.getInterfaces();
                            for(Class<?> inter: interfaces) {

                                beanFactory.addBean(inter.getName(), instance);
                            }
                        }else if(annotation == Component.class){
                            Component service = clazz.getAnnotation(Component.class);
                            String beanName = service.value();
                            if("".equals(beanName)){
                                beanName = StrUtil.lowerFirstCase(clazz.getSimpleName());
                            }
                            Object instance = clazz.newInstance();
                            beanFactory.addBean(beanName,instance);
                            Class<?>[] interfaces = clazz.getInterfaces();
                            for(Class<?> inter: interfaces) {
                                beanFactory.addBean(inter.getName(), instance);
                            }
                        }else{
                            String beanName = StrUtil.lowerFirstCase(clazz.getSimpleName());
                            Object instance = clazz.newInstance();
                            System.out.println("beanName:"+beanName);
                            beanFactory.addBean(beanName,instance);
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        beanFactory.setIsLoadTrue();
        log.info("==> load beanFactory success !");
    }


    public static void doAutowired() {
        if(beanFactory.isEmpty()){
            log.warn("beans' container is empty!");
            return;
        }
        for(Object  bean : beanFactory.getBeans()){
            Field[] fields = bean.getClass().getDeclaredFields();

            for(Field field : fields){
                if(field.isAnnotationPresent(Autowired.class)){
                    Autowired autowired = field.getAnnotation(Autowired.class);
                    String beanName = autowired.value();
                    if(StrUtil.isBlank(beanName)){
                        beanName = field.getType().getSimpleName();
                    }
                    field.setAccessible(true);
                    try {
                        field.set(bean,beanFactory.getBean(beanName));
                    } catch (IllegalAccessException e) {
                        log.error("field autowired fail");
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public static void main(String[] args) {
        try {

            Field[] field = Service.class.getFields();
            System.out.println(field.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
