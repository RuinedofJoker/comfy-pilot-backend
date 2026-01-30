package org.joker.comfypilot.common.tool.skills;

import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Skill 实体类
 * 表示一个 skill 及其元数据、层级关系
 */
@Data
public class Skill {

    /**
     * 技能名称（从 SKILL.md 的 YAML 前置元数据中解析）
     */
    private String name;

    /**
     * 技能描述（从 SKILL.md 的 YAML 前置元数据中解析）
     */
    private String description;

    /**
     * 技能所在目录的绝对路径
     */
    private Path skillPath;

    /**
     * 父技能（如果有）
     */
    private Skill parent;

    /**
     * 子技能列表
     */
    private List<Skill> children = new ArrayList<>();

    /**
     * 可选的元数据字段
     */
    private String license;
    private String compatibility;
    private Map<String, String> metadata = new HashMap<>();
    private String allowedTools;

    /**
     * 添加子技能
     */
    public void addChild(Skill child) {
        this.children.add(child);
        child.setParent(this);
    }

    /**
     * 判断是否为根技能（没有父技能）
     */
    public boolean isRoot() {
        return parent == null;
    }

    /**
     * 获取技能的层级深度（根技能为 0）
     */
    public int getDepth() {
        if (parent == null) {
            return 0;
        }
        return parent.getDepth() + 1;
    }
}
