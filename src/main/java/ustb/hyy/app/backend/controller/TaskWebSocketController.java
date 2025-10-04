package ustb.hyy.app.backend.controller;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ustb.hyy.app.backend.dto.response.TaskStatusResponse;
import ustb.hyy.app.backend.service.AnalysisTaskService;

/**
 * WebSocket任务状态Controller
 * 处理任务状态的实时推送
 *
 * @author 侯阳洋
 * @since 2025-10-04
 */
@Tag(name = "WebSocket任务状态", description = "任务状态实时推送接口")
@Slf4j
@Controller
@RequiredArgsConstructor
public class TaskWebSocketController {

    private final AnalysisTaskService taskService;

    /**
     * 订阅任务状态
     * 客户端订阅 /topic/tasks/{taskId}/status 时触发
     */
    @SubscribeMapping("/tasks/{taskId}/status")
    public TaskStatusResponse subscribeTaskStatus(@DestinationVariable Long taskId) {
        log.info("客户端订阅任务状态，taskId: {}", taskId);
        // 返回当前任务状态
        return taskService.getTaskStatus(taskId);
    }

    /**
     * 订阅所有任务状态更新
     * 客户端订阅 /topic/tasks/updates 时触发
     */
    @SubscribeMapping("/tasks/updates")
    public String subscribeAllTaskUpdates() {
        log.info("客户端订阅所有任务更新");
        return "已订阅任务列表更新";
    }

    /**
     * 处理客户端发送的消息（如果需要）
     * 客户端发送到 /app/tasks/ping
     */
    @MessageMapping("/tasks/ping")
    @SendTo("/topic/tasks/pong")
    public String ping(String message) {
        log.debug("收到ping消息: {}", message);
        return "pong";
    }
}
