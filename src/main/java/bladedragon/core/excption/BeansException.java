package bladedragon.core.excption;

import com.sun.istack.internal.Nullable;

public abstract class BeansException  extends  RuntimeException{
    public BeansException(String msg){
            super(msg);
        }
    public BeansException(@Nullable String msg, @Nullable Throwable cause) {
        super(msg, cause);
    }
}
