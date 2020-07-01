package bladedragon.aop;

import net.sf.cglib.proxy.Callback;

import java.util.List;

public class AdviceMethodInterceptor implements Callback {
    public AdviceMethodInterceptor(Class<?> targetClass, List<ProxyAdvicer> proxyList) {
    }
}
