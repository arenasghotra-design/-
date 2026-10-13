package com.example.data.model

enum class SpaceCategory(val label: String, val englishLabel: String) {
    ALL("全部前沿", "ALL"),
    DEEP_SPACE("深空探测", "Deep Space"),
    MANNED_SPACE("载人航天", "Manned Space"),
    HEAVY_ROCKET("重型运载", "Heavy Rocket"),
    SPACE_SCIENCE("空间科学", "Space Science"),
    COMMERCIAL_SPACE("商业航天", "Commercial")
}

enum class LaunchStatus(val label: String, val colorHex: Long) {
    COUNTDOWN("点火倒计时", 0xFFFF6D00),
    FUEL_LOADING("推进剂加注中", 0xFFFFAB00),
    READY("发射程序就绪", 0xFF00E5FF),
    SUCCESS("已成功入轨", 0xFF10B981)
}

data class SpaceArticle(
    val id: String,
    val title: String,
    val summary: String,
    val details: String,
    val source: String,
    val category: SpaceCategory,
    val publishedAt: Long,
    val formattedDate: String,
    val tags: List<String>,
    val imageUrl: String,
    val impactBadge: String,
    val technicalSpecs: Map<String, String> = emptyMap()
)

data class RocketLaunch(
    val id: String,
    val missionName: String,
    val rocketName: String,
    val launchSite: String,
    val scheduledEpochMs: Long,
    val status: LaunchStatus,
    val orbitType: String,
    val payloadInfo: String,
    val technicalHighlights: String
)

data class NotificationAlert(
    val id: Long = 0,
    val matchedKeyword: String,
    val articleId: String,
    val articleTitle: String,
    val timestamp: Long,
    val isRead: Boolean = false
)
