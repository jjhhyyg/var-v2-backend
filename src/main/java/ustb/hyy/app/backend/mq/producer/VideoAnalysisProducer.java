package ustb.hyy.app.backend.mq.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ustb.hyy.app.backend.mq.message.VideoAnalysisMessage;

/**
 * 视频分析消息生产者
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VideoAnalysisProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.queue.video-analysis}")
    private String videoAnalysisQueue;

    /**
     * 发送视频分析任务到队列
     *
     * @param message 视频分析消息
     */
    public void sendAnalysisTask(VideoAnalysisMessage message) {
        try {
            rabbitTemplate.convertAndSend(videoAnalysisQueue, message);
            log.info("视频分析任务已发送到MQ，taskId: {}, queue: {}",
                    message.getTaskId(), videoAnalysisQueue);
        } catch (Exception e) {
            log.error("发送视频分析任务到MQ失败，taskId: {}", message.getTaskId(), e);
            throw new RuntimeException("发送任务到消息队列失败", e);
        }
    }
}
