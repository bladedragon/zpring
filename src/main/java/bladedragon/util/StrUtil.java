package bladedragon.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public class StrUtil {

    public static boolean isBlank(String str){
        if(null == str || str.isEmpty()){
            return true;
        }else{
            return false;
        }
    }

    public static String lowerFirstCase(String str){
            char[] chars = str.toCharArray();
            if(chars[0] >= 65 && chars[0] <=90) {
                chars[0] += 32;
            }
            return String.valueOf(chars);
    }

    public static String upFirstCase(String str){
        char[] chars = str.toCharArray();
        if(chars[0] >=97 && chars[0] <= 122){
            chars[0] -= 32;
        }
        return String.valueOf(chars);
    }


    public static String headerFormat(String contentType,String charset){
        return contentType+";charset="+charset;
    }

    public static String sendFormat(String str){
        return "Send "+str+" error!";
    }

    /**
     * 存在改良空间
     * @param obj
     * @param charset
     * @return
     */
    public static String str(Object obj, Charset charset) {
        if (null == obj) {
            return null;
        } else if (obj instanceof String) {
            return (String)obj;
        } else if (obj instanceof byte[]) {
            return str((byte[])((byte[])obj), charset);
        } else if (obj instanceof Byte[]) {
            return str((Byte[])((Byte[])obj), charset);
        } else if (obj instanceof ByteBuffer) {
            return str((ByteBuffer)obj, charset);
        } else {
            return null != obj && obj.getClass().isArray() ? Arrays.toString((Object[]) obj) : obj.toString();
        }
    }

    public static void main(String[] args) {
        System.out.println(lowerFirstCase("Zbc"));
    }
}
