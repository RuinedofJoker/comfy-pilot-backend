package org.joker.comfypilot.common.application.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页请求基类
 * 所有分页查询请求都应继承此类
 */
@Data
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从 1 开始）
     */
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向（ASC/DESC）
     */
    private String orderDirection = "DESC";

    /**
     * 获取偏移量
     *
     * @return 偏移量
     */
    public long getOffset() {
        return (long) (pageNum - 1) * pageSize;
    }

    /**
     * 校验并修正分页参数
     */
    public void validate() {
        if (pageNum == null || pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大每页数量
        }
    }
}
