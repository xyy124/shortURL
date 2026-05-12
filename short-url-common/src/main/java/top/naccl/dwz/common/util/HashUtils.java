package top.naccl.dwz.common.util;

import java.nio.charset.StandardCharsets;

public class HashUtils {
    public static String murmurHash32ToBase62(String str) {
        int hash = cn.hutool.core.util.HashUtil.murmur32(str.getBytes(StandardCharsets.UTF_8));
        return Base62Utils.encode(Math.abs((long) hash));
    }
}
