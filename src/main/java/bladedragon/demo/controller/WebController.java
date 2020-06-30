package bladedragon.demo.controller;

import bladedragon.core.annotation.Autowired;
import bladedragon.core.annotation.Controller;
import bladedragon.core.annotation.RestController;
import bladedragon.demo.model.Person;
import bladedragon.demo.service.WebService;
import bladedragon.mvc.annotation.PostMapping;
import bladedragon.mvc.annotation.RequestMapping;
import bladedragon.mvc.annotation.RequestMethod;
import bladedragon.mvc.annotation.RequestParam;
import bladedragon.mvc.netty.codec.SelfResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/demo")
public class WebController {

    @Autowired
    private WebService webService;

    @RequestMapping(value = "/query",method = RequestMethod.GET)

    public void hello(@RequestParam("name") String name, SelfResponse response){
        log.info(webService.helloWorld());
        response.setJsonContent("{\"info\":\""+name+"\"}");
    }

    @PostMapping(value="/add")
    public void addHello(SelfResponse response){
        log.info("addHellow");
        String[] nickname = new String[]{"bladedragon","clement","dhdj"};

        Person person = new Person("zzz",nickname,18);
//        response.setJsonContent("{\"info\":\"success\"}");
        response.setJsonContent(person);
    }



}