package bladedragon.mvc.netty.server;

public interface Server {

     void run(int port);

     void shutdown();

}
