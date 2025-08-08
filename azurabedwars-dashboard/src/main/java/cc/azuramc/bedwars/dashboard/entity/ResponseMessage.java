package cc.azuramc.bedwars.dashboard.entity;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 * @author An5w1r@163.com
 */
@Data
public class ResponseMessage<T> {
    private Integer code;
    private String message;
    private T data;

    public ResponseMessage(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ResponseMessage<T> success(T data) {
        return new ResponseMessage<>(HttpStatus.OK.value(), "success", data);
    }

    public static <T> ResponseMessage<T> error(String message) {
        return new ResponseMessage<>(HttpStatus.BAD_REQUEST.value(), message, null);
    }

    public static <T> ResponseMessage<T> error(HttpStatus status, String message) {
        return new ResponseMessage<>(status.value(), message, null);
    }

    public static <T> ResponseMessage<T> notFound(String message) {
        return new ResponseMessage<>(HttpStatus.NOT_FOUND.value(), message, null);
    }
}
