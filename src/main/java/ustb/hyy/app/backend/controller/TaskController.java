package ustb.hyy.app.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ustb.hyy.app.backend.common.response.PageResult;
import ustb.hyy.app.backend.common.response.Result;
import ustb.hyy.app.backend.dto.request.ProgressUpdateRequest;
import ustb.hyy.app.backend.dto.request.ResultSubmitRequest;
import ustb.hyy.app.backend.dto.request.TaskUploadRequest;
import ustb.hyy.app.backend.dto.response.TaskResponse;
import ustb.hyy.app.backend.dto.response.TaskResultResponse;
import ustb.hyy.app.backend.dto.response.TaskStatusResponse;
import ustb.hyy.app.backend.service.AnalysisTaskService;

/**
 * 任务管理Controller
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Tag(name = "任务管理", description = "视频分析任务的增删改查接口")
@Slf4j
@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@Validated
public class TaskController {

    private final AnalysisTaskService taskService;

    /**
     * 上传视频并创建分析任务
     */
    @Operation(summary = "上传视频并创建分析任务", description = "上传视频文件并创建分析任务，任务创建后需手动调用开始分析接口")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<TaskResponse> uploadTask(@Validated @ModelAttribute TaskUploadRequest request) {
        log.info("接收到任务上传请求，文件名: {}", request.getVideo().getOriginalFilename());
        TaskResponse response = taskService.uploadTask(request);
        return Result.success("任务创建成功", response);
    }

    /**
     * 手动开始任务分析
     */
    @Operation(summary = "开始任务分析", description = "手动启动任务分析，将任务发送到AI处理队列")
    @PostMapping("/{taskId:[0-9]+}/start")
    public Result<String> startAnalysis(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("开始任务分析，taskId: {}", taskId);
        taskService.startAnalysis(taskId);
        return Result.success("任务已开始分析");
    }

    /**
     * 查询任务列表
     */
    @Operation(summary = "查询任务列表", description = "分页查询任务列表，支持按状态筛选")
    @GetMapping
    public Result<PageResult<TaskResponse>> listTasks(
            @Parameter(description = "页码（从0开始）") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "任务状态筛选") @RequestParam(required = false) String status) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        PageResult<TaskResponse> result;
        if (status != null && !status.isEmpty()) {
            result = taskService.listTasksByStatus(status, pageable);
        } else {
            result = taskService.listTasks(pageable);
        }

        return Result.success(result);
    }

    /**
     * 获取任务详情
     */
    @Operation(summary = "获取任务详情", description = "根据任务ID查询任务详细信息")
    @GetMapping("/{taskId:[0-9]+}")
    public Result<TaskResponse> getTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("查询任务详情，taskId: {}", taskId);
        TaskResponse response = taskService.getTask(taskId);
        return Result.success(response);
    }

    /**
     * 获取任务状态
     */
    @Operation(summary = "获取任务状态", description = "查询任务实时状态和进度（优先从Redis缓存获取）")
    @GetMapping("/{taskId:[0-9]+}/status")
    public Result<TaskStatusResponse> getTaskStatus(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        TaskStatusResponse response = taskService.getTaskStatus(taskId);
        return Result.success(response);
    }

    /**
     * 更新任务进度（AI模块回调）
     */
    @Operation(summary = "更新任务进度", description = "AI模块回调接口，更新任务处理进度并缓存到Redis")
    @PostMapping("/{taskId:[0-9]+}/progress")
    public Result<Void> updateProgress(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Validated @RequestBody ProgressUpdateRequest request) {
        log.info("接收到进度更新，taskId: {}, status: {}, progress: {}",
                taskId, request.getStatus(), request.getProgress());
        taskService.updateProgress(taskId, request);
        return Result.success();
    }

    /**
     * 提交分析结果（AI模块回调）
     */
    @Operation(summary = "提交分析结果", description = "AI模块回调接口，提交完整的分析结果（动态参数、事件、追踪物体）")
    @PostMapping("/{taskId:[0-9]+}/result")
    public Result<String> submitResult(
            @Parameter(description = "任务ID") @PathVariable Long taskId,
            @Validated @RequestBody ResultSubmitRequest request) {
        log.info("接收到结果提交，taskId: {}, status: {}", taskId, request.getStatus());
        taskService.submitResult(taskId, request);
        return Result.success("结果提交成功");
    }

    /**
     * 获取分析结果
     */
    @Operation(summary = "获取分析结果", description = "获取任务完整的分析结果，包括动态参数、异常事件、追踪物体及统计信息")
    @GetMapping("/{taskId:[0-9]+}/result")
    public Result<TaskResultResponse> getTaskResult(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("查询任务结果，taskId: {}", taskId);
        TaskResultResponse response = taskService.getTaskResult(taskId);
        return Result.success(response);
    }

    /**
     * 删除任务
     */
    @Operation(summary = "删除任务", description = "删除任务及其所有关联数据（级联删除）")
    @DeleteMapping("/{taskId:[0-9]+}")
    public Result<String> deleteTask(@Parameter(description = "任务ID") @PathVariable Long taskId) {
        log.info("删除任务，taskId: {}", taskId);
        taskService.deleteTask(taskId);
        return Result.success("任务删除成功");
    }
}
