package top.naccl.dwz.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {
    SUCCESS(200, "成功"),
    BAD_REQUEST(400, "请求参数有误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),
    CONFLICT(409, "短链已存在"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    ERROR(500, "服务器内部错误");

    private final int code;
    private final String msg;
}
