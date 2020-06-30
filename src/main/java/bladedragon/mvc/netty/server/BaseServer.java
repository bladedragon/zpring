package bladedragon.mvc.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseServer implements Server {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static NioEventLoopGroup bossGroup;
    protected static NioEventLoopGroup workerGroup;
    protected static ServerBootstrap serverBootstrap;


    private static int nthreads = Runtime.getRuntime().availableProcessors();


    public static void init(){

        bossGroup = new NioEventLoopGroup(nthreads, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"BOSS_"+index.incrementAndGet());
            }
        });

        workerGroup = new NioEventLoopGroup(nthreads, new ThreadFactory() {
            private AtomicInteger index=  new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"WOREKER_"+index.incrementAndGet());
            }
        });

        serverBootstrap = new ServerBootstrap();
    }


    @Override
    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
