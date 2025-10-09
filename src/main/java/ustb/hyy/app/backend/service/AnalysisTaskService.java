package ustb.hyy.app.backend.service;

import org.springframework.data.domain.Pageable;

import ustb.hyy.app.backend.common.response.PageResult;
import ustb.hyy.app.backend.dto.request.ProgressUpdateRequest;
import ustb.hyy.app.backend.dto.request.ResultSubmitRequest;
import ustb.hyy.app.backend.dto.request.TaskUploadRequest;
import ustb.hyy.app.backend.dto.response.TaskResponse;
import ustb.hyy.app.backend.dto.response.TaskResultResponse;
import ustb.hyy.app.backend.dto.response.TaskStatusResponse;

/**
 * 分析任务Service接口
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
public interface AnalysisTaskService {

    /**
     * 上传视频并创建分析任务
     *
     * @param request 任务上传请求
     * @return 任务响应
     */
    TaskResponse uploadTask(TaskUploadRequest request);

    /**
     * 手动开始任务分析
     *
     * @param taskId 任务ID
     */
    void startAnalysis(Long taskId);

    /**
     * 重新分析任务
     * 清除旧的分析结果并重新发送到AI处理队列
     *
     * @param taskId 任务ID
     */
    void reanalyzeTask(Long taskId);

    /**
     * 获取任务详情
     *
     * @param taskId 任务ID
     * @return 任务响应
     */
    TaskResponse getTask(Long taskId);

    /**
     * 获取任务状态
     *
     * @param taskId 任务ID
     * @return 任务状态响应
     */
    TaskStatusResponse getTaskStatus(Long taskId);

    /**
     * 更新任务进度（AI模块回调）
     *
     * @param taskId  任务ID
     * @param request 进度更新请求
     */
    void updateProgress(Long taskId, ProgressUpdateRequest request);

    /**
     * 提交分析结果（AI模块回调）
     *
     * @param taskId  任务ID
     * @param request 结果提交请求
     */
    void submitResult(Long taskId, ResultSubmitRequest request);

    /**
     * 获取分析结果
     *
     * @param taskId 任务ID
     * @return 任务结果响应
     */
    TaskResultResponse getTaskResult(Long taskId);

    /**
     * 分页获取任务列表
     *
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    PageResult<TaskResponse> listTasks(Pageable pageable);

    /**
     * 根据状态分页获取任务列表
     *
     * @param status   任务状态
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    PageResult<TaskResponse> listTasksByStatus(String status, Pageable pageable);

    /**
     * 删除任务（级联删除所有相关数据）
     *
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);

    /**
     * 更新任务的结果视频路径
     *
     * @param taskId 任务ID
     * @param resultVideoPath 结果视频路径
     */
    void updateResultVideoPath(Long taskId, String resultVideoPath);

    /**
     * 更新任务的预处理视频路径
     *
     * @param taskId 任务ID
     * @param preprocessedVideoPath 预处理视频路径
     */
    void updatePreprocessedVideoPath(Long taskId, String preprocessedVideoPath);

    /**
     * 更新任务的模型版本
     *
     * @param taskId 任务ID
     * @param modelVersion 模型版本
     */
    void updateModelVersion(Long taskId, String modelVersion);
}
