package bladedragon.mvc.netty.codec;

import bladedragon.util.FileProgressiveFutureListener;
import bladedragon.util.JsonUtil;
import bladedragon.util.StrUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Getter
public class SelfResponse {
    /** 返回内容类型：普通文本 */
    public final static String CONTENT_TYPE_TEXT = "text/plain";
    /** 返回内容类型：HTML */
    public final static String CONTENT_TYPE_HTML = "text/html";
    /** 返回内容类型：XML */
    public final static String CONTENT_TYPE_XML = "text/xml";
    /** 返回内容类型：JAVASCRIPT */
    public final static String CONTENT_TYPE_JAVASCRIPT = "application/javascript";
    /** 返回内容类型：JSON */
    public final static String CONTENT_TYPE_JSON = "application/json";
    public final static String CONTENT_TYPE_JSON_IE = "text/json";

    private ChannelHandlerContext ctx;
    private SelfRequest request;

    private HttpVersion httpVersion = HttpVersion.HTTP_1_1;
    private HttpResponseStatus status = HttpResponseStatus.OK;
    private String contentType = CONTENT_TYPE_HTML;
    private String charset = "UTF-8";
    private HttpHeaders headers = new DefaultHttpHeaders();
    private Set<io.netty.handler.codec.http.cookie.Cookie> cookies = new HashSet<Cookie>();
    private Object content = Unpooled.EMPTY_BUFFER;
    // 发送完成标记
    private boolean isSent;


    public SelfResponse(ChannelHandlerContext ctx, SelfRequest request) {
        this.ctx = ctx;
        this.request = request;
    }

    //----------------------------------------- self setting -------

    public Object getContent() {
        return content;
    }

    public SelfResponse setHttpVersion(HttpVersion httpVersion) {
        this.httpVersion = httpVersion;
        return this;
    }


    public SelfResponse setStatus(HttpResponseStatus status){
        this.status = status;
        return this;
    }

    public SelfResponse setStatus(int status) {
        return setStatus(HttpResponseStatus.valueOf(status));
    }

    public SelfResponse setContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public SelfResponse setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public SelfResponse addHeader(String name, Object value) {
        headers.add(name, value);
        return this;
    }

    public SelfResponse setHeader(String name, Object value) {
        headers.set(name, value);
        return this;
    }

    public SelfResponse setContentLength(long contentLength) {
        setHeader(HttpHeaderNames.CONTENT_LENGTH.toString(), contentLength);
        return this;
    }


    public SelfResponse setKeepAlive() {
        setHeader(HttpHeaderNames.CONNECTION.toString(), HttpHeaderValues.KEEP_ALIVE.toString());
        return this;
    }

    public SelfResponse addCookie(Cookie cookie) {
        cookies.add(cookie);
        return this;
    }

    public SelfResponse addCookie(String name, String value, int maxAgeInSeconds, String path, String domain) {
        Cookie cookie = new DefaultCookie(name, value);
        if (domain != null) {
            cookie.setDomain(domain);
        }
        cookie.setMaxAge(maxAgeInSeconds);
        cookie.setPath(path);
        return addCookie(cookie);
    }

    public SelfResponse setContent(String text){
        this.content =  Unpooled.copiedBuffer(text, Charset.forName(charset));
        return this;
    }

    public SelfResponse setJsonContent(Object o){
        return setContent(JsonUtil.toJson(o));
    }

    public SelfResponse setXmlContent(String contentText) {
        setContentType(CONTENT_TYPE_XML);
        return setContent(contentText);
    }

    public SelfResponse setContent(File file) {
        this.content = file;
        return this;
    }

    public SelfResponse setContent(ByteBuf byteBuf) {
        this.content = byteBuf;
        return this;
    }

    private DefaultHttpResponse toDefaultHttpResponse() {
        final DefaultHttpResponse defaultHttpResponse = new DefaultHttpResponse(httpVersion, status);

        fillHeadersAndCookies(defaultHttpResponse.headers());

        return defaultHttpResponse;
    }

    private FullHttpResponse toFullHttpResponse() {
        final ByteBuf byteBuf = (ByteBuf) content;
        final FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(httpVersion, status, byteBuf);

        // headers
        final HttpHeaders httpHeaders = fullHttpResponse.headers();
        fillHeadersAndCookies(httpHeaders);
        httpHeaders.set(HttpHeaderNames.CONTENT_LENGTH.toString(), byteBuf.readableBytes());

        return fullHttpResponse;
    }

    private void fillHeadersAndCookies(HttpHeaders httpHeaders) {
        httpHeaders.set(HttpHeaderNames.CONTENT_TYPE.toString(), StrUtil.headerFormat(contentType, charset));
        httpHeaders.set(HttpHeaderNames.CONTENT_ENCODING.toString(), charset);

        // Cookies
        for (Cookie cookie : cookies) {
            httpHeaders.add(HttpHeaderNames.SET_COOKIE.toString(), ServerCookieEncoder.LAX.encode(cookie));
        }
    }

    //-------------------------------------------build response ----------
    public ChannelFuture send(){
        ChannelFuture channelFuture;
        if (content instanceof File) {
            // 文件
            File file = (File) content;
            try {
                channelFuture = sendFile(file);
            } catch (Exception e) {
                log.error(StrUtil.sendFormat(file.toString()), e);
                channelFuture = sendError(HttpResponseStatus.FORBIDDEN, "");
            }
        } else {
            // 普通文本
            channelFuture = sendFull();
        }

        this.isSent = true;
        return channelFuture;
    }



    private ChannelFuture sendFull() {
        if (request != null && request.isKeepAlive()) {
            setKeepAlive();
            return ctx.writeAndFlush(this.toFullHttpResponse());
        } else {
            return sendAndCloseFull();
        }
    }

    private ChannelFuture sendFile(File file) throws IOException {
        final RandomAccessFile raf = new RandomAccessFile(file, "r");

        // 内容长度
        long fileLength = raf.length();
        this.setContentLength(fileLength);

        // 文件类型
        String contentType = URLConnection.getFileNameMap().getContentTypeFor(file.getName());
        if (null !=contentType && !contentType.isEmpty()) {
            // 无法识别默认使用数据流
            contentType = "application/octet-stream";
        }
        this.setContentType(contentType);

        ctx.write(this.toDefaultHttpResponse());
        ctx.write(new DefaultFileRegion(raf.getChannel(), 0, fileLength), ctx.newProgressivePromise()).addListener(FileProgressiveFutureListener.build(raf));

        return sendEmptyLast();
    }

    private ChannelFuture sendEmptyLast() {
        final ChannelFuture lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        if (false == request.isKeepAlive()) {
            lastContentFuture.addListener(ChannelFutureListener.CLOSE);
        }

        return lastContentFuture;
    }

    private ChannelFuture sendAndCloseFull() {
        return ctx.writeAndFlush(this.toFullHttpResponse()).addListener(ChannelFutureListener.CLOSE);
    }

    public ChannelFuture sendError(HttpResponseStatus status, String msg) {
        if (ctx.channel().isActive()) {
            return this.setStatus(status).setContent(msg).send();
        }
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("headers:\r\n ");
        for (Map.Entry<String, String> entry : headers.entries()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("content: ").append(StrUtil.str(content, CharsetUtil.UTF_8));

        return sb.toString();
    }

    //---------------build=-------------

    public static SelfResponse build(ChannelHandlerContext ctx, SelfRequest request) {
        return new SelfResponse(ctx, request);
    }

    protected static SelfResponse build(ChannelHandlerContext ctx) {
        return new SelfResponse(ctx, null);
    }



}
