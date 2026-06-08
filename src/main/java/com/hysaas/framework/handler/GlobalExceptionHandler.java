package com.hysaas.framework.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import com.hysaas.common.exception.BizException;
import com.hysaas.common.result.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** 全局异常处理，统一返回 R 格式 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public R<Void> handleBizException(BizException e) {
        return R.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public R<Void> handleNotLogin(NotLoginException e) {
        return R.fail(401, "未登录或登录已过期");
    }

    @ExceptionHandler(NotPermissionException.class)
    public R<Void> handleNotPermission(NotPermissionException e) {
        return R.fail(403, "无权限");
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public R<Void> handleValidation(Exception e) {
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException ex && ex.getBindingResult().hasErrors()) {
            message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        } else if (e instanceof BindException ex && ex.getBindingResult().hasErrors()) {
            message = ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage();
        }
        return R.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    public R<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return R.fail("系统繁忙，请稍后重试");
    }
}
