package ustb.hyy.app.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应结果封装
 *
 * @author 侯阳洋
 * @since 2025-10-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码（从1开始）
     */
    private Integer page;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    private Boolean hasPrevious;

    /**
     * 从Spring Data的Page对象构建PageResult
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return PageResult.<T>builder()
                .items(page.getContent())
                .total(page.getTotalElements())
                .page(page.getNumber() + 1) // Page从0开始，转换为从1开始
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    /**
     * 从Spring Data的Page对象构建PageResult（带数据转换）
     */
    public static <T> PageResult<T> of(Page<?> page, List<T> items) {
        return PageResult.<T>builder()
                .items(items)
                .total(page.getTotalElements())
                .page(page.getNumber() + 1)
                .pageSize(page.getSize())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }
}
