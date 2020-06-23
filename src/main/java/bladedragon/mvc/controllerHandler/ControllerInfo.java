package bladedragon.mvc.controllerHandler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ControllerInfo {
    private Object controllerInstance;

    private Method method;

    private List<String> methodParameter;
}
