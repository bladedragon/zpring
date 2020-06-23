package bladedragon.mvc.netty.handler;


import bladedragon.mvc.netty.codec.SelfRequest;
import bladedragon.mvc.netty.codec.SelfResponse;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

@ChannelHandler.Sharable
public class DispatchHandler extends SimpleChannelInboundHandler<SelfRequest> {
    public static final DispatchHandler INSTANCE = new DispatchHandler();
     DispatchHandler(){}

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, SelfRequest selfRequest) throws Exception {
        final SelfResponse response = SelfResponse.build(ctx,selfRequest);

    }
}
