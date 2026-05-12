package top.naccl.dwz.common.dto.request;

import lombok.Data;

@Data
public class ShortenRequest {
    private String longUrl;
    private String customCode;
}
