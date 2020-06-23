package bladedragon.util;


import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class PropertyUtil {

    public static Map<String,Object> loadSetting(String location){
        String suffix = location.split(".")[1] ;
        Map<String,Object> map = new HashMap<>();
        if(suffix == "yml") {
            Yaml yaml = new Yaml();
            map = doLoadYaml(location, yaml);
        }else if(suffix == "properties"){
            Properties properties = new Properties();
            doLoadProperty(location,properties);
            for(String name : properties.stringPropertyNames()){
                map.put(name,properties.getProperty(name));
            }
        }else{
            log.error("cannot read properties' location");
            return null;
        }
        return map;
    }

    private static void doLoadProperty(String location,Properties properties){
        InputStream inputStream = Object.class.getResourceAsStream(location);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Map<String,Object> doLoadYaml(String location, Yaml yaml){
        InputStream inputStream = Object.class.getResourceAsStream(location);
        Map<String,Object> map = yaml.load(inputStream);
        return map;
    }
}
