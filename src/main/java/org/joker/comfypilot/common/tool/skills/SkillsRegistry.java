package org.joker.comfypilot.common.tool.skills;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Skills 注册器
 * 在应用启动时扫描配置的 skills 目录，解析并注册所有 skills
 */
@Slf4j
@Component
public class SkillsRegistry implements CommandLineRunner {

    private final SkillsConfig skillsConfig;

    /**
     * 所有已注册的 skills（key: skill名称, value: Skill对象）
     */
    private final Map<String, Skill> skillMap = new ConcurrentHashMap<>();

    /**
     * 根技能列表（没有父技能的 skills）
     */
    private final List<Skill> rootSkills = new ArrayList<>();

    /**
     * 配置的 skills 目录列表（用于路径校验）
     */
    private final Set<Path> configuredDirectories = new HashSet<>();

    public SkillsRegistry(SkillsConfig skillsConfig) {
        this.skillsConfig = skillsConfig;
    }

    @Override
    public void run(String... args) throws Exception {
        log.info("开始扫描和注册 Skills...");

        if (skillsConfig.getDirectories() == null || skillsConfig.getDirectories().isEmpty()) {
            log.warn("未配置 skills 目录，跳过 Skills 注册");
            return;
        }

        int skillCount = 0;
        for (String directory : skillsConfig.getDirectories()) {
            try {
                Path dirPath = Paths.get(directory).toAbsolutePath().normalize();
                if (!Files.exists(dirPath)) {
                    log.warn("Skills 目录不存在，跳过: {}", dirPath);
                    continue;
                }
                if (!Files.isDirectory(dirPath)) {
                    log.warn("不是目录，跳过: {}", dirPath);
                    continue;
                }

                configuredDirectories.add(dirPath);
                log.info("扫描 Skills 目录: {}", dirPath);

                int count = scanAndRegisterSkills(dirPath, null);
                skillCount += count;

            } catch (Exception e) {
                log.error("扫描 Skills 目录失败: {}", directory, e);
            }
        }

        log.info("Skills 注册完成，共注册 {} 个技能，根技能数: {}", skillCount, rootSkills.size());
        log.info("已注册的技能列表: {}", skillMap.keySet());
    }

    /**
     * 递归扫描并注册 skills
     *
     * @param directory     要扫描的目录
     * @param parentSkill   父技能（如果有）
     * @return 注册的技能数量
     */
    private int scanAndRegisterSkills(Path directory, Skill parentSkill) throws IOException {
        int count = 0;

        // 检查当前目录是否有 SKILL.md
        Path skillMdPath = directory.resolve("SKILL.md");
        Skill currentSkill = parentSkill;

        if (Files.exists(skillMdPath) && Files.isRegularFile(skillMdPath)) {
            try {
                Skill skill = parseSkillMd(skillMdPath, directory);
                if (skill != null) {
                    // 注册技能
                    if (skillMap.containsKey(skill.getName())) {
                        log.warn("技能名称重复: {}, 路径: {}", skill.getName(), directory);
                    } else {
                        skillMap.put(skill.getName(), skill);
                        
                        // 建立父子关系
                        if (parentSkill != null) {
                            parentSkill.addChild(skill);
                        } else {
                            rootSkills.add(skill);
                        }

                        currentSkill = skill;
                        count++;
                        log.debug("注册技能: name={}, path={}, parent={}", 
                                skill.getName(), directory, parentSkill != null ? parentSkill.getName() : "无");
                    }
                }
            } catch (Exception e) {
                log.error("解析 SKILL.md 失败: {}", skillMdPath, e);
            }
        }

        // 递归扫描子目录
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory)) {
            for (Path entry : stream) {
                if (Files.isDirectory(entry)) {
                    count += scanAndRegisterSkills(entry, currentSkill);
                }
            }
        }

        return count;
    }

    /**
     * 解析 SKILL.md 文件
     *
     * @param skillMdPath SKILL.md 文件路径
     * @param skillDir    skill 所在目录
     * @return Skill 对象
     */
    private Skill parseSkillMd(Path skillMdPath, Path skillDir) throws IOException {
        String content = Files.readString(skillMdPath);

        // 解析 YAML 前置元数据
        Map<String, Object> metadata = parseYamlFrontMatter(content);
        if (metadata == null || !metadata.containsKey("name") || !metadata.containsKey("description")) {
            log.warn("SKILL.md 缺少必需的 name 或 description 字段: {}", skillMdPath);
            return null;
        }

        Skill skill = new Skill();
        skill.setName((String) metadata.get("name"));
        skill.setDescription((String) metadata.get("description"));
        skill.setSkillPath(skillDir);

        // 解析可选字段
        if (metadata.containsKey("license")) {
            skill.setLicense((String) metadata.get("license"));
        }
        if (metadata.containsKey("compatibility")) {
            skill.setCompatibility((String) metadata.get("compatibility"));
        }
        if (metadata.containsKey("allowed-tools")) {
            skill.setAllowedTools((String) metadata.get("allowed-tools"));
        }
        if (metadata.containsKey("metadata") && metadata.get("metadata") instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, String> metaMap = (Map<String, String>) metadata.get("metadata");
            skill.setMetadata(metaMap);
        }

        return skill;
    }

    /**
     * 解析 YAML 前置元数据
     * YAML 前置元数据格式：
     * ---
     * name: skill-name
     * description: description text
     * ---
     *
     * @param content SKILL.md 文件内容
     * @return YAML 元数据的 Map，如果没有则返回 null
     */
    private Map<String, Object> parseYamlFrontMatter(String content) {
        // 匹配 YAML 前置元数据（以 --- 开头和结尾）
        Pattern pattern = Pattern.compile("^---\\s*\n(.*?)\n---\\s*\n", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (!matcher.find()) {
            return null;
        }

        String yamlContent = matcher.group(1);
        Yaml yaml = new Yaml();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> result = yaml.load(yamlContent);
            return result;
        } catch (Exception e) {
            log.error("解析 YAML 失败: {}", yamlContent, e);
            return null;
        }
    }

    /**
     * 根据名称获取 skill
     *
     * @param name skill 名称
     * @return Skill 对象，如果不存在则返回 null
     */
    public Skill getSkillByName(String name) {
        return skillMap.get(name);
    }

    /**
     * 获取所有根技能
     *
     * @return 根技能列表
     */
    public List<Skill> getRootSkills() {
        return new ArrayList<>(rootSkills);
    }

    /**
     * 获取所有已注册的技能
     *
     * @return 所有技能的 Map
     */
    public Map<String, Skill> getAllSkills() {
        return new HashMap<>(skillMap);
    }

    /**
     * 获取配置的 skills 目录列表
     *
     * @return 配置的目录集合
     */
    public Set<Path> getConfiguredDirectories() {
        return new HashSet<>(configuredDirectories);
    }

    public int getSkillCount() {
        return skillMap.size();
    }
}
