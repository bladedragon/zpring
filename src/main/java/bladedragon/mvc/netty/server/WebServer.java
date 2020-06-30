package bladedragon.mvc;

import bladedragon.mvc.netty.server.BaseServer;
import bladedragon.mvc.netty.server.ServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioChannelOption;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;


public class WebServer extends BaseServer{


    private int PORT;
    private ScheduledExecutorService executorService;

    static {
        init();
        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childOption(NioChannelOption.TCP_NODELAY, true)
                .option(NioChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ServerInitializer());
    }

    @Override
    public  void run(int port) {

        this.PORT = port;

        try {
            ChannelFuture channelFuture = serverBootstrap
                    .bind(new InetSocketAddress(port))
                    .sync()
                    .addListener(future -> {
                if (future.isSuccess()) {
                    logger.info(new Date() + ": 端口[" + PORT + "]绑定成功!");
                    System.out.println("端口绑定成功");
                } else {
                    logger.info("端口[" + PORT + "]绑定失败!");
                }
            });
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

}


