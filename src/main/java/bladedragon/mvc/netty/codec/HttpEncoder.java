package bladedragon.mvc.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

@Slf4j
public class HttpEncoder extends MessageToMessageEncoder<SelfResponse> {


    @Override
    protected void encode(ChannelHandlerContext ctx, SelfResponse responseBody, List<Object> out){
        log.error("运行到encode");
        //获取服务端的字节流
        ByteBuf body = (ByteBuf) responseBody.getContent();
        //创建一个服务端响应对象
        FullHttpResponse response = new DefaultFullHttpResponse(
                responseBody.getHttpVersion(), OK, Unpooled.wrappedBuffer(body));

        response.headers()
                .set(CONTENT_TYPE, responseBody.getContentType())
                .setInt(CONTENT_LENGTH, response.content().readableBytes());
        //添加到结果中
        out.add(response);
    }
}