package bladedragon.mvc.netty.handler;


import bladedragon.mvc.Dispatcher;
import bladedragon.mvc.netty.codec.SelfRequest;
import bladedragon.mvc.netty.codec.SelfResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ChannelHandler.Sharable
public class DispatchHandler extends SimpleChannelInboundHandler<SelfRequest> {
    public static final DispatchHandler INSTANCE = new DispatchHandler();

    DispatchHandler(){}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SelfRequest selfRequest) throws Exception {
        final SelfResponse response = SelfResponse.build(ctx, selfRequest);
        Dispatcher dispatcher = new Dispatcher();
        System.out.println("运行到这里--Dispacher");
        dispatcher.doDispatcher(response,selfRequest);
        if(ctx.channel().isActive() && ctx.channel().isWritable()){
            ctx.writeAndFlush(response);
        }else{
            log.error("cannot get response");
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}

