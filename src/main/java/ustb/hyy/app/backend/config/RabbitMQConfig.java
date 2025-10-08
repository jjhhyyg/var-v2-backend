package ustb.hyy.app.backend.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Configuration
public class RabbitMQConfig {

    @Value("${app.queue.video-analysis}")
    private String videoAnalysisQueue;

    /**
     * 视频分析队列
     */
    @Bean
    public Queue videoAnalysisQueue() {
        return QueueBuilder.durable(videoAnalysisQueue)
                .build();
    }

    /**
     * 消息转换器（使用JSON格式）
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
