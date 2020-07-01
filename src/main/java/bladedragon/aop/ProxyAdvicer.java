package bladedragon.aop;

import bladedragon.aop.advice.Advice;
import bladedragon.aop.advice.AfterReturningAdvice;
import bladedragon.aop.advice.MethodBeforeAdvice;
import bladedragon.aop.advice.ThrowsAdvice;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ProxyAdvicer {
    private Advice advice;

    public ProxyAdvicer(Advice advice, ProxyPointcut proxyPointcut, int order) {

    }

    public Object doProxy(Object target, Class<?> targetClass, Method method, Object[] args, MethodProxy proxy) throws Throwable {
        Object result =  null;
        if(advice instanceof MethodBeforeAdvice){
            ((MethodBeforeAdvice) advice).before(targetClass,method,args);
        }
        try {
            //执行被代理的方法
            result = proxy.invokeSuper(target, args);
            if (advice instanceof AfterReturningAdvice) {
                ((AfterReturningAdvice) advice).afterReturning(targetClass, result, method, args);
            }
        }catch (Exception e){
            if(advice instanceof ThrowsAdvice){
                ((ThrowsAdvice) advice).afterThrowing(targetClass,method,args,e);
            }else{
                throw new Throwable(e);
            }
        }
        return result;
    }


}
