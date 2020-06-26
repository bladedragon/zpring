package bladedragon.demo.controller;

import bladedragon.core.annotation.Autowired;
import bladedragon.core.annotation.Controller;
import bladedragon.demo.service.WebService;
import bladedragon.mvc.annotation.RequestMapping;
import bladedragon.mvc.annotation.RequestMethod;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WebController {

    @Autowired
    private WebService webService;

    @RequestMapping(value = "/demo",method = RequestMethod.GET)
    public void hello(){
        log.info(webService.helloWorld());
    }
}