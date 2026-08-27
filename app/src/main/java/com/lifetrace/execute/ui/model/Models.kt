package com.lifetrace.execute.ui.model

enum class TaskStatus { TODO, IN_PROGRESS, WAITING, DONE }
enum class Priority { LOW, NORMAL, HIGH, URGENT }
enum class ProjectStatus { ACTIVE, PAUSED, DONE }

data class TaskUi(
    val title: String,
    val time: String,
    val project: String? = null,
    val status: TaskStatus = TaskStatus.TODO,
    val priority: Priority = Priority.NORMAL
)

data class TimelineUi(
    val time: String,
    val title: String,
    val tag: String,
    val completed: Boolean = false
)

data class ProjectUi(
    val title: String,
    val progress: Float,
    val deadline: String,
    val memberCount: Int,
    val status: ProjectStatus
)

data class CaptureBucketUi(
    val title: String,
    val count: Int
)

object MockData {
    val timeline = listOf(
        TimelineUi("08:00", "晨间例行", "习惯", completed = true),
        TimelineUi("09:30", "产品会", "会议"),
        TimelineUi("11:00", "需求评审", "工作"),
        TimelineUi("14:00", "回顾邮件", "工作"),
        TimelineUi("16:00", "运动 30 分钟", "习惯")
    )

    val todayTasks = listOf(
        TaskUi("撰写 PRD 文档", "11:00", "LifeTrace 2.0", priority = Priority.HIGH),
        TaskUi("设计登录页", "14:00", "LifeTrace 2.0"),
        TaskUi("回复合作方邮件", "16:00", priority = Priority.HIGH),
        TaskUi("整理竞品分析", "明天 10:00"),
        TaskUi("预约体检", "5/29 09:00"),
        TaskUi("学习 Kotlin 协程", "6/1 20:00")
    )

    val projects = listOf(
        ProjectUi("LifeTrace 2.0 迭代", 0.60f, "6月30日", 5, ProjectStatus.ACTIVE),
        ProjectUi("个人品牌建设", 0.35f, "7月15日", 2, ProjectStatus.ACTIVE),
        ProjectUi("阅读计划 2026", 0.80f, "9月30日", 1, ProjectStatus.ACTIVE),
        ProjectUi("家庭旅行计划", 0.20f, "10月1日", 3, ProjectStatus.PAUSED)
    )

    val captureBuckets = listOf(
        CaptureBucketUi("待分类收集", 12),
        CaptureBucketUi("灵感想法", 8),
        CaptureBucketUi("阅读摘录", 15),
        CaptureBucketUi("待办记录", 6),
        CaptureBucketUi("参考链接", 9)
    )
}
