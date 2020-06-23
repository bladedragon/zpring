package bladedragon.mvc.controllerHandler;

import bladedragon.core.BeanFactory;
import bladedragon.core.annotation.Controller;
import bladedragon.mvc.annotation.RequestMapping;
import bladedragon.mvc.annotation.RequestMethod;
import bladedragon.mvc.annotation.RequestParam;
import bladedragon.mvc.netty.codec.SelfRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
public class ControllerHandler {

    private static Map<PathInfo,ControllerInfo> pathControllerMap = new ConcurrentHashMap<>();

    private static BeanFactory beanFactory = BeanFactory.getInstance();

    static{
        initRequestMapping();
    }

    public static void initRequestMapping(){
        if(beanFactory.isEmpty() || !beanFactory.isLoad()){
            log.error("beanFacory hasn't been loaded");
            return;
        }
        for(Object bean : beanFactory.getBeans()){
            Class<?> clazz = bean.getClass();
            PathInfo pathInfo;
            ControllerInfo controllerInfo;
            if(!clazz.isAnnotationPresent(Controller.class)){
                continue;
            }
            String baseUrl = "";
            String httpMethod;
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl =requestMapping.value();
                RequestMethod requestMethod = requestMapping.method();
                httpMethod = requestMethod.toString();
            }
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method : methods){
                if(method.isAnnotationPresent(RequestMapping.class)){
                    //添加Path
                    RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                    String methodUrl = requestMapping.value();
                    String path = (baseUrl+methodUrl).replaceAll("/+","/");
                    if(path.endsWith("/")){
                        path = path.substring(0,path.length()-1);
                    }
                    RequestMethod requestMethod = requestMapping.method();
                    httpMethod = requestMethod.toString();
                    pathInfo = new PathInfo(path,httpMethod);
                    if (pathControllerMap.containsKey(pathInfo)) {
                        log.error("url:{} 重复注册", pathInfo.getHttpPath());
                        throw new RuntimeException("url重复注册");
                    }

                    //添加参数
//                    Map<String,Class<?>> params = new HashMap<>();
                    List<String> params = new ArrayList<>();
                    for(Parameter methodParam  : method.getParameters()){
                        RequestParam requestParam = methodParam.getAnnotation(RequestParam.class);
                        String paramName= methodParam.getType().getSimpleName();;
                        if (null != requestParam) {
                            paramName = requestParam.value();
                        }
                        params.add(paramName);
                    }



                    controllerInfo = new ControllerInfo(bean,method,params);
                    pathControllerMap.put(pathInfo,controllerInfo);
                    log.info("Add Controller RequestMethod:{}, RequestPath:{}, Controller:{}, Method:{}",
                            pathInfo.getHttpMethod(), pathInfo.getHttpPath(),
                            controllerInfo.getControllerInstance(), controllerInfo.getMethod().getName());
                     }
                }
            }
        }

        public Map<PathInfo,ControllerInfo> getPathControllerMap(){
            return pathControllerMap;
        }

    public ControllerInfo getController(String requestMethod, String requestPath) {
        PathInfo pathInfo = new PathInfo(requestMethod, requestPath);
        return pathControllerMap.get(pathInfo);
    }


}
