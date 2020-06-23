package bladedragon.bean;

import bladedragon.core.annotation.Service;

@Service
public class WebServiceImp implements WebService {

    @Override
    public String helloWorld() {
        return "hello world";
    }
}
