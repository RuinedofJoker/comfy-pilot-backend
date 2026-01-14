# CLAUDE.md

Always respond in Chinese-simplified

## Cursor Rules Specifications Summary
The project maintains comprehensive coding standards and patterns in `cursor/` directory:

@cursor/base/core.mdc
@cursor/base/general.mdc
@cursor/base/project-structure.mdc
@cursor/base/tech-stack.mdc

@cursor/languages/java.mdc

@cursor/frameworks/springboot.mdc

@cursor/other/document.mdc
@cursor/other/git.mdc
@cursor/other/gitflow.mdc


All code must strictly adhere to these patterns and standards for consistency and maintainability.

你所写的所有文档内容(docs)全部放到`docs/claudecode`下，其他的目录对你写文档(docs)都是只读的，用户会将你的文档迁移到对应的目录，你可以在输出文档后推荐用户迁移到哪个目录下
在项目的需求设计阶段尽量将设计以画图的形式展现，使用PlantUML的语法在`docs/claudecode/uml`下新建图的源代码文件，需求说明以md的语法在`docs/claudecode/md`下新建文件