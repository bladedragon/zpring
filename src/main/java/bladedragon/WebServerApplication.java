package bladedragon;

import bladedragon.mvc.netty.server.WebServer;

public class WebServerApplication {

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.run(8080);

    }
}
