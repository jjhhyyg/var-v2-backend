package ustb.hyy.app.backend.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;

import lombok.extern.slf4j.Slf4j;
import ustb.hyy.app.backend.common.exception.BusinessException;

/**
 * 视频处理工具类（基于JavaCV）
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Slf4j
public class VideoUtils {

    /**
     * 解析视频时长（秒）
     *
     * @param videoPath 视频文件路径
     * @return 视频时长（秒）
     */
    public static int parseVideoDuration(String videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            // 获取视频时长（微秒）
            long lengthInMicros = grabber.getLengthInTime();

            // 转换为秒
            int durationInSeconds = (int) (lengthInMicros / 1_000_000);

            log.info("视频时长解析成功: {} 秒 ({} 分钟)", durationInSeconds, durationInSeconds / 60.0);

            return durationInSeconds;
        } catch (FrameGrabber.Exception e) {
            log.error("视频时长解析失败: {}", videoPath, e);
            throw new BusinessException("视频文件解析失败，请确认文件格式正确", e);
        }
    }

    /**
     * 获取视频基本信息
     *
     * @param videoPath 视频文件路径
     * @return 视频信息对象
     */
    public static VideoInfo getVideoInfo(String videoPath) {
        try (FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(videoPath)) {
            grabber.start();

            VideoInfo info = new VideoInfo();
            info.setDuration((int) (grabber.getLengthInTime() / 1_000_000));
            info.setWidth(grabber.getImageWidth());
            info.setHeight(grabber.getImageHeight());
            info.setFrameRate(grabber.getFrameRate());
            info.setTotalFrames(grabber.getLengthInFrames());
            info.setFormat(grabber.getFormat());

            // 获取视频编码格式
            String videoCodecName = grabber.getVideoCodecName();
            info.setCodec(videoCodecName);

            log.info("视频信息: {}x{}, {} fps, {} 帧, {} 秒, 编码: {}",
                    info.getWidth(), info.getHeight(), info.getFrameRate(),
                    info.getTotalFrames(), info.getDuration(), videoCodecName);

            return info;
        } catch (FrameGrabber.Exception e) {
            log.error("获取视频信息失败: {}", videoPath, e);
            throw new BusinessException("视频文件解析失败，请确认文件格式正确", e);
        }
    }

    /**
     * 检查视频编码是否需要重新编码
     *
     * @param codec 视频编码格式
     * @return true表示需要重新编码，false表示不需要
     */
    public static boolean needsReencoding(String codec) {
        if (codec == null || codec.isEmpty()) {
            return true;
        }

        // H264编码：h264, libx264, avc1等
        // H265编码：h265, hevc, libx265, hvc1等
        String codecLower = codec.toLowerCase();
        return !codecLower.contains("h264") &&
               !codecLower.contains("264") &&
               !codecLower.contains("avc") &&
               !codecLower.contains("h265") &&
               !codecLower.contains("265") &&
               !codecLower.contains("hevc") &&
               !codecLower.contains("hvc");
    }

    /**
     * 重新编码视频为H264格式
     *
     * @param inputPath 输入视频路径
     * @param outputPath 输出视频路径
     * @throws BusinessException 编码失败时抛出
     */
    public static void reencodeToH264(String inputPath, String outputPath) {
        log.info("开始重新编码视频为H264格式，输入: {}, 输出: {}", inputPath, outputPath);

        FFmpegFrameGrabber grabber = null;
        FFmpegFrameRecorder recorder = null;

        try {
            // 创建Frame抓取器
            grabber = new FFmpegFrameGrabber(inputPath);
            grabber.start();

            // 创建Frame录制器
            recorder = new FFmpegFrameRecorder(outputPath, grabber.getImageWidth(), grabber.getImageHeight());
            recorder.setVideoCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264);
            recorder.setFormat("mp4");
            recorder.setFrameRate(grabber.getFrameRate());
            recorder.setVideoBitrate(grabber.getVideoBitrate() > 0 ? grabber.getVideoBitrate() : 2000000);

            // 如果有音频流，也复制音频
            if (grabber.getAudioChannels() > 0) {
                recorder.setAudioChannels(grabber.getAudioChannels());
                recorder.setAudioCodec(org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC);
                recorder.setSampleRate(grabber.getSampleRate());
                recorder.setAudioBitrate(grabber.getAudioBitrate() > 0 ? grabber.getAudioBitrate() : 128000);
            }

            recorder.start();

            // 逐帧转码
            Frame frame;
            int frameCount = 0;
            while ((frame = grabber.grab()) != null) {
                recorder.record(frame);
                frameCount++;

                // 每处理1000帧输出一次日志
                if (frameCount % 1000 == 0) {
                    log.debug("已处理 {} 帧", frameCount);
                }
            }

            log.info("视频重新编码完成，共处理 {} 帧", frameCount);

        } catch (Exception e) {
            // 删除可能生成的不完整输出文件
            try {
                File outputFile = new File(outputPath);
                if (outputFile.exists()) {
                    outputFile.delete();
                    log.info("已删除不完整的输出文件: {}", outputPath);
                }
            } catch (Exception deleteEx) {
                log.warn("删除不完整的输出文件失败: {}", outputPath, deleteEx);
            }

            log.error("视频重新编码失败", e);
            throw new BusinessException("视频重新编码失败: " + e.getMessage(), e);
        } finally {
            // 释放资源
            try {
                if (recorder != null) {
                    recorder.stop();
                    recorder.release();
                }
            } catch (Exception e) {
                log.warn("关闭录制器失败", e);
            }

            try {
                if (grabber != null) {
                    grabber.stop();
                    grabber.release();
                }
            } catch (Exception e) {
                log.warn("关闭抓取器失败", e);
            }
        }
    }

    /**
     * 应用 faststart 优化到视频文件
     * faststart 将 moov atom 移到文件开头，使视频支持浏览器流式播放（边下载边播放）
     *
     * @param videoPath 视频文件路径
     * @throws BusinessException faststart 应用失败时抛出
     */
    public static void applyFaststart(String videoPath) {
        log.info("开始应用 faststart 优化: {}", videoPath);

        Path videoFile = Paths.get(videoPath);
        if (!Files.exists(videoFile)) {
            throw new BusinessException("视频文件不存在: " + videoPath);
        }

        // 创建临时输出文件
        String tempOutput = videoPath + ".faststart.tmp.mp4";
        Path tempFile = Paths.get(tempOutput);

        ProcessBuilder processBuilder = new ProcessBuilder(
            "ffmpeg",
            "-i", videoPath,
            "-c", "copy",          // 不重新编码，只复制流
            "-movflags", "faststart",  // 应用 faststart 标志
            "-y",                  // 覆盖输出文件
            tempOutput
        );

        // 合并标准错误和标准输出
        processBuilder.redirectErrorStream(true);

        Process process = null;
        try {
            // 启动 FFmpeg 进程
            process = processBuilder.start();

            // 读取输出（用于日志记录和错误诊断）
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }

            // 等待进程完成（最多等待5分钟）
            boolean finished = process.waitFor(5, TimeUnit.MINUTES);

            if (!finished) {
                process.destroy();
                throw new BusinessException("FFmpeg 处理超时（超过5分钟）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                log.error("FFmpeg 执行失败，退出码: {}, 输出:\n{}", exitCode, output);
                throw new BusinessException("FFmpeg 执行失败，退出码: " + exitCode);
            }

            // 验证临时文件
            if (!Files.exists(tempFile)) {
                throw new BusinessException("FFmpeg 未生成输出文件");
            }

            long tempFileSize = Files.size(tempFile);
            if (tempFileSize == 0) {
                throw new BusinessException("FFmpeg 生成的文件大小为 0");
            }

            // 替换原文件
            long originalSize = Files.size(videoFile);
            Files.move(tempFile, videoFile, StandardCopyOption.REPLACE_EXISTING);

            log.info("faststart 优化完成: {} (原始: {} MB, 优化后: {} MB)",
                    videoPath,
                    originalSize / 1024 / 1024,
                    tempFileSize / 1024 / 1024);

        } catch (IOException e) {
            log.error("文件操作失败", e);
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException cleanupEx) {
                log.warn("清理临时文件失败: {}", tempOutput, cleanupEx);
            }
            throw new BusinessException("faststart 优化失败: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("FFmpeg 进程被中断", e);
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException cleanupEx) {
                log.warn("清理临时文件失败: {}", tempOutput, cleanupEx);
            }
            throw new BusinessException("faststart 优化被中断", e);
        } catch (BusinessException e) {
            // 清理临时文件
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException cleanupEx) {
                log.warn("清理临时文件失败: {}", tempOutput, cleanupEx);
            }
            throw e;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    /**
     * 视频信息实体
     */
    public static class VideoInfo {
        private int duration;      // 时长（秒）
        private int width;         // 宽度（像素）
        private int height;        // 高度（像素）
        private double frameRate;  // 帧率
        private int totalFrames;   // 总帧数
        private String format;     // 格式
        private String codec;      // 视频编码格式

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public double getFrameRate() {
            return frameRate;
        }

        public void setFrameRate(double frameRate) {
            this.frameRate = frameRate;
        }

        public int getTotalFrames() {
            return totalFrames;
        }

        public void setTotalFrames(int totalFrames) {
            this.totalFrames = totalFrames;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public String getCodec() {
            return codec;
        }

        public void setCodec(String codec) {
            this.codec = codec;
        }
    }

    /**
     * 将视频转码为H264编码格式
     *
     * @param inputPath 输入视频路径
     * @param outputPath 输出视频路径
     * @return 转码后的视频路径
     */
    public static String transcodeToH264(String inputPath, String outputPath) {
        try {
            // 检查输入文件是否存在
            File inputFile = new File(inputPath);
            if (!inputFile.exists()) {
                throw new BusinessException("输入视频文件不存在: " + inputPath);
            }

            // 创建输出目录
            File outputFile = new File(outputPath);
            File outputDir = outputFile.getParentFile();
            if (outputDir != null && !outputDir.exists()) {
                outputDir.mkdirs();
            }

            // 先检查视频是否已经是H264编码
            if (isH264Encoded(inputPath)) {
                log.info("视频已经是H264编码，直接复制文件: {}", inputPath);
                Files.copy(Paths.get(inputPath), Paths.get(outputPath), StandardCopyOption.REPLACE_EXISTING);
                return outputPath;
            }

            log.info("开始H264转码: {} -> {}", inputPath, outputPath);

            // 构建FFmpeg命令
            // -i: 输入文件
            // -c:v libx264: 使用H264编码器
            // -preset fast: 编码速度预设
            // -crf 23: 质量参数 (18-28, 越小质量越好)
            // -c:a aac: 音频使用AAC编码
            // -b:a 128k: 音频比特率
            // -y: 覆盖输出文件
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-i", inputPath,
                    "-c:v", "libx264",
                    "-preset", "fast",
                    "-crf", "23",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-y",
                    outputPath
            );

            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // 读取FFmpeg输出
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    log.debug("FFmpeg: {}", line);
                }
            }

            // 等待转码完成
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new BusinessException("视频转码失败，FFmpeg退出码: " + exitCode);
            }

            // 检查输出文件是否生成
            if (!outputFile.exists() || outputFile.length() == 0) {
                throw new BusinessException("转码后的视频文件未生成或为空");
            }

            log.info("H264转码完成: {}", outputPath);
            return outputPath;

        } catch (IOException | InterruptedException e) {
            log.error("视频转码失败: {}", inputPath, e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException("视频转码失败: " + e.getMessage(), e);
        }
    }

    /**
     * 检查视频是否已经是H264编码
     *
     * @param videoPath 视频文件路径
     * @return true如果是H264编码
     */
    private static boolean isH264Encoded(String videoPath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffprobe",
                    "-v", "error",
                    "-select_streams", "v:0",
                    "-show_entries", "stream=codec_name",
                    "-of", "default=noprint_wrappers=1:nokey=1",
                    videoPath
            );

            Process process = processBuilder.start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String codecName = reader.readLine();
                log.debug("视频编码格式: {}", codecName);
                return "h264".equalsIgnoreCase(codecName);
            }
        } catch (IOException e) {
            log.warn("检查视频编码格式失败，假定需要转码: {}", videoPath, e);
            return false;
        }
    }
}
