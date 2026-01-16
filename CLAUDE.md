# CLAUDE.md

Always respond in Chinese-simplified

## Cursor Rules Specifications Summary

The project maintains comprehensive coding standards and patterns in `.cursor/rules/` directory.In every new conversation, you should read all the rules and then follow them.


All code must strictly adhere to these patterns and standards for consistency and maintainability.

每个新的会话需要操纵或阅读当前`后端代码仓库`前先阅读当前代码仓库的`.cursor/rules/`下的所有规则文件和`docs/requirements`需求目录下的`README.md`、00-项目概述.md`、`99-技术规范.md`
并根据上面的规则和需求索引文件来结合用户的问题查询`docs/requirements`需求目录下对应模块的模块设计、api设计、数据库设计文件

当前阶段为模块实现阶段：
据UI设计图里的页面和需求文档设计指定模块的文档和代码
当前阶段每一步实现都会在当前后端代码仓库根目录下的steps目录下新建一个step[x].md(x为当前该目录下最大的值+1，如之前最大的是step1.md，当前步记录文件为step2.md)
用户会告诉你当前是在第多少步(如当前我们在step1)，当前步结束时用户会告诉你我需要新建一步，这时你需要将当前步的内容记录到当前步文件里，然后新建一个新的步文件开始新的步
新的步创建时需要继承上一步没有做完的事，如果需要新步的大纲也需要在根据老步创建新步时指定到新步文件里
每一步都只需要看上一步做了什么，不需要关注更之前的步