package org.joker.comfypilot.common.infrastructure.persistence.annotation;

import java.lang.annotation.*;

/**
 * 唯一键注解
 * 用于标记 PO 类中的唯一索引字段
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>单字段唯一索引：直接在字段上添加 @UniqueKey</li>
 *   <li>联合唯一索引：在多个字段上添加 @UniqueKey，并指定相同的 group 和不同的 order</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * // 单字段唯一索引
 * {@code @UniqueKey}
 * private String email;
 *
 * // 联合唯一索引
 * {@code @UniqueKey(group = "user_role", order = 1)}
 * private Long userId;
 * {@code @UniqueKey(group = "user_role", order = 2)}
 * private Long roleId;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UniqueKey {

    /**
     * 唯一键组名（用于联合唯一索引）
     * <p>默认为空字符串，表示单字段唯一索引</p>
     * <p>如果多个字段指定相同的 group，则表示这些字段组成联合唯一索引</p>
     *
     * @return 唯一键组名
     */
    String group() default "";

    /**
     * 在联合唯一索引中的顺序
     * <p>仅在联合唯一索引中有效，用于确定字段在索引中的顺序</p>
     * <p>默认为 0</p>
     *
     * @return 字段顺序
     */
    int order() default 0;
}
