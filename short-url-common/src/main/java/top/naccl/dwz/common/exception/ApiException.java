package top.naccl.dwz.common.exception;

import lombok.Getter;
import top.naccl.dwz.common.enums.ResultCode;

@Getter
public class ApiException extends RuntimeException {
    private final int code;

    public ApiException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
    }
}
