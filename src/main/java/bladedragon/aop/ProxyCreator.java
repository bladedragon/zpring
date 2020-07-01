package bladedragon.aop;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.util.List;

public class ProxyCreator {


    public static Object createProxy(Class<?> targetClass, List<ProxyAdvicer> proxyList){
        return Enhancer.create(targetClass, (MethodInterceptor) (target, method, args, proxy) ->
                proxyList.get(0).doProxy(target, targetClass, method, args, proxy));
    }
}
