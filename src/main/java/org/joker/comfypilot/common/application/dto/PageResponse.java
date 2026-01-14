package org.joker.comfypilot.common.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页响应基类
 *
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

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
     * 构造分页响应
     *
     * @param records  数据列表
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     */
    public PageResponse(List<T> records, Long total, Integer pageNum, Integer pageSize) {
        this.records = records;
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = pageNum < totalPages;
        this.hasPrevious = pageNum > 1;
    }

    /**
     * 创建空分页响应
     *
     * @param <T> 数据类型
     * @return 空分页响应
     */
    public static <T> PageResponse<T> empty() {
        return new PageResponse<>(List.of(), 0L, 1, 10, 0, false, false);
    }
}
