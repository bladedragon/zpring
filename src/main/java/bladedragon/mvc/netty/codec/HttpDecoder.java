package bladedragon.mvc.netty.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HttpDecoder extends MessageToMessageDecoder<FullHttpRequest> {
        @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest msg, List<Object> out) {
            System.out.println("运行到这里decoder");
        if (!msg.decoderResult().isSuccess()){
//            sendError(ctx);
            return;
        }
        final Request request = Request.build(ctx, msg);

        out.add(request);
    }
}
