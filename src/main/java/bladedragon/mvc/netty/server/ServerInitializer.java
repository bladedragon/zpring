package bladedragon.mvc.netty.server;

import bladedragon.mvc.netty.codec.HttpDecoder;
import bladedragon.mvc.netty.codec.HttpEncoder;
import bladedragon.mvc.netty.codec.PacketCodecHandler;
import bladedragon.mvc.netty.handler.DispatchHandler;
import bladedragon.mvc.netty.handler.IdleCheckHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;

import java.net.Socket;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {

    private GlobalTrafficShapingHandler gtsHandler = new GlobalTrafficShapingHandler(new NioEventLoopGroup(),
            100 * 1024 * 1024, 100 * 1024 * 1024);

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline()
                .addLast(new LoggingHandler(LogLevel.DEBUG))
                .addLast("TrafficShapingHandler",gtsHandler)
                .addLast("idleHandler",new IdleCheckHandler())
                .addLast(new HttpRequestDecoder())
                .addLast(new HttpObjectAggregator(65536))
//                .addLast(new HttpDecoder())

                .addLast(new HttpResponseEncoder())
                .addLast(PacketCodecHandler.INSTANCE)
//                .addLast(new HttpEncoder())

                .addLast(new LoggingHandler(LogLevel.INFO))
                .addLast(DispatchHandler.INSTANCE);

    }
}
