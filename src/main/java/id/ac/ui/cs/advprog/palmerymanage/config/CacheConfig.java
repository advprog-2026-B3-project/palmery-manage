package id.ac.ui.cs.advprog.palmerymanage.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PLANTATION_CACHE = "plantations";

    /**
     * CacheManager berbasis Redis.
     * Hanya dibuat jika RedisConnectionFactory bean tersedia (Redis aktif).
     * Saat test dengan spring.cache.type=none atau RedisAutoConfiguration di-exclude,
     * bean ini TIDAK akan dibuat sehingga tidak menyebabkan error.
     *
     * Konfigurasi:
     * - Default TTL: 30 menit
     * - Key serializer: String (human-readable di redis-cli)
     * - Value serializer: JSON (tidak perlu implements Serializable)
     * - Null values tidak di-cache
     */
    @Bean
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
                )
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer)
                )
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        cacheConfigurations.put(
                PLANTATION_CACHE,
                defaultConfig.entryTtl(Duration.ofMinutes(30))
        );

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Fallback CacheManager jika Redis tidak tersedia (misal: saat unit test).
     * NoOpCacheManager tidak menyimpan apa-apa — semua @Cacheable tetap berjalan
     * tetapi tanpa efek caching. Ini aman untuk test environment.
     *
     * Hanya dibuat jika TIDAK ada CacheManager lain yang terdefinisi.
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager noOpCacheManager() {
        return new NoOpCacheManager();
    }
}
