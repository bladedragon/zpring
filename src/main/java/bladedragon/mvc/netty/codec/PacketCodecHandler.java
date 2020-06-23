package bladedragon.mvc.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@ChannelHandler.Sharable
public class PacketCodecHandler extends MessageToMessageCodec<FullHttpRequest, SelfResponse> {

    public static final PacketCodecHandler INSTANCE = new PacketCodecHandler();

    private  PacketCodecHandler(){}

    @Override
    protected void encode(ChannelHandlerContext ctx, SelfResponse response, List<Object> out) throws Exception {
        ByteBuf body = (ByteBuf) response.getContent();

        FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(
            response.getHttpVersion(),OK, Unpooled.wrappedBuffer(body));
        fullHttpResponse.headers().set(CONTENT_TYPE,response.getContentType())
                .set(CONTENT_LENGTH,fullHttpResponse.content().readableBytes());

        out.add(fullHttpResponse);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest req, List<Object> out) throws Exception {
        if(!req.decoderResult().isSuccess()){

        }
        final SelfRequest request = SelfRequest.build(ctx,req);
        out.add(request);
    }
}
