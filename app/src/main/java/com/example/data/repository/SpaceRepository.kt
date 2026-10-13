package com.example.data.repository

import com.example.data.local.dao.SubscriptionDao
import com.example.data.local.entity.AlertHistoryEntity
import com.example.data.local.entity.BookmarkEntity
import com.example.data.local.entity.SubscriptionKeywordEntity
import com.example.data.model.LaunchStatus
import com.example.data.model.RocketLaunch
import com.example.data.model.SpaceArticle
import com.example.data.model.SpaceCategory
import com.example.notification.CosmoNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SpaceRepository(
    private val subscriptionDao: SubscriptionDao,
    private val notificationManager: CosmoNotificationManager
) {
    private val _articles = MutableStateFlow<List<SpaceArticle>>(emptyList())
    val articles: Flow<List<SpaceArticle>> = _articles.asStateFlow()

    private val _launches = MutableStateFlow<List<RocketLaunch>>(emptyList())
    val launches: Flow<List<RocketLaunch>> = _launches.asStateFlow()

    val subscribedKeywords: Flow<List<SubscriptionKeywordEntity>> = subscriptionDao.getAllKeywords()
    val alertHistory: Flow<List<AlertHistoryEntity>> = subscriptionDao.getAllAlerts()
    val bookmarks: Flow<List<BookmarkEntity>> = subscriptionDao.getAllBookmarks()

    init {
        _articles.value = getInitialArticles()
        _launches.value = getInitialLaunches()
    }

    suspend fun initializeDefaultKeywordsIfEmpty() = withContext(Dispatchers.IO) {
        val active = subscriptionDao.getActiveKeywords()
        if (active.isEmpty()) {
            val defaults = listOf("星舰", "嫦娥", "长征", "空间站", "可重复使用")
            defaults.forEach { kw ->
                subscriptionDao.insertKeyword(SubscriptionKeywordEntity(keyword = kw, matchCount = 0, isEnabled = true))
            }
        }
        recalculateKeywordMatches()
    }

    suspend fun refreshArticles(simulateNewBreakthrough: Boolean = true): List<String> = withContext(Dispatchers.IO) {
        val currentList = _articles.value.toMutableList()
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        val triggeredAlerts = mutableListOf<String>()

        if (simulateNewBreakthrough) {
            val newDynamicArticles = listOf(
                SpaceArticle(
                    id = "breakthrough_${now}",
                    title = "【突发前沿】中国载人航天工程办公室：新一代可重复使用空间货运飞船完成全系统综合热控试车",
                    summary = "飞船采用全低成本商业化复合材料结构与高复用充气式减速再入技术，大幅压缩近地轨道货物上行成本并支持百公斤级冷链下行。",
                    details = "该型飞船由中国航天科技集团抓总研制，突破了耐高温高熵合金防热瓦与模块化自适应轨道控制算法。整船具备高达6.5吨的货物运载能力，可与中国空间站梦天实验舱货运气闸舱实现全自动超精度微应力交会对接，标志着我国深空低成本轨道物流体系迈向规模化部署新阶段。",
                    source = "中国载人航天官方快讯",
                    category = SpaceCategory.MANNED_SPACE,
                    publishedAt = now,
                    formattedDate = dateFormat.format(Date(now)),
                    tags = listOf("空间站", "可重复使用", "货运飞船", "中国航天"),
                    imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop",
                    impactBadge = "重大前沿突破",
                    technicalSpecs = mapOf(
                        "有效载荷" to "6.5 吨",
                        "重复使用寿命" to "≥ 10 次",
                        "交会对接精度" to "< 2 厘米",
                        "防热瓦耐温" to "1650 ℃"
                    )
                ),
                SpaceArticle(
                    id = "starship_update_${now}",
                    title = "【发射快讯】SpaceX 星舰 (Starship) S31 与 B13 完成全系统湿态彩排，超重型推进器点火准备就绪",
                    summary = "Starbase 基地发射台已完成满载 4500 吨超低温液氧甲烷加注演练，下一阶段将实施 33 台猛禽发动机全时长静态点火。",
                    details = "本次综合湿态测试检验了超重型助推器的热分离环与新型隔热罩冷却喷淋系统的可靠性。马斯克确认星舰下一次飞行将测试太空推进剂推进剂在轨转移关键技术，为 NASA 阿尔忒弥斯计划的人类登月着陆器 (HLS) 扫清关键工程障碍。",
                    source = "SpaceX Mission Control",
                    category = SpaceCategory.COMMERCIAL_SPACE,
                    publishedAt = now - 1800000,
                    formattedDate = dateFormat.format(Date(now - 1800000)),
                    tags = listOf("星舰", "SpaceX", "可重复使用", "猛禽发动机"),
                    imageUrl = "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?q=80&w=800&auto=format&fit=crop",
                    impactBadge = "试飞倒计时",
                    technicalSpecs = mapOf(
                        "推进剂总量" to "4500 吨 (CH4+LOX)",
                        "起飞总推力" to "75900 kN",
                        "第一级助推器" to "33台 Raptor 3",
                        "在轨转移目标" to "10 吨低温燃料"
                    )
                )
            )

            // Insert new articles at top if not already exists
            newDynamicArticles.forEach { newArt ->
                if (currentList.none { it.title == newArt.title }) {
                    currentList.add(0, newArt)
                }
            }
            _articles.value = currentList
        }

        // Recalculate and trigger keyword pushes for matched items
        val activeKeywords = subscriptionDao.getActiveKeywords()
        for (kw in activeKeywords) {
            val matchedArticles = currentList.filter { articleMatches(it, kw.keyword) }
            subscriptionDao.updateMatchCount(kw.keyword, matchedArticles.size)

            // If a newly introduced article matches, trigger push
            matchedArticles.take(1).forEach { matchedArt ->
                if (matchedArt.publishedAt >= now - 60000) {
                    val alertId = subscriptionDao.insertAlert(
                        AlertHistoryEntity(
                            matchedKeyword = kw.keyword,
                            articleId = matchedArt.id,
                            articleTitle = matchedArt.title,
                            timestamp = now,
                            isRead = false
                        )
                    )
                    val pushed = notificationManager.sendSpaceAlertNotification(
                        notificationId = (alertId % 100000).toInt(),
                        keyword = kw.keyword,
                        articleTitle = matchedArt.title,
                        articleSnippet = matchedArt.summary,
                        articleId = matchedArt.id
                    )
                    triggeredAlerts.add(kw.keyword)
                }
            }
        }

        triggeredAlerts
    }

    suspend fun addKeyword(keyword: String) = withContext(Dispatchers.IO) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return@withContext
        val currentArticles = _articles.value
        val matches = currentArticles.count { articleMatches(it, trimmed) }
        subscriptionDao.insertKeyword(
            SubscriptionKeywordEntity(
                keyword = trimmed,
                createdAt = System.currentTimeMillis(),
                matchCount = matches,
                isEnabled = true
            )
        )
    }

    suspend fun removeKeyword(keyword: String) = withContext(Dispatchers.IO) {
        subscriptionDao.deleteKeyword(keyword)
    }

    suspend fun toggleKeyword(keyword: String, isEnabled: Boolean) = withContext(Dispatchers.IO) {
        subscriptionDao.setKeywordEnabled(keyword, isEnabled)
    }

    suspend fun testSimulatePush(keyword: String): Boolean = withContext(Dispatchers.IO) {
        val candidate = _articles.value.firstOrNull { articleMatches(it, keyword) }
            ?: _articles.value.firstOrNull()
            ?: return@withContext false

        val alertId = subscriptionDao.insertAlert(
            AlertHistoryEntity(
                matchedKeyword = keyword,
                articleId = candidate.id,
                articleTitle = "【雷达推送测试】" + candidate.title,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )

        notificationManager.sendSpaceAlertNotification(
            notificationId = (alertId % 100000).toInt(),
            keyword = keyword,
            articleTitle = candidate.title,
            articleSnippet = candidate.summary,
            articleId = candidate.id
        )
    }

    suspend fun scanAndTriggerAllMatches(): Int = withContext(Dispatchers.IO) {
        val activeKeywords = subscriptionDao.getActiveKeywords()
        val allArts = _articles.value
        var count = 0
        val now = System.currentTimeMillis()

        for (kw in activeKeywords) {
            val matches = allArts.filter { articleMatches(it, kw.keyword) }
            subscriptionDao.updateMatchCount(kw.keyword, matches.size)
            if (matches.isNotEmpty()) {
                val topMatch = matches.first()
                val alertId = subscriptionDao.insertAlert(
                    AlertHistoryEntity(
                        matchedKeyword = kw.keyword,
                        articleId = topMatch.id,
                        articleTitle = topMatch.title,
                        timestamp = now,
                        isRead = false
                    )
                )
                notificationManager.sendSpaceAlertNotification(
                    notificationId = (alertId % 100000).toInt(),
                    keyword = kw.keyword,
                    articleTitle = topMatch.title,
                    articleSnippet = topMatch.summary,
                    articleId = topMatch.id
                )
                count++
            }
        }
        count
    }

    suspend fun recalculateKeywordMatches() = withContext(Dispatchers.IO) {
        val activeKeywords = subscriptionDao.getActiveKeywords()
        val allArts = _articles.value
        for (kw in activeKeywords) {
            val count = allArts.count { articleMatches(it, kw.keyword) }
            subscriptionDao.updateMatchCount(kw.keyword, count)
        }
    }

    suspend fun markAlertRead(alertId: Long) = withContext(Dispatchers.IO) {
        subscriptionDao.markAlertAsRead(alertId)
    }

    suspend fun clearAllAlerts() = withContext(Dispatchers.IO) {
        subscriptionDao.clearAlertHistory()
    }

    suspend fun toggleBookmark(article: SpaceArticle, isSaved: Boolean) = withContext(Dispatchers.IO) {
        if (isSaved) {
            subscriptionDao.deleteBookmark(article.id)
        } else {
            subscriptionDao.insertBookmark(
                BookmarkEntity(
                    articleId = article.id,
                    title = article.title,
                    category = article.category.name,
                    source = article.source,
                    savedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun articleMatches(article: SpaceArticle, keyword: String): Boolean {
        val kw = keyword.lowercase()
        return article.title.lowercase().contains(kw) ||
                article.summary.lowercase().contains(kw) ||
                article.details.lowercase().contains(kw) ||
                article.tags.any { it.lowercase().contains(kw) }
    }

    private fun getInitialArticles(): List<SpaceArticle> {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA)
        return listOf(
            SpaceArticle(
                id = "art_01",
                title = "嫦娥七号月球极区采样与探测载荷公布：携带极区飞跃器勘查月球永久阴影坑水冰",
                summary = "国家航天局探月与航天工程中心披露嫦娥七号任务最新科学目标，将搭载全景相机、月壤水分子分析仪及六足移动飞跃探测器。",
                details = "嫦娥七号计划于2026年前后发射，核心目标锁定月球南极艾特肯盆地。飞跃探测器将在着陆器着陆后分离，利用微型推进器多次跃出撞击坑边缘，深入深达数十米、温度低至零下230摄氏度的永久阴影区，原位分析月尘中的结合态水冰丰度，为后续国际月球科研站 (ILRS) 选址提供第一手高精度科学数据。",
                source = "中国国家航天局 (CNSA)",
                category = SpaceCategory.DEEP_SPACE,
                publishedAt = now - 3600000 * 2,
                formattedDate = dateFormat.format(Date(now - 3600000 * 2)),
                tags = listOf("嫦娥", "月球南极", "水冰探测", "深空探测", "探月工程"),
                imageUrl = "https://images.unsplash.com/photo-1614728894747-a83421e2b9c9?q=80&w=800&auto=format&fit=crop",
                impactBadge = "重大科研专项",
                technicalSpecs = mapOf(
                    "任务目标" to "月球南极永久阴影区原位水冰勘探",
                    "飞跃器质量" to "约 55 公斤",
                    "深空通讯" to "鹊桥二号中继星激光链接",
                    "工作寿命" to "≥ 8 年"
                )
            ),
            SpaceArticle(
                id = "art_02",
                title = "长征十号载人运载火箭芯一级动力系统热试车成功，百吨级液氧煤油发动机群并联验证",
                summary = "新一代载人登月火箭完成我国航天史上最大推力动力系统试车，3台大推力高压补燃液氧煤油发动机协同点火，推力达390吨。",
                details = "长征十号是实施我国载人月球探测工程的骨干重型运载工具，整箭起飞重量约2189吨，近地轨道运力不低于70吨，地月转移轨道运力不低于27吨。本次试车考核了复杂力热环境下的多机并联气液供给均衡性、矢量伺服控制动态响应及点火关机瞬态冲击，为2030年前实现中国人首次登陆月球奠定了坚实的火箭动力基石。",
                source = "中国航天科技集团 (CASC)",
                category = SpaceCategory.HEAVY_ROCKET,
                publishedAt = now - 3600000 * 5,
                formattedDate = dateFormat.format(Date(now - 3600000 * 5)),
                tags = listOf("长征", "重型火箭", "载人登月", "发动机试车"),
                imageUrl = "https://images.unsplash.com/photo-1517976487502-570a273b0616?q=80&w=800&auto=format&fit=crop",
                impactBadge = "里程碑突破",
                technicalSpecs = mapOf(
                    "地月轨道运力" to "≥ 27 吨",
                    "箭体直径" to "5.0 米",
                    "起飞总推力" to "约 2678 吨",
                    "发动机类型" to "YF-100K 高压补燃"
                )
            ),
            SpaceArticle(
                id = "art_03",
                title = "SpaceX 星舰 (Starship) 完成第八次综合试飞技术评审：全新不锈钢合金与筷子臂精准回收",
                summary = "联邦航空管理局 (FAA) 完成环境与安全审批，SpaceX 正式进入星舰全尺寸轨道级试飞发射窗口期，力争实现双级完全可重复使用。",
                details = "星舰组合体高度达121米，由第一级超重型助推器 (Super Heavy) 和第二级星舰飞船构成。本次试飞着重验证太空微重力环境下的液氧甲烷自生增压点火测试，并在星舰入轨前重入大气层阶段，测试升级版蜂窝隔热瓦的抗烧蚀极限。回收塔机械臂“筷子”将再次尝试在高空拦截捕获超重助推器。",
                source = "SpaceX 官方技术日志",
                category = SpaceCategory.COMMERCIAL_SPACE,
                publishedAt = now - 3600000 * 9,
                formattedDate = dateFormat.format(Date(now - 3600000 * 9)),
                tags = listOf("星舰", "SpaceX", "可重复使用", "星际飞船"),
                imageUrl = "https://images.unsplash.com/photo-1541185933-ef5d8ed016c2?q=80&w=800&auto=format&fit=crop",
                impactBadge = "全球商业焦点",
                technicalSpecs = mapOf(
                    "总推力" to "74000 kN",
                    "二级运力" to "150 吨 (完全复用)",
                    "燃料组合" to "液氧 / 甲烷",
                    "整箭高度" to "121 米"
                )
            ),
            SpaceArticle(
                id = "art_04",
                title = "天宫空间站问天舱生命生态实验柜迎重大突破：首次在微重力环境完成斑马鱼全生命周期繁育",
                summary = "科研团队通过水下密闭生态循环装置，成功解析骨量流失与心血管重塑在空间微重力下的分子机理，为长期星际旅行提供防护依据。",
                details = "中国空间站微重力科学实验柜运行稳定。斑马鱼和金鱼藻组成的密闭小型水生生态系统中，斑马鱼完成了定向产卵、胚胎发育并孵化出子代幼鱼。实验揭示了成骨细胞和破骨细胞在失重环境下的力学敏感通路，对航天员长期驻留骨质疏松防治及地面退行性病变药物开发具有重大价值。",
                source = "中国科学院空间应用工程与技术中心",
                category = SpaceCategory.SPACE_SCIENCE,
                publishedAt = now - 3600000 * 14,
                formattedDate = dateFormat.format(Date(now - 3600000 * 14)),
                tags = listOf("空间站", "天宫", "微重力科研", "生命科学"),
                imageUrl = "https://images.unsplash.com/photo-1446776811953-b23d57bd21aa?q=80&w=800&auto=format&fit=crop",
                impactBadge = "科学发现",
                technicalSpecs = mapOf(
                    "实验轨道" to "400 公里近地圆轨道",
                    "实验周期" to "45 天连续监测",
                    "循环水体体积" to "1.25 升高精度自循环",
                    "温控精度" to "± 0.2 ℃"
                )
            ),
            SpaceArticle(
                id = "art_05",
                title = "蓝箭航天朱雀三号可重复使用液体火箭完成十公里级垂直起降回收 (VTVL) 飞行试验",
                summary = "自主研发的天鹊系列液氧甲烷发动机实现大范围变推力平稳着陆，国内首款不锈钢箭体可重复使用火箭迈入最后定型阶段。",
                details = "试验在酒泉卫星发射中心蓝箭航天发射场进行，火箭在空中经历加速上升、发动机一次关机、无动力滑行、发动机二次点火减速及反推悬停，最后在着陆场四条着陆缓冲腿稳稳展开接触地面。全程着陆点偏差小于1.8米，姿态角误差小于0.3度，标志着我国商业航天完全具备重型火箭一子级回收核心能力。",
                source = "蓝箭航天 (LandSpace)",
                category = SpaceCategory.COMMERCIAL_SPACE,
                publishedAt = now - 3600000 * 20,
                formattedDate = dateFormat.format(Date(now - 3600000 * 20)),
                tags = listOf("可重复使用", "液氧甲烷", "商业航天", "垂直起降"),
                imageUrl = "https://images.unsplash.com/photo-1516849841032-87cbac4d88f7?q=80&w=800&auto=format&fit=crop",
                impactBadge = "国内民营首创",
                technicalSpecs = mapOf(
                    "起飞重量" to "约 68 吨",
                    "变推力范围" to "50% ~ 110%",
                    "着陆精度" to "< 1.8 米",
                    "重复使用目标" to "≥ 20 次"
                )
            ),
            SpaceArticle(
                id = "art_06",
                title = "天问二号小行星探测器进入正样研制阶段：瞄准小行星 2016 HO3 采样返回与彗星主带交会",
                summary = "中国深空探测重大专项稳步推进，计划于2025年发射，实现十克级近地小行星弱引力表面锚定采样并送回地球。",
                details = "天问二号采用电推进与化学推进混合构型，将首先飞越并环绕近地小行星‘振荡天星’ (2016 HO3)，通过机械臂抓取与微钻复合机构采集太阳系原始星云风化层样本。随后释放返回舱送回地球，轨道器继续利用地球引力弹弓加速，飞往数百个天文单位之外的311P活跃小行星/主带彗星展开多波段成像勘测。",
                source = "国家航天局探月与深空探测中心",
                category = SpaceCategory.DEEP_SPACE,
                publishedAt = now - 3600000 * 28,
                formattedDate = dateFormat.format(Date(now - 3600000 * 28)),
                tags = listOf("天问", "深空探测", "小行星采样", "彗星探测"),
                imageUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop",
                impactBadge = "前沿深空任务",
                technicalSpecs = mapOf(
                    "探测目标" to "2016 HO3 & 311P",
                    "采样重量" to "≥ 100 克",
                    "全任务周期" to "约 10 年",
                    "主推进系统" to "LIPS-300 离子电推"
                )
            )
        )
    }

    private fun getInitialLaunches(): List<RocketLaunch> {
        val now = System.currentTimeMillis()
        return listOf(
            RocketLaunch(
                id = "launch_01",
                missionName = "星舰综合飞行测试 (Starship IFT-8)",
                rocketName = "Starship Super Heavy (B13 + S31)",
                launchSite = "美国德州博卡奇卡星舰基地 (Starbase Pad A)",
                scheduledEpochMs = now + (3600000 * 18 + 1420000), // ~18 hours
                status = LaunchStatus.COUNTDOWN,
                orbitType = "近地轨道跨大气层测试 (Suborbital / LEO)",
                payloadInfo = "星链 V2.0 模拟部署器 + 在轨推进剂低温转移实验舱",
                technicalHighlights = "33台猛禽3发动机全功率起飞，发射台机械臂双臂高空拦截捕获超重助推器。"
            ),
            RocketLaunch(
                id = "launch_02",
                missionName = "神舟二十号载人飞行任务",
                rocketName = "长征二号F 遥二十 (CZ-2F)",
                launchSite = "中国酒泉卫星发射中心载人航天发射场",
                scheduledEpochMs = now + (3600000 * 46 + 2800000), // ~46 hours
                status = LaunchStatus.FUEL_LOADING,
                orbitType = "近地轨道 (LEO 390km x 390km / 倾角 41.5°)",
                payloadInfo = "神舟载人飞船 + 3名中国航天员乘组 + 空间站科研耗材",
                technicalHighlights = "高可靠逃逸塔救生保障，6.5小时自主快速交会对接天和核心舱前向对接口。"
            ),
            RocketLaunch(
                id = "launch_03",
                missionName = "阿尔忒弥斯二号载人绕月飞行测试 (Artemis II)",
                rocketName = "太空发射系统重型 (SLS Block 1)",
                launchSite = "肯尼迪航天中心 LC-39B",
                scheduledEpochMs = now + (3600000 * 128 + 940000), // ~5 days
                status = LaunchStatus.READY,
                orbitType = "自由返回地月转移轨道 (TLI)",
                payloadInfo = "猎户座 (Orion) 载人飞船 + 4名多国宇航员乘组",
                technicalHighlights = "自阿波罗计划后人类首次飞越月球背面，最大深空通讯距离超40万公里。"
            ),
            RocketLaunch(
                id = "launch_04",
                missionName = "长征十号甲首飞技术验证星",
                rocketName = "长征十号甲 (CZ-10A) 两级半构型",
                launchSite = "中国文昌航天发射场 2号发射工位",
                scheduledEpochMs = now + (3600000 * 210), // ~8 days
                status = LaunchStatus.READY,
                orbitType = "低倾角近地轨道 (LEO 14吨运力验证)",
                payloadInfo = "可重复使用一子级栅格舵回收验证包 + 梦舟飞船配重舱",
                technicalHighlights = "首次验证海上网捕系缆回收或定点网格支架着陆，一子级发动机推力可调。"
            )
        )
    }
}
