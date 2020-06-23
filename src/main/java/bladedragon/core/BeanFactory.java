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

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BeanFactory {

    private Map<String,Object> container = new ConcurrentHashMap<>();

    private boolean isLoadBean = false;


    public static BeanFactory getInstance(){
        return ContainerHolder.HOLDER.instance;
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
        Set<String> nameSet = container.keySet();
        Set<Class<?>> classSet = new HashSet<>();
        try {
            for (String name : nameSet) {
                classSet.add(Class.forName(name));
            }
        }catch (ClassNotFoundException e){
            log.error("some names cannot map tp class");
        }
        return classSet;

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
}
