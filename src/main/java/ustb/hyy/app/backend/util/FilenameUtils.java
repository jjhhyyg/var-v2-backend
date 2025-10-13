package ustb.hyy.app.backend.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件名时间戳工具类
 * 提供带时间戳的文件名生成和更新功能
 */
public class FilenameUtils {

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile("_(\\d{8}_\\d{6})$");
    
    /**
     * 生成当前时间戳字符串（格式：yyyyMMdd_HHmmss）
     */
    public static String generateTimestamp() {
        return LocalDateTime.now().format(TIMESTAMP_FORMAT);
    }
    
    /**
     * 从文件名中提取时间戳和基础名称
     * 
     * @param filename 文件名（可以包含或不包含扩展名）
     * @return 包含两个元素的数组：[基础名称（不含时间戳和扩展名）, 时间戳（如果存在则返回，否则为null）]
     * 
     * 示例:
     *   "video_20240101_120000.mp4" -> ["video", "20240101_120000"]
     *   "video.mp4" -> ["video", null]
     *   "video_preprocessed_20240101_120000.mp4" -> ["video_preprocessed", "20240101_120000"]
     */
    public static String[] extractTimestampFromFilename(String filename) {
        // 移除扩展名
        String nameWithoutExt = removeExtension(filename);
        
        // 匹配时间戳模式：_yyyyMMdd_HHmmss
        Matcher matcher = TIMESTAMP_PATTERN.matcher(nameWithoutExt);
        
        if (matcher.find()) {
            String timestamp = matcher.group(1);
            String baseName = nameWithoutExt.substring(0, matcher.start());
            return new String[]{baseName, timestamp};
        } else {
            return new String[]{nameWithoutExt, null};
        }
    }
    
    /**
     * 生成带时间戳的文件名
     * 
     * @param baseName 基础文件名（可能已包含时间戳）
     * @param extension 文件扩展名（包括点，如 .mp4）
     * @param updateExisting 如果文件名已包含时间戳，是否更新为新时间戳
     * @return 带时间戳的完整文件名
     * 
     * 示例:
     *   generateFilenameWithTimestamp("video", ".mp4", true) -> "video_20240101_120000.mp4"
     *   generateFilenameWithTimestamp("video_20240101_120000", ".mp4", true) -> "video_20240102_130000.mp4"
     *   generateFilenameWithTimestamp("video_20240101_120000", ".mp4", false) -> "video_20240101_120000.mp4"
     */
    public static String generateFilenameWithTimestamp(String baseName, String extension, boolean updateExisting) {
        // 确保扩展名以点开头
        if (extension != null && !extension.isEmpty() && !extension.startsWith(".")) {
            extension = "." + extension;
        }
        if (extension == null) {
            extension = "";
        }
        
        // 提取基础名称和现有时间戳
        String[] parts = extractTimestampFromFilename(baseName);
        String base = parts[0];
        String existingTimestamp = parts[1];
        
        // 决定是否需要添加/更新时间戳
        if (existingTimestamp == null || updateExisting) {
            // 没有时间戳或需要更新时间戳
            String newTimestamp = generateTimestamp();
            return base + "_" + newTimestamp + extension;
        } else {
            // 保留现有时间戳
            return base + "_" + existingTimestamp + extension;
        }
    }
    
    /**
     * 为文件路径添加或更新时间戳
     * 
     * @param filepath 完整的文件路径
     * @param updateExisting 如果文件名已包含时间戳，是否更新为新时间戳
     * @return 带时间戳的完整文件路径
     * 
     * 示例:
     *   addOrUpdateTimestamp("/path/to/video.mp4", true) -> "/path/to/video_20240101_120000.mp4"
     *   addOrUpdateTimestamp("/path/to/video_20240101_120000.mp4", true) -> "/path/to/video_20240102_130000.mp4"
     *   addOrUpdateTimestamp("/path/to/video_20240101_120000.mp4", false) -> "/path/to/video_20240101_120000.mp4"
     */
    public static String addOrUpdateTimestamp(String filepath, boolean updateExisting) {
        int lastSeparator = Math.max(filepath.lastIndexOf('/'), filepath.lastIndexOf('\\'));
        String directory = lastSeparator >= 0 ? filepath.substring(0, lastSeparator + 1) : "";
        String filename = lastSeparator >= 0 ? filepath.substring(lastSeparator + 1) : filepath;
        
        // 提取扩展名和基础名称
        String extension = getExtension(filename);
        String baseName = removeExtension(filename);
        
        // 生成新文件名
        String newFilename = generateFilenameWithTimestamp(baseName, extension, updateExisting);
        
        // 返回完整路径
        return directory + newFilename;
    }
    
    /**
     * 移除文件名的扩展名
     */
    private static String removeExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(0, dotIndex) : filename;
    }
    
    /**
     * 获取文件扩展名（包括点）
     */
    private static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return dotIndex > 0 ? filename.substring(dotIndex) : "";
    }

    /**
     * 生成基于UUID和时间戳的文件名
     *
     * @param originalFilename 原始文件名（用于提取扩展名）
     * @return UUID_timestamp.extension 格式的文件名
     *
     * 示例:
     *   generateUuidFilename("video.mp4") -> "a1b2c3d4-e5f6-7890-abcd-ef1234567890_20240101_120000.mp4"
     */
    public static String generateUuidFilename(String originalFilename) {
        String extension = getExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        String timestamp = generateTimestamp();
        return uuid + "_" + timestamp + extension;
    }
}
