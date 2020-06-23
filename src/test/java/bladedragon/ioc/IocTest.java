package bladedragon.ioc;

import bladedragon.bean.WebController;
import bladedragon.core.BeanFactory;
import bladedragon.core.ioc.IocContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

@Slf4j
public class IocTest {

    @Test
    public void doIoc() {
        IocContext context = new IocContext();
        BeanFactory factory = BeanFactory.getInstance();
//        context.doAutowired();
        WebController controller = (WebController) factory.getBean("webController");
        if(null == controller){
            for(String name : factory.getBeanNames()){
                System.out.println(name);
            }
        }else{
            controller.hello();
        }

    }

}
