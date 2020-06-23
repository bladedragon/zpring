package bladedragon.mvc.controllerHandler;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PathInfo {
    private String httpPath;

    private String httpMethod;
}
