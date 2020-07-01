package bladedragon.aop;

import bladedragon.bean.WebController;
import bladedragon.core.BeanFactory;
import bladedragon.core.ioc.IocContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

/**
 * 注意要搞配置文件中的位置
 */
@Slf4j
public class AopTest {
    @Test
    public void doAop() {
        BeanFactory beanContainer = BeanFactory.getInstance();
        IocContext context = new IocContext();
        new Aop().doAop();
        IocContext.doAutowired();

        WebController controller = (WebController) beanContainer.getBean("webController");

        controller.hello();
    }
}