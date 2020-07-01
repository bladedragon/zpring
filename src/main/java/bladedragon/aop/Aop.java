package bladedragon.aop;

import bladedragon.aop.advice.Advice;
import bladedragon.aop.advice.Order;
import bladedragon.aop.annotation.Aspect;
import bladedragon.core.BeanFactory;
import bladedragon.core.ioc.IocContext;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class Aop {

    private BeanFactory beanFactory;
    public Aop(){
        beanFactory = BeanFactory.getInstance();
    }

    public void doAop(){
        System.out.println("start do aop");
        //test
        Set<Class<?>> classSet = beanFactory.getClassesBySuperClass(Advice.class);

        Set<String> classNameSet = new HashSet<>();
        for(Class<?> clz : classSet){
            classNameSet.add(clz.getSimpleName());
        }
        log.info("classNameSet: "+classNameSet);

        beanFactory.getClassesBySuperClass(Advice.class)
                .stream()
                .filter(clz->clz.isAnnotationPresent(Aspect.class))
                .forEach(clz->{
                    System.out.println("aop foreach"+clz.getSimpleName());
                    //TOOD: 注意要修改aop的名字
                    final Advice advice = (Advice) beanFactory.getBean(clz);
                    Aspect aspect = clz.getAnnotation(Aspect.class);
                    beanFactory.getClassesByAnnotation(aspect.target())
                            .stream()
                            .filter(target -> !Advice.class.isAssignableFrom(target))
                            .filter(target -> !target.isAnnotationPresent(Aspect.class))
                            .forEach(target -> {
                                ProxyAdvicer advicer = new ProxyAdvicer(advice);
                                List<ProxyAdvicer> advicerList = new ArrayList<>();
                                advicerList.add(advicer);
                                Object proxyBean = ProxyCreator.createProxy(target,advicerList);
                                beanFactory.addBean(target.getSimpleName(),proxyBean);
                                log.info("aop add bean: "+ target.getSimpleName());
                            });

                });
    }

//    private List<ProxyAdvicer> createMatchProxies(List<ProxyAdvicer> proxyList, Class<?> clz) {
//
//    }

    private ProxyAdvicer createProxyAdvisor(Class<?> clz) {
        int order = 0;
        if (clz.isAnnotationPresent(Order.class)) {
            order = clz.getAnnotation(Order.class).value();
        }
        String expression = clz.getAnnotation(Aspect.class).pointcut();
        ProxyPointcut proxyPointcut = new ProxyPointcut();
        proxyPointcut.setExpression(expression);
        Advice advice = (Advice) beanFactory.getBean(clz);
        return new ProxyAdvicer(advice, proxyPointcut, order);
    }

}
