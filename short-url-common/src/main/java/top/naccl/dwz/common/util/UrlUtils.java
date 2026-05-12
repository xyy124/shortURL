package top.naccl.dwz.common.util;

import java.util.regex.Pattern;

public class UrlUtils {
    private static final Pattern URL_PATTERN =
            Pattern.compile("^(https?://)?([\\w.-]+)+(:\\d+)?(/[\\w./%-]*)*\\??[\\w&=.-]*$");
    private static final Pattern CUSTOM_CODE_PATTERN =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9]{3,15}$");

    public static boolean checkURL(String url) {
        return url != null && URL_PATTERN.matcher(url).matches();
    }

    public static boolean validateCustomCode(String code) {
        return code != null && CUSTOM_CODE_PATTERN.matcher(code).matches();
    }
}
