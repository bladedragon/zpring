package bladedragon.mvc;

import bladedragon.core.BeanFactory;
import bladedragon.mvc.controllerHandler.ControllerHandler;
import bladedragon.mvc.controllerHandler.ControllerInfo;
import bladedragon.mvc.controllerHandler.PathInfo;
import bladedragon.mvc.netty.codec.SelfRequest;
import bladedragon.mvc.netty.codec.SelfResponse;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Dispatcher {

    private static BeanFactory beanFactory = BeanFactory.getInstance();
    //TODO: 设置成单例模式
    ControllerHandler controllerHandler = new ControllerHandler();


    public void doDispatcher(SelfResponse response, SelfRequest request){
        Map<PathInfo, ControllerInfo> handlerMap = controllerHandler.getPathControllerMap();
        ControllerInfo controllerInfo = getControllerInfo(request);

        Map<String,Object> params = request.getParams();
        log.info("Request传入的参数是 ：{}",params.keySet());
        List<Object> invokeParams = new ArrayList<Object>();
        for(String param : controllerInfo.getMethodParameter()){
            if(params.containsKey(param)){
                invokeParams.add(params.get(param));
            }
            if(param.equals("SelfRequest")){
                invokeParams.add(request);
            }else if(param.equals("SelfResponse")){
                invokeParams.add(response);
            }
        }
        log.info("等待注入的参数列表是：[{}]",invokeParams);

        try {
            controllerInfo.getMethod().invoke(controllerInfo.getControllerInstance(),invokeParams.toArray());
        } catch (IllegalAccessException e) {
            log.error("方法调用失败");
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            log.error("方法调用失败");
            e.printStackTrace();
        }

    }


    private ControllerInfo getControllerInfo(SelfRequest req){
        String requestMethod = req.getMethod();
        String requestPath = req.getPath();
        log.info("getRequest: {} {}", requestMethod, requestPath);
        if(requestPath.endsWith("/")){
            requestPath.substring(0,requestPath.length()-1);
        }
        return controllerHandler.getController(requestMethod,requestPath);

    }


}
