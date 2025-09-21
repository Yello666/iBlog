package yellow.iblog.exception;


import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import yellow.iblog.Common.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler
    public ApiResponse<Exception> ExceptionHandler(Exception e){
        log.error("出错了",e);
        return ApiResponse.fail("出错啦，请联系工作人员～");
    }
    @ExceptionHandler
    public ApiResponse<Exception> HttpRequestMethodNotSupportedExceptionHandler(HttpRequestMethodNotSupportedException e){
        log.error("请求类型出错或者路由出错",e);
        return ApiResponse.fail(400,"请求类型或者路由出错啦");
    }
    @ExceptionHandler(UserNotFoundException.class)
    public ApiResponse<Exception> handleUserNotFound(UserNotFoundException e) {
        return ApiResponse.fail(400,e.getMessage());
    }

    @ExceptionHandler(PasswordIncorrectException.class)
    public ApiResponse<Exception> handlePasswordError(PasswordIncorrectException e) {
        return ApiResponse.fail(400,e.getMessage());
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Exception> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        log.error("数据库完整性约束异常：必填字段值为空，或者字段值类型不匹配,或重复插入{}", e.getMessage());
        return ApiResponse.fail(400, "必填字段值为空");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ApiResponse<Exception> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("数据库完整性约束异常：重复插入:{}", e.getMessage());
        String message=e.getMessage();
        int i=message.indexOf("Duplicate entry");
        String errMsg=message.substring(i);
        String[] str=errMsg.split(" ");

        return ApiResponse.fail(400, str[2]+"已存在");
    }

    @ExceptionHandler(DataAccessException.class)
    public ApiResponse<Exception> handleDataAccess(DataAccessException e) {
        log.error("数据库访问异常{}", e.getMessage());
        return ApiResponse.fail(500, "数据库访问出错，请检查SQL或数据源配置");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Exception> handleNoResourceFoundException(NoResourceFoundException e) {
        log.error("资源路径不存在{}", e.getMessage());
        return ApiResponse.fail(404, "资源路径不存在，请检查路由是否正确");
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<Exception> handleAccessDeniedException(AccessDeniedException e) {
        log.error("权限不足:{}", e.getMessage());
        return ApiResponse.fail(403, e.getMessage());
        //401-没有登录，403-登录了但是没有管理员权限
    }



}
