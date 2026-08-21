---
name: tdlc-spec-assistant
description: Use when implementing a change from a generated change directory — i.e. when <ROOT>/openspec/changes/<change-name>/ contains design.md and tasks.md, and <ROOT>/docs/current/analysis.md exists. proposal.md/reference.md optional. Triggered by /tdlc-spec-assistant. Bridges spec pipeline output to superpowers:executing-plans for implementation, with design.md as the scope authority.
---

# TDLC Spec Assistant

基于已生成的文档契约完成代码开发。文档是只读输入，实现发生在源码树。实施阶段通过 `superpowers:executing-plans` 执行。

**违反规则的字面意思就是违反规则的精神。** 不要走捷径：不重新生成 specs、不要求 `specs/`、不凭空补需求。

## 路径约定

所有路径基于 spec 工作区根目录：

```text
<ROOT> = spec/cr/<cr-id>/<repo>
```

- `<cr-id>`：当前 CR 标识（例：`spec-20260623222127`）
- `<repo>`：目标仓库名（例：`tdlc_main_poc`）
- `<change-name>`：change 目录名，通常与 `<cr-id>` 相同

示例：`<ROOT> = spec/cr/spec-20260623222127/tdlc_main_poc`

下文所有路径均以 `<ROOT>` 为前缀；实施前必须先从 CR 上下文确定 `<cr-id>` 与 `<repo>`，代入后再操作。

## 文档契约

```text
<ROOT>/docs/current/analysis.md              # 代码现状 + 影响范围（事实层，必读）
<ROOT>/openspec/changes/<change-name>/
├── design.md                         # 技术方案 + 范围边界（变更层，scope 权威，必读）
├── tasks.md                          # 可执行任务清单（执行层，必读）
├── proposal.md                       # 目标层（可选参考，不作为 scope/验收依据）
└── reference.md                      # 需求来源文件指针（可选参考）
```

**三核心文档（design + tasks + analysis）缺一不可**；`proposal.md` / `reference.md` 为可选参考，缺失不阻塞实施。

**信任层级（决定冲突时谁说了算）：**

| 文档 | 角色 | 如何使用 |
|---|---|---|
| `analysis.md` | **代码事实** | Stage 5 用 codegraph 查出的文件路径、符号、现状；实施时以此核对代码事实 |
| `design.md` | **变更权威** | Stage 5 产出的技术方案与范围边界；**本次实施 scope 以 design.md 为准（含非目标）** |
| `tasks.md` | **执行计划** | Stage 6 `tdlc-writing-plans` 产出；按序实施，但任务须落在 design.md scope 内 |
| `proposal.md` | **不依赖** | 目标层文档；不作为 scope 或验收依据，仅在 design.md 范围有歧义时回查 |
| `reference.md` | **来源指针** | 仅在需求理解有歧义时回查；通常是 1-2 行路径，低优先级 |

> **关于 tasks.md 来源**：Stage 6 由 `tdlc-writing-plans` skill 生成 —— 它读 CR 需求文件 + `analysis.md`（拿 codegraph 代码事实）+ 源码（read/glob/grep），不依赖 proposal/design，不调 codegraph。若模型连续断流无法生成，流水线 **fail-fast 报错终止**，不会产生兜底文档——此时应排查上游问题而非强行实施。

> **⚠ 关于 Stage 7 review 对 tasks.md 的补全**：Stage 7 `tdlc-spec-review` 以原始 PRD 为核对基准，会对 tasks.md 无覆盖项原地 edit 追加任务（归入「补全」分组）。这会引入两类风险，必须在 Step 4 下游闸门拦截：
> 1. **scope 漂移**：review 按 PRD 补全的任务可能超出 `design.md` 本次范围（如 design 划为非目标的埋点被 review 按 PRD 重新塞回 tasks.md）——Step 4 必须以 design.md 对账，超 scope 任务标记「不执行」。
> 2. **假定路径**：review 补全的任务在 step 预算压力下可能写入未经核验的"修改"类路径——Step 4 必须核验 tasks.md 中 Modify 路径真实存在。

**文档质量核验：** 若任一文档内容明显模板化（如缺少项目特定的文件路径/类名、章节为通用占位说明、与 `analysis.md` 代码事实脱节），说明文档质量不达标。这类文档**信任度降低**，必须在 Step 4 重点核验其与 `analysis.md` 代码事实和实际源码的一致性，不能盲信其中的文件路径和符号。

## 工作流程

```text
1. 验证目录结构
2. 选择 change
3. 读取核心文档 + 目标项目约定
4. 审查一致性与可实施性（标记文档质量风险）
5. 用户确认后在当前分支准备实施
6. 交接准备 → superpowers:executing-plans 执行
7. 汇总并交付
```

## Step 1：验证目录

```bash
test -f <ROOT>/docs/current/analysis.md
test -d <ROOT>/openspec/changes
```

任一缺失即停止，明确报告缺失路径。不使用 `/openspec-propose`，不生成 `specs/`。

## Step 2：选择 Change

```bash
find <ROOT>/openspec/changes -mindepth 1 -maxdepth 1 -type d \
  ! -name 'archive' ! -name '.*' -print
```

- 0 个：停止，提示无可实施 change。
- 1 个：自动选中并宣布名称。
- 多个：让用户选择，**不得默认选第一个**。

**已归档检查：** 同时扫描 `<ROOT>/docs/current/changes/archive/` 目录。若发现同名 change 已归档，告知用户该 change 已由他人执行并归档，询问是否仍要继续（防止多成员重复执行）。

```bash
ls <ROOT>/docs/current/changes/archive/ 2>/dev/null
```

不依赖 OpenSpec CLI；change 目录及四个 Markdown 文件就是事实来源。

## Step 3：读取输入文档与项目约定

按信任层级读取。**三核心文档必读、缺一不可**；proposal/reference 仅在歧义时回查：

1. `<ROOT>/docs/current/analysis.md`（代码事实，必读）
2. `<ROOT>/openspec/changes/<change-name>/design.md`（scope 权威，必读）
3. `<ROOT>/openspec/changes/<change-name>/tasks.md`（执行计划，必读）
4. `<ROOT>/openspec/changes/<change-name>/proposal.md`（可选，仅 design 范围有歧义时回查）
5. `<ROOT>/openspec/changes/<change-name>/reference.md`（可选，仅需求理解有歧义时回查）

三核心文档任一缺失或为空（< 32 字符视为空）即停止，列出问题。**不得凭空补全缺失需求。**

**必需：发现并读取目标项目的开发约定。** 按优先级查找（找到就读，找不到不报错）：

1. `AGENTS.md` — 架构、反模式、回调签名陷阱、实施红线
2. `CLAUDE.md` — 项目指引和编码规范
3. `docs/` 下的 code-style / conventions / architecture 类文档

这些文档记录了项目的**已知陷阱和约束**，实施前必须掌握。若找不到任何约定文档，在 Step 4 审查时标注"无项目约定可参考，需人工确认反模式"。

## Step 4：审查设计与计划

逐项核对（直接审查文档，不重新发明需求）：

- **scope 对账（必查）**：以 `design.md` 为 scope 权威。逐任务核对 tasks.md 是否落在 design.md 范围内；**tasks.md 中超出 design.md 范围的任务一律标记「不执行·超出 scope」**，不得进入实施。典型场景：Stage 7 review 按 PRD 补全的「补全」分组任务（如本次 design 非目标之外的埋点/数据事件）——这类任务即便在 tasks.md 里，也不得执行。design.md「非目标」明确排除的项，或 tasks.md 中无 design.md 对应方案的任务，标注「建议另起 change」。
  > **背景**：tasks.md（Stage 6 `tdlc-writing-plans` 产出）不依赖 design.md 生成，两者是独立输出，天然可能漂移；design.md 在本 skill 中作 scope 权威用于对账过滤这种漂移。
- **一致性**：`design.md` 是否基于 `analysis.md` 的代码事实？tasks.md 是否与 design.md 的技术方案一致？
- **可实施性**：tasks.md 是否覆盖 `design.md` 全部验收点？每个任务是否含精确文件、符号、步骤、测试命令、预期结果？
- **路径真实性（必查）**：tasks.md 中标注为"修改/Modify"的文件路径必须在 `analysis.md` 代码事实中出现或经源码核验真实存在；不存在的"修改"路径视为假定路径，标注需核验或剔除。标注为"新增/Create"的路径允许不存在（实施时新建）。若 tasks.md 混淆新增与修改、或路径未经核验，Step 4 必须逐一对照源码确认后再交接。
- **格式合规**：tasks.md 任务是否符合 `### Task N` + `- [ ]` 步骤结构（executing-plans 交接要求）；review 补全的「补全」分组若格式不符，Step 6a 交接前补全。
- **依赖顺序**：任务按依赖排列，数据迁移在前、模型/服务在后、控制器/路由最后。
- **项目陷阱**：tasks.md 引用的文件路径、类名、方法签名是否与项目约定文档 / 实际源码一致？是否触碰已知反模式？
- **文档质量**：若文档内容模板化、缺少项目特定文件路径/类名，其文件路径和符号必须与 `analysis.md` 实际代码逐一核对，不能盲信。
- **红线提取**：从项目约定文档中提取实施红线清单（如禁止修改的方法签名、不可引入的依赖、已知 bug 的规避方式），作为 Step 6 的硬约束。

发现问题：列出**具体文件 + 修改建议**，等待用户决定，不直接编码。文档一致且可实施：简要汇报审查结论并请求确认。

## Step 5：在当前分支准备实施

用户明确确认后，**不创建 worktree，直接在当前分支操作**：

1. 检查工作目录状态（`git status`），确认无未提交的无关改动，避免与实施改动混淆
2. 确认当前分支可写且符合团队协作约定
3. 完成项目初始化（安装依赖、编译等）
4. 运行基线验证（启动应用并确认健康状态）

基线失败先报告，不归因于本次实现。

> **不使用 worktree 的原因：** spec 工作区 (`<ROOT>`) 与目标仓库共享同一工作树，worktree 隔离会导致路径割裂、依赖重复安装、基线验证环境不一致。在当前分支直接实施，改动可见、可追溯，由后续的 commit/push 流程统一收口。

## Step 6：交接准备 + 执行

本步骤分为两个阶段：先准备交接物料，再交给 `superpowers:executing-plans` 执行。

### 6a. 交接准备（tdlc-spec-assistant 负责）

在调用 executing-plans 前，确认 tasks.md 可直接作为 plan file：

1. **格式检查**：确认 tasks.md 中每个任务有清晰的 `### Task N` 标题、`- [ ]` 复选框步骤、精确文件路径和验证命令。若格式不符，在交接前补全。**Stage 7 review 补全的「补全」分组若用了非 `### Task N` 格式，在此统一归一化。**

2. **scope 过滤**：将 Step 4 标记为「不执行·超出 scope」的任务（含 review 补入但 design 未覆盖的项）从交接范围剔除，明确告知 executing-plans 跳过这些任务编号。

3. **红线注入**：将 Step 4 提取的项目红线清单附在交接上下文中，确保 executing-plans 执行时受红线约束。

4. **路径核验标注**：对 tasks.md 中未经 Step 4 核验的"修改"类路径，标注需 executing-plans 在首次触碰时先对照源码确认存在；标注为"新增"的路径不在此列。

5. **验证策略确认**：检查 tasks.md 中的测试命令是否可执行。若项目无测试框架，验证策略为按 tasks.md 的"测试命令及预期结果"逐项手动验证（如 curl、CLI、日志检查）。**不强行引入测试框架。**

### 6b. 执行阶段（交给 superpowers:executing-plans）

显式声明交接：

> "tasks.md 是本次实施的 plan file。按 `superpowers:executing-plans` 协议执行：逐任务推进、复选框追踪进度、按验证命令确认结果。"

**执行约束（传递给 executing-plans）：**

- **计划文件**：`<ROOT>/openspec/changes/<change-name>/tasks.md`
- **任务追踪**：按 tasks.md 的 `### Task N` 结构创建任务列表，逐步推进
- **逐步执行**：每个 `- [ ]` 步骤完成后勾选，不跳步
- **红线硬约束**：Step 4 提取的项目红线不可违反，遇到与红线冲突立即 STOP
- **设计冲突**：实施中发现 design.md / tasks.md 与源码事实不符 → STOP，返回 Step 4 重新审查
- **范围控制**：不擅自扩大 `design.md` 定义的范围；Step 4 标记为「超出 scope」的任务（含 Stage 7 review 补入但 design 未覆盖的项）跳过不执行，不生成 `specs/`、`spec.md` 或新计划文件
- **验证**：按 tasks.md 中每个任务的"测试命令及预期结果"逐项验证。命令不适用时说明原因并选等价验证方式
- **完成后**：回到 Step 7 汇总交付

**何时 STOP 并返回：**

| 情况 | 处理 |
|------|------|
| 触碰项目红线 | STOP → 报告冲突，等待用户决定 |
| tasks.md 步骤与源码矛盾 | STOP → 返回 Step 4 审查 |
| 依赖缺失或环境不可用 | STOP → 报告阻塞，不猜测解决方案 |
| 验证连续失败（≥ 3 次） | STOP → 汇总失败原因，等待用户介入 |

## Step 7：汇总交付与归档

全部任务完成后：

### 7a. 汇总

- 已完成任务（含复选框状态）
- 修改文件清单
- 验证命令与实际结果（对照 tasks.md 预期）
- 触碰的项目红线及如何规避
- 未解决风险与文档质量核验结论
- 建议的后续动作（如需补测试、补迁移等）

### 7b. 归档

将已执行的 change 目录从 `<ROOT>/openspec/changes/` 移动到 `<ROOT>/docs/current/changes/archive/`，标记为已完成，防止多成员重复执行。

```bash
# 将已完成的 change 归档到 archive 子目录
mkdir -p <ROOT>/docs/current/changes/archive
mv <ROOT>/openspec/changes/<change-name> <ROOT>/docs/current/changes/archive/<change-name>
```

**归档后效果：**
- `<ROOT>/openspec/changes/<change-name>/` 不再存在 → Step 2 不会再选中它
- `<ROOT>/docs/current/changes/archive/<change-name>/` 保留完整 change 文档（proposal/design/reference/tasks）→ 可追溯执行历史
- 其他成员运行 Step 2 时，归档检查会发现已完成的 change 并提示

**异常处理：**
- 若 `<ROOT>/docs/current/changes/archive/<change-name>` 已存在（同名归档冲突），追加时间戳后缀：`<change-name>-YYYYMMDDHHMMSS`
- 若用户明确拒绝归档，跳过此步骤并记录"未归档"到汇总中

## Guardrails

- 三核心文档（`design.md` / `tasks.md` / `analysis.md`）缺一不可（< 32 字符视为空）；`proposal.md` / `reference.md` 可选，缺失不阻塞
- 用户确认不可跳过（Step 4 审查结论须用户拍板后再进入 Step 5 实施）
- `design.md` 是 scope 权威；`analysis.md` 是代码事实来源；`tasks.md` 须在 design scope 内，超出 scope 的任务（含 Stage 7 review 补入项）不执行
- `tasks.md` 是唯一实施计划，通过 `superpowers:executing-plans` 执行
- 内容模板化、缺少项目特定路径/符号的文档必须核验，不盲信
- 不依赖 OpenSpec CLI，不要求/生成 `specs/`
- 不触碰项目红线（从 AGENTS.md / CLAUDE.md 提取，Step 4 审查确认）
- 不强行引入测试框架 / linter 除非任务明确要求
- 所有路径以 `<ROOT> = spec/cr/<cr-id>/<repo>` 为前缀，实施前先代入 `<cr-id>` 与 `<repo>`
- 执行完成后必须归档 change 到 `<ROOT>/docs/current/changes/archive/`，防止多成员重复执行
- 归档前若发现同名已归档 change，追加时间戳避免覆盖
