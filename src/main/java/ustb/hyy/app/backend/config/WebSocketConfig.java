package ustb.hyy.app.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 使用STOMP协议进行消息传递
 *
 * @author 侯阳洋
 * @since 2025-10-04
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单的消息代理，用于向客户端推送消息
        // /topic 用于广播消息（一对多）
        // /queue 用于点对点消息（一对一）
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置客户端发送消息的前缀
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，客户端通过此端点连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域，生产环境应该设置具体域名
                .withSockJS(); // 启用SockJS支持，用于浏览器不支持WebSocket时的降级方案
    }
}
