package top.naccl.dwz.common.dto.request;

import lombok.Data;

@Data
public class CustomShortenRequest {
    private String longUrl;
    private String customCode;
}
