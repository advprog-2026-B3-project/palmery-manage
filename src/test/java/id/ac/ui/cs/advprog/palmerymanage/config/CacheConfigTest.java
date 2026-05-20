package id.ac.ui.cs.advprog.palmerymanage.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CacheConfigTest {

    private final CacheConfig cacheConfig = new CacheConfig();

    @Test
    void cacheManager_withRedisConnectionFactory_returnsRedisCacheManager() {
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        
        CacheManager manager = cacheConfig.cacheManager(factory);
        
        assertNotNull(manager);
        assertTrue(manager instanceof RedisCacheManager);
    }

    @Test
    void noOpCacheManager_returnsNoOpCacheManager() {
        CacheManager manager = cacheConfig.noOpCacheManager();
        
        assertNotNull(manager);
        assertTrue(manager instanceof NoOpCacheManager);
    }
}
