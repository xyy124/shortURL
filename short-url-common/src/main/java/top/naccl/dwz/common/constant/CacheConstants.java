package top.naccl.dwz.common.constant;

public interface CacheConstants {
    String BLOOM_FILTER_KEY = "bloom:short-url";
    String URL_MAPPING_KEY = "url:";
    String LOCK_KEY_PREFIX = "lock:url:";
    String EMPTY_VALUE = "___EMPTY___";

    long CAFFEINE_MAX_SIZE = 10_000;
    long CAFFEINE_EXPIRE_MINUTES = 15;

    long REDIS_BASE_TTL_SECONDS = 1800;
    long REDIS_TTL_RANDOM_RANGE = 300;
    long NULL_VALUE_TTL_SECONDS = 300;
}
