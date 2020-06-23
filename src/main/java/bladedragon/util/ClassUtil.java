package bladedragon.util;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Slf4j
public class ClassUtil {

    public static ClassLoader getClassLoader(){
        return Thread.currentThread().getContextClassLoader();
    }

    public static Class<?> loadClass(String className){
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            log.error("load class error",e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(String className){
        try {
            Class<?> clazz = Class.forName(className);
            return (T) clazz.newInstance();
        } catch (Exception e) {
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> clazz){
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            log.error("newInstance error", e);
            throw new RuntimeException(e);
        }
    }

    public static String getClassNameByPath(Path classPath,Path basePath,String basePackage){
        String packageName = classPath.toString().replace(basePath.toString(),"");
        String className = (basePackage+packageName)
                .replace("/",".")
                .replace("\\",".")
                .replace(".class","");
        return className;
    }

    public static List<String> getClassNamesByPath(String basePackage){
        URL url = Object.class.getResource("/" + basePackage.replaceAll("\\.", "/"));
        List<String> list = null;
        if (null == url) {
            throw new RuntimeException("无法获取项目路径文件");
        }

        if (url.getProtocol().equalsIgnoreCase("file")) {
            File file = new File(url.getFile());
            Path basePath = file.toPath();

            try {
                list = Files.walk(basePath)
                        .filter(path -> path.toFile().getName().endsWith(".class"))
                        .map(path -> getClassNameByPath(path,basePath,basePackage))
                        .collect(Collectors.toList());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return list;
    }

    public static void main(String[] args) throws IOException {
        String basePackage = "bladedragon.core";
        URL url =Thread.currentThread().getContextClassLoader().getResource(basePackage.replace(".","/"));
        File file = new File(url.getFile());
        Path basePath = file.toPath();
        Set<Object> set = Files.walk(basePath)
                .filter(path -> path.toFile().getName().endsWith(".class"))
//                .map(path -> getClassNameByPath(path,basePath,basePackage))
                .collect(Collectors.toSet());
        System.out.println(basePath);
//        getClassNameByPath(path,)
    }






}
