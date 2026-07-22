package com.example.realtime_message_application.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.example.realtime_message_application.component.RedisMessageSubscriber;
import com.example.realtime_message_application.component.RedisNotificationSubscriber;

@Configuration
public class RedisConfig {

    public static final String CACHE_USERS         = "users";
    public static final String CACHE_USER_BY_NAME  = "userByName";
    public static final String CACHE_CONVERSATIONS = "conversations";
    public static final String CACHE_CONV_BY_USER  = "convByUser";

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

        // Cấu hình TTL riêng cho từng cache
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        cacheConfigurations.put(CACHE_USERS,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put(CACHE_USER_BY_NAME,
                defaultConfig.entryTtl(Duration.ofMinutes(30)));

        cacheConfigurations.put(CACHE_CONVERSATIONS,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put(CACHE_CONV_BY_USER,
                defaultConfig.entryTtl(Duration.ofMinutes(5)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public ChannelTopic chatEventTopic() {
        return new ChannelTopic("pubsub:chat-events");
    }

    @Bean
    public ChannelTopic notificationTopic() {
        return new ChannelTopic("pubsub:notification-events");
    }

    @Bean
    public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    @Bean
    public RedisTemplate<String, Object> redisPubSubTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return template;
    }

    // Đăng ký bộ lắng nghe sự kiện từ Redis
    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
            MessageListenerAdapter chatListenerAdapter, MessageListenerAdapter notificationListenerAdapter,
            ChannelTopic chatEventTopic, ChannelTopic notificationTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        // Lắng nghe topic chat events
        container.addMessageListener(chatListenerAdapter, chatEventTopic);

        // Lắng nghe topic notification events
        container.addMessageListener(notificationListenerAdapter, notificationTopic);
        return container;
    }

    // Kết nối dữ liệu nhận được từ Redis vào class Xử lý RedisMessageSubscriber
    @Bean
    public MessageListenerAdapter chatListenerAdapter(RedisMessageSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onMessage");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }

    @Bean
    public MessageListenerAdapter notificationListenerAdapter(RedisNotificationSubscriber subscriber) {
        MessageListenerAdapter adapter = new MessageListenerAdapter(subscriber, "onNotification");
        adapter.setSerializer(new StringRedisSerializer());
        return adapter;
    }
}
