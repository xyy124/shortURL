package top.naccl.dwz.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ShortenResponse {
    private String shortCode;
    private String shortUrl;
    private String longUrl;
}
