package bladedragon.mvc.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class BaseServer implements Server {

    protected Logger logger = LoggerFactory.getLogger(getClass());
    protected static NioEventLoopGroup bossGroup;
    protected static NioEventLoopGroup workerGroup;
    protected static DefaultEventLoopGroup defaultGroup;
    protected static ServerBootstrap serverBootstrap;


    private static int nthreads = 8;


    public static void init(){
        defaultGroup = new DefaultEventLoopGroup(nthreads, new ThreadFactory() {
            private AtomicInteger index = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r,"DEFAULT_EVENTLOOP_GROUIP_"+index.incrementAndGet());
            }
        });

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
                return new Thread("WOREKER_"+index.incrementAndGet());
            }
        });

        serverBootstrap = new ServerBootstrap();
    }


    @Override
    public void shutdown() {
        if (defaultGroup != null) {
            defaultGroup.shutdownGracefully();
        }
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
