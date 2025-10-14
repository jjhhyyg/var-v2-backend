package ustb.hyy.app.backend.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.bytedeco.javacv.FFmpegFrameGrabber;
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

            log.info("视频信息: {}x{}, {} fps, {} 帧, {} 秒",
                    info.getWidth(), info.getHeight(), info.getFrameRate(),
                    info.getTotalFrames(), info.getDuration());

            return info;
        } catch (FrameGrabber.Exception e) {
            log.error("获取视频信息失败: {}", videoPath, e);
            throw new BusinessException("视频文件解析失败，请确认文件格式正确", e);
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
