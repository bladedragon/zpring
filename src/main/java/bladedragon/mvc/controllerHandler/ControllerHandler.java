package bladedragon.mvc.controllerHandler;

import bladedragon.core.BeanFactory;
import bladedragon.core.annotation.Controller;
import bladedragon.core.annotation.RestController;
import bladedragon.core.ioc.IocContext;
import bladedragon.mvc.annotation.RequestMapping;
import bladedragon.mvc.annotation.RequestMethod;
import bladedragon.mvc.annotation.RequestParam;
import bladedragon.mvc.netty.codec.SelfRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
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
        IocContext context = new IocContext();
        initRequestMapping();
        log.info("==>pathControllerMap: "+ pathControllerMap);
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
            if(!clazz.isAnnotationPresent(Controller.class) && !clazz.isAnnotationPresent(RestController.class)){
                continue;
            }
            String baseUrl = "";
            String httpMethod;
            if(clazz.isAnnotationPresent(RequestMapping.class)){
                RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
                baseUrl =requestMapping.value();
                RequestMethod requestMethod = requestMapping.method();
                httpMethod = requestMethod.toString();
                log.info("==> httpMethod: "+httpMethod);
            }
            Method[] methods = clazz.getDeclaredMethods();
            for(Method method : methods){
                log.info("==> methods iterator: "+method.getName());
                if(method.isAnnotationPresent(RequestMapping.class)){
                    //添加Path
                    RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                    log.info("==> RequestMapping: "+ requestMapping);
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
                       if(requestParam != null){
                           String paramName = requestParam.value();
                           params.add(paramName);
                           //得到的是各参数的参数类型
                           log.info("addParamName: {}",paramName);
                       }else{
                           if(methodParam.getType().getSimpleName().equals("SelfRequest")){
                              String paramName = "SelfRequest";
                              params.add(paramName);
                               //得到的是各参数的参数类型
                               log.info("addParamName: {}",paramName);
                           }else if("SelfResponse".equals(methodParam.getType().getSimpleName())){
                               String paramName = "SelfResponse";
                               params.add(paramName);
                               //得到的是各参数的参数类型
                               log.info("addParamName: {}",paramName);
                           }
                       }
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
        log.info(" ==> in Method getController , pathControllerMap: "+pathControllerMap);
        log.info("==> requestMethod :" + requestMethod+", requestPath: "+requestPath);
        PathInfo pathInfo = new PathInfo(requestPath, requestMethod);
        return pathControllerMap.get(pathInfo);
    }


}
