package top.naccl.dwz.common.util;

public class Base62Utils {
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int BASE = 62;

    public static String encode(long value) {
        StringBuilder sb = new StringBuilder();
        while (value > 0) {
            sb.append(BASE62.charAt((int) (value % BASE)));
            value /= BASE;
        }
        while (sb.length() < 6) {
            sb.append('0');
        }
        return sb.reverse().toString();
    }
}
