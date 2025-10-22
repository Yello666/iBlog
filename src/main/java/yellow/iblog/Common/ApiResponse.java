package yellow.iblog.Common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor
@Getter
@Setter
public class ApiResponse<T> {
//给前端的标准返回格式
    private int code;       // 状态码: 0=成功, 非0=失败
    private String message; // 错误提示 / 说明信息
    private T data;         // 返回的数据

    // 构造方法
    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ✅ 成功
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    // ✅ 自定义成功提示
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // ✅ 失败
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>(-1, message, null);
    }

    // ✅ 自定义错误码 + 消息
    public static <T> ApiResponse<T> fail(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<Exception> fail(int code,String message, Exception e) {
        return new ApiResponse<>(code,message,e);
    }
    public boolean IsSuccess(){
        return this.getCode() >=0;

    }
}
