package org.joker.comfypilot.common.annotation;

import java.lang.annotation.*;

/**
 * 工具集注解
 * 用于标记工具类，为工具名称添加统一前缀
 *
 * <p>使用场景：</p>
 * <ul>
 *   <li>为同一类工具添加统一的命名前缀，便于分类和识别</li>
 *   <li>避免不同工具集之间的命名冲突</li>
 * </ul>
 *
 * <p>示例：</p>
 * <pre>
 * {@code @ToolSet("file_")}
 * public class FileSystemTools {
 *     // 工具方法 readFile 将被注册为 file_readFile
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ToolSet {

    /**
     * 工具名称前缀
     * <p>将自动添加到该类中所有工具方法的名称前面</p>
     * <p>默认为空字符串，表示不添加前缀</p>
     *
     * @return 工具名称前缀
     */
    String value() default "";

    /**
     * 工具名称前缀（与 value 等效）
     * <p>提供别名以支持更明确的语义</p>
     *
     * @return 工具名称前缀
     */
    String toolPrefix() default "";
}
