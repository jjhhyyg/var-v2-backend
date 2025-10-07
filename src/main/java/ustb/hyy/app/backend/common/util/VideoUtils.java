package ustb.hyy.app.backend.common.util;

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
}
