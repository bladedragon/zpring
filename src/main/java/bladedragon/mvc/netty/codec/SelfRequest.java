package bladedragon.mvc.netty.codec;


import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class SelfRequest {

    public static final HttpDataFactory HTTP_DATA_FACTORY = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private HttpRequest request;

    private String path;
    private String ip;
    private Map<String,String> headers = new HashMap<>();
    private Map<String,Object> params = new HashMap<>();
    private Map<String, Cookie> cookies = new HashMap<>();

    private SelfRequest(ChannelHandlerContext ctx,HttpRequest req){
        this.request = req;
        String uri = req.uri();
        this.path = getPath(uri);
        this.ip = getIp(ctx);
        putHeaderAndCookies(req.headers());
        putParams(new QueryStringDecoder(uri));
        if(req.method() != HttpMethod.GET && !"application/octet-stream".equals(req.headers().get("Content-Type"))){
            HttpPostRequestDecoder decoder = null;
            try{
                decoder = new HttpPostRequestDecoder(HTTP_DATA_FACTORY,req);
                putParams(decoder);

            }finally {
                if(null != decoder){
                    decoder.destroy();
                    decoder = null;
                }
            }
        }
    }

    /**
     * 获得版本信息
     *
     * @return 版本
     */
    public String getProtocolVersion() {
        return request.protocolVersion().text();
    }

    /**
     * 获得URI（带参数的路径）
     *
     * @return URI
     */
    public String getUri() {
        return request.uri();
    }

    /**
     * @return 获得path（不带参数的路径）
     */
    public String getPath() {
        return path;
    }

    /**
     * 获得Http方法
     *
     * @return Http method
     */
    public String getMethod() {
        return request.method().name();
    }

    /**
     * 获得IP地址
     *
     * @return IP地址
     */
    public String getIp() {
        return ip;
    }
    /**
     * 获得所有头信息
     *
     * @return 头信息Map
     */
    public Map<String, String> getHeaders() {
        return headers;
    }
    /**
     * 使用ISO8859_1字符集获得Header内容<br>
     * 由于Header中很少有中文，故一般情况下无需转码
     *
     * @param headerKey 头信息的KEY
     * @return 值
     */
    public String getHeader(String headerKey) {
        return headers.get(headerKey);
    }
    /**
     * @return 客户浏览器是否为IE
     */
    public boolean isIE() {
        String userAgent = getHeader("User-Agent");
        if (null != userAgent && !userAgent.isEmpty()) {
            userAgent = userAgent.toUpperCase();
            if (userAgent.contains("MSIE") || userAgent.contains("TRIDENT")) {
                return true;
            }
        }
        return false;
    }

    public boolean isKeepAlive() {
        final String connectionHeader = getHeader(HttpHeaderNames.CONNECTION.toString());
        // 无论任何版本Connection为close时都关闭连接
        if (HttpHeaderValues.CLOSE.toString().equalsIgnoreCase(connectionHeader)) {
            return false;
        }

        // HTTP/1.0只有Connection为Keep-Alive时才会保持连接
        if (HttpVersion.HTTP_1_0.text().equals(getProtocolVersion())) {
            if (false == HttpHeaderValues.KEEP_ALIVE.toString().equalsIgnoreCase(connectionHeader)) {
                return false;
            }
        }
        // HTTP/1.1默认打开Keep-Alive
        return true;
    }


    private void putParams(HttpPostRequestDecoder decoder) {
        if(null != decoder){
            for(InterfaceHttpData data : decoder.getBodyHttpDatas()){
                putParam(data);
            }
        }
    }

    private void putParams(QueryStringDecoder decoder) {
        if(null != decoder){
            List<String> valueList = null;
            for(Map.Entry<String,List<String>> entry : decoder.parameters().entrySet()){
                valueList =entry.getValue();
                if(null != valueList){
                    putParam(entry.getKey(), 1 == valueList.size() ? valueList.get(0) : valueList);
                }
            }
        }
    }

    private void putParam(InterfaceHttpData data) {
        final InterfaceHttpData.HttpDataType dataType = data.getHttpDataType();
        if (dataType == InterfaceHttpData.HttpDataType.Attribute) {
            //普通参数
            Attribute attribute = (Attribute) data;
            try {
                this.putParam(attribute.getName(), attribute.getValue());
            } catch (IOException e) {
                log.error(e.toString());
            }
        }else if(dataType == InterfaceHttpData.HttpDataType.FileUpload){
            //文件
            FileUpload fileUpload = (FileUpload) data;
            if(fileUpload.isCompleted()){
                try {
                    this.putParam(data.getName(), fileUpload.getFile());
                } catch (IOException e) {
                    log.error(e+ "Get file param [{}] error!", data.getName());
                }
            }
        }
    }

    protected void putParam(String key, Object value) {
        this.params.put(key, value);
    }

    private void putHeaderAndCookies(HttpHeaders headers) {
        for(Map.Entry<String,String> entry : headers){
            this.headers.put(entry.getKey(),entry.getValue());
        }

        final String cookieString =this.headers.get(HttpHeaderNames.COOKIE.toString());
        if(null != cookieString && !cookieString.isEmpty()){
            final Set<Cookie> cookieSet = ServerCookieDecoder.LAX.decode(cookieString);
            cookieSet.stream().forEach(cookie ->  this.cookies.put(cookie.name(),cookie));
        }
    }

    private String getIp(ChannelHandlerContext ctx) {
       String ipStr = getHeader("X-Forwarded-For");
       String ipAddress = null;
        try {
           if(ipStr != null && !ipStr.isEmpty()){
               ipAddress = InetAddress.getByName(ipStr).getHostAddress();
           }else{
               final InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
               ipAddress = insocket.getAddress().getHostAddress();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return ipAddress;
    }

    private String getPath(String uriStr) {
        URI uri = null;
        try {
            uri = new URI(uriStr);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return uri.getPath();

    }

    public Map<String,Object> getParams(){
        return this.params;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("\r\nprotocolVersion: ").append(getProtocolVersion()).append("\r\n");
        sb.append("uri: ").append(getUri()).append("\r\n");
        sb.append("path: ").append(path).append("\r\n");
        sb.append("method: ").append(getMethod()).append("\r\n");
        sb.append("ip: ").append(ip).append("\r\n");
        sb.append("headers:\r\n ");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }
        sb.append("params: \r\n");
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            sb.append("    ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\r\n");
        }

        return sb.toString();
    }

    public final static SelfRequest build(ChannelHandlerContext ctx, FullHttpRequest nettyRequest) {
        return new SelfRequest(ctx, nettyRequest);
    }




}
