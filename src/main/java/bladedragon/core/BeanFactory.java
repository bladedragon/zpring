package bladedragon.core;

import bladedragon.core.annotation.Component;
import bladedragon.core.annotation.Controller;
import bladedragon.core.annotation.Repository;
import bladedragon.core.annotation.Service;

import bladedragon.util.StrUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanFactory {

    private Map<String,Object> container = new ConcurrentHashMap<>();

    private boolean isLoadBean = false;


    public static BeanFactory getInstance(){
        return ContainerHolder.HOLDER.instance;
    }

    public Object getBean(Class<?> clz){
            String beanName = clz.getSimpleName();
            Object instance = container.get(beanName);
            if(null == instance){
                log.info("beanName :"+StrUtil.lowerFirstCase(beanName));
                return container.get(StrUtil.lowerFirstCase(beanName));
            }
            log.info("beanName: "+ beanName);
            return instance;
    }
    public Object getBean(String beanName){
        if(StrUtil.isBlank(beanName)){
           throw new RuntimeException("beanName is null");
        }
        Object bean = this.container.get(beanName);
        if(bean == null){
//            throw new NoSuchBeanException(beanName);
        }
        return bean;
    }
    public Set<Object> getBeans(){
        if(container.isEmpty()){
            return null;
        }
        return new HashSet<>(container.values());
    }

    public Object addBean(String beanName,Object bean){
        log.info("add bean named '"+beanName+"' successfully");
        return container.put(beanName,bean);
    }

    public void removeBean(String beanName){
        if(container.containsKey(beanName)){
           container.remove(beanName);
        }else{
            log.info("no bean named '"+beanName+"' found in container");
        }
    }

    public int size(){
        return container.size();
    }

    public Set<String> getBeanNames(){
        return container.keySet();
    }

    public Set<Class<?>> getClassesByAnnotation(Class<? extends Annotation> annotation){

        Set<Class<?>> classSet = new HashSet<>();
        try {
            for (Object instance : container.values()) {
                classSet.add(instance.getClass());
            }
        }catch (Exception e){
            log.error("some names cannot map tp class");
        }
        return classSet.stream()
                .filter(clz->clz.isAnnotationPresent(annotation))
                .collect(Collectors.toSet());

    }

    //----------------- 和 loadBean 相关-----

    public boolean isLoad(){
        return this.isLoadBean;
    }

    public void setIsLoadTrue(){
        isLoadBean = true;
    }

    public boolean isEmpty(){
        return container.isEmpty();
    }



    private enum ContainerHolder{
        HOLDER;
        private BeanFactory instance;
        ContainerHolder(){
            instance = new BeanFactory();
        }
    }

    //---------和Aop相关
    public Set<Class<?>> getClassesBySuperClass(Class<?> interfaceClz){
        Set<String> clzNames = container.keySet();
        log.info("clzNames: "+clzNames);
        Set<Class<?>> returnClasses = new HashSet<>();

            for(String name: clzNames) {
                Class<?> clz = container.get(name).getClass();
                if (interfaceClz.isAssignableFrom(clz) && !clz.equals(interfaceClz)) {
                    returnClasses.add(clz);
                }
            }

        return returnClasses;
    }

    //TODO: forName用法错误
    public Set<Class<?>> getClasses(){
        Set<Class<?>> classSet = new HashSet<>();
        for(Object instance: container.values()){

                Class clz = instance.getClass();
                if(null != clz){
                    classSet.add(clz);
                }
        }
        return classSet;
    }

}
