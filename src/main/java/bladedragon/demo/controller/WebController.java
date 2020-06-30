package bladedragon.demo.controller;

import bladedragon.core.annotation.Autowired;
import bladedragon.core.annotation.Controller;
import bladedragon.demo.service.WebService;
import bladedragon.mvc.annotation.RequestMapping;
import bladedragon.mvc.annotation.RequestMethod;
import bladedragon.mvc.annotation.RequestParam;
import bladedragon.mvc.netty.codec.SelfResponse;
import lombok.extern.slf4j.Slf4j;

@Controller
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

    @RequestMapping(value="/add",method = RequestMethod.POST)
    public void addHello(SelfResponse response){
        log.info("addHellow");
        response.setJsonContent("{\"info\":\"success\"}");
    }



}