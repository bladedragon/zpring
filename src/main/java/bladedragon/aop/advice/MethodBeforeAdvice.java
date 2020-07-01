package bladedragon.aop.advice;

import java.lang.reflect.Method;

public interface MethodBeforeAdvice extends Advice{
    void before(Class<?> clz, Method method,Object[] args) throws Throwable;
}
