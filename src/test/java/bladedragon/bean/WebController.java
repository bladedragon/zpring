package bladedragon.bean;

import bladedragon.core.annotation.Autowired;
import bladedragon.core.annotation.Controller;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class WebController {

    @Autowired
    private WebService webService;

    public void hello(){
        log.info(webService.helloWorld());
    }
}
