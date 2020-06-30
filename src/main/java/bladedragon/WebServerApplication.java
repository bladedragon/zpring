package bladedragon;

import bladedragon.mvc.WebServer;

public class WebServerApplication {

    public static void main(String[] args) {
        WebServer server = new WebServer();
        server.run(8080);

    }
}
