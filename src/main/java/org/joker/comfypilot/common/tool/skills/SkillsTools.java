package org.joker.comfypilot.common.tool.skills;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import lombok.extern.slf4j.Slf4j;
import org.joker.comfypilot.common.annotation.ToolSet;
import org.joker.comfypilot.common.tool.filesystem.FileInfo;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Skills 工具类
 * 提供 skill 相关的工具方法，包括：
 * 1. 获取 skill 信息（官方 XML 格式）
 * 2. 文件系统操作（仅限 skills 目录）
 */
@Slf4j
@Component
@ToolSet
public class SkillsTools {

    private final SkillsRegistry skillsRegistry;

    public SkillsTools(SkillsRegistry skillsRegistry) {
        this.skillsRegistry = skillsRegistry;
    }

    /**
     * 获取一级 skills 或指定 skill 的直接子 skills（官方 XML 格式）
     *
     * @param skillName 可选的 skill 名称。如果为空，返回所有根技能；如果不为空，返回该技能的直接子技能
     * @return 官方格式的 XML 字符串
     */
    @Tool(name = "getSkillsInfo", value = "获取可用的 Agent Skills 信息（一级或指定技能的子技能）")
    public String getSkillsInfo(
            @P(value = "可选的技能名称，为空时返回所有根技能，不为空时返回该技能的直接子技能", required = false) String skillName) {

        List<Skill> skillsToDisplay;

        if (skillName == null || skillName.trim().isEmpty()) {
            // 获取所有根技能
            skillsToDisplay = skillsRegistry.getRootSkills();
        } else {
            // 获取指定技能的子技能
            Skill parentSkill = skillsRegistry.getSkillByName(skillName.trim());
            if (parentSkill == null) {
                return String.format("<error>技能不存在: %s</error>", skillName);
            }
            skillsToDisplay = parentSkill.getChildren();
        }

        return buildSkillsXml(skillsToDisplay, false);
    }

    /**
     * 递归获取所有 skills 或指定 skill 下的所有层级的 skills（官方 XML 格式）
     *
     * @param skillName 可选的 skill 名称。如果为空，返回所有技能；如果不为空，返回该技能及其所有子技能
     * @return 官方格式的 XML 字符串
     */
    @Tool(name = "getAllSkillsInfo", value = "递归获取所有 Agent Skills 信息（包括所有层级）")
    public String getAllSkillsInfo(
            @P(value = "可选的技能名称，为空时返回所有技能，不为空时返回该技能及其所有子技能", required = false) String skillName) {

        List<Skill> skillsToDisplay;

        if (skillName == null || skillName.trim().isEmpty()) {
            // 获取所有根技能（递归会包含所有子技能）
            skillsToDisplay = skillsRegistry.getRootSkills();
        } else {
            // 获取指定技能
            Skill parentSkill = skillsRegistry.getSkillByName(skillName.trim());
            if (parentSkill == null) {
                return String.format("<error>技能不存在: %s</error>", skillName);
            }
            skillsToDisplay = List.of(parentSkill);
        }

        return buildSkillsXml(skillsToDisplay, true);
    }

    /**
     * 根据 skill 名称获取其所在目录的绝对路径
     *
     * @param skillName skill 名称
     * @return skill 目录的绝对路径
     */
    @Tool(name = "getSkillPath", value = "根据技能名称获取该技能所在目录的绝对路径")
    public String getSkillPath(@P(value = "技能名称", required = true) String skillName) {
        Skill skill = skillsRegistry.getSkillByName(skillName);
        if (skill == null) {
            return String.format("技能不存在: %s", skillName);
        }
        return skill.getSkillPath().toString();
    }

    /**
     * 检查路径是否为目录
     *
     * @param path 路径
     * @return 是否为目录
     */
    @Tool(name = "isSkillDirectory", value = "检查 Skills 目录下的指定路径是否为目录")
    public boolean isSkillDirectory(@P(value = "要检查的路径", required = true) String path) {
        validatePathInSkillsDirectory(path);
        try {
            Path filePath = Paths.get(path);
            return Files.isDirectory(filePath);
        } catch (Exception e) {
            log.error("检查是否为目录失败: {}", path, e);
            return false;
        }
    }

    /**
     * 检查路径是否为文件
     *
     * @param path 路径
     * @return 是否为文件
     */
    @Tool(name = "isSkillFile", value = "检查 Skills 目录下的指定路径是否为文件")
    public boolean isSkillFile(@P(value = "要检查的路径", required = true) String path) {
        validatePathInSkillsDirectory(path);
        try {
            Path filePath = Paths.get(path);
            return Files.isRegularFile(filePath);
        } catch (Exception e) {
            log.error("检查是否为文件失败: {}", path, e);
            return false;
        }
    }

    /**
     * 读取 Skills 目录下的文件内容
     *
     * @param path 文件路径
     * @return 文件内容
     * @throws IOException IO 异常
     */
    @Tool(name = "readSkillFile", value = "读取 Skills 目录下的指定文件内容")
    public String readSkillFile(@P(value = "要读取的文件路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);
        Path filePath = Paths.get(path);
        if (!Files.exists(filePath)) {
            throw new IOException("文件不存在: " + path);
        }
        if (!Files.isRegularFile(filePath)) {
            throw new IOException("不是文件: " + path);
        }
        return Files.readString(filePath, StandardCharsets.UTF_8);
    }

    /**
     * 列出 Skills 目录下的目录内容
     *
     * @param path 目录路径
     * @return 文件和目录列表的 JSON 字符串
     * @throws IOException IO 异常
     */
    @Tool(name = "listSkillDirectory", value = "列出 Skills 目录下的指定目录内容")
    public String listSkillDirectory(@P(value = "要列出内容的目录路径", required = true) String path) throws IOException {
        validatePathInSkillsDirectory(path);
        Path dirPath = Paths.get(path);
        if (!Files.exists(dirPath)) {
            throw new IOException("目录不存在: " + path);
        }
        if (!Files.isDirectory(dirPath)) {
            throw new IOException("不是目录: " + path);
        }

        List<FileInfo> fileInfos = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dirPath)) {
            stream.forEach(p -> {
                try {
                    BasicFileAttributes attrs = Files.readAttributes(p, BasicFileAttributes.class);
                    FileInfo info = new FileInfo(
                            p.getFileName().toString(),
                            p.toString(),
                            attrs.isDirectory(),
                            attrs.size(),
                            attrs.lastModifiedTime().toMillis()
                    );
                    fileInfos.add(info);
                } catch (IOException e) {
                    log.error("读取文件属性失败: {}", p, e);
                }
            });
        }

        // 将列表转换为 JSON 字符串返回
        StringBuilder result = new StringBuilder("[");
        for (int i = 0; i < fileInfos.size(); i++) {
            FileInfo info = fileInfos.get(i);
            result.append(info.toJsonString());
            if (i < fileInfos.size() - 1) {
                result.append(",");
            }
        }
        result.append("]");
        return result.toString();
    }

    /**
     * 校验路径是否在配置的 skills 目录下
     *
     * @param path 要校验的路径
     * @throws SecurityException 如果路径不在 skills 目录下
     */
    private void validatePathInSkillsDirectory(String path) {
        try {
            Path filePath = Paths.get(path).toAbsolutePath().normalize();
            
            // 检查路径是否在任意一个配置的 skills 目录下
            boolean inSkillsDir = false;
            for (Path configuredDir : skillsRegistry.getConfiguredDirectories()) {
                if (filePath.startsWith(configuredDir)) {
                    inSkillsDir = true;
                    break;
                }
            }

            if (!inSkillsDir) {
                throw new SecurityException("安全限制：只能访问配置的 Skills 目录下的文件。配置的目录: " 
                        + skillsRegistry.getConfiguredDirectories());
            }
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new SecurityException("路径校验失败: " + path, e);
        }
    }

    /**
     * 构建 Skills 的 XML 格式字符串
     *
     * @param skills    要显示的 skills 列表
     * @param recursive 是否递归显示子技能
     * @return XML 格式字符串
     */
    private String buildSkillsXml(List<Skill> skills, boolean recursive) {
        if (skills.isEmpty()) {
            return "<available_skills description=\"Skills the agent can use.\">No skills available.</available_skills>";
        }

        StringBuilder xml = new StringBuilder();
        xml.append("<available_skills description=\"Skills the agent can use. Use the Read tool with the provided absolute path to fetch full contents.\">\n");

        for (Skill skill : skills) {
            appendSkillXml(xml, skill, recursive);
        }

        xml.append("</available_skills>");
        return xml.toString();
    }

    /**
     * 递归添加 skill 的 XML
     *
     * @param xml       StringBuilder
     * @param skill     要添加的 skill
     * @param recursive 是否递归显示子技能
     */
    private void appendSkillXml(StringBuilder xml, Skill skill, boolean recursive) {
        String fullPath = skill.getSkillPath().resolve("SKILL.md").toString();
        
        xml.append(String.format("<agent_skill fullPath=\"%s\">%s</agent_skill>\n",
                escapeXml(fullPath),
                escapeXml(skill.getDescription())));

        if (recursive && !skill.getChildren().isEmpty()) {
            for (Skill child : skill.getChildren()) {
                appendSkillXml(xml, child, true);
            }
        }
    }

    /**
     * XML 转义
     *
     * @param text 要转义的文本
     * @return 转义后的文本
     */
    private String escapeXml(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

}
