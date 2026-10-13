package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AlertHistoryEntity
import com.example.data.local.entity.SubscriptionKeywordEntity
import com.example.data.model.RocketLaunch
import com.example.data.model.SpaceArticle
import com.example.data.model.SpaceCategory
import com.example.data.repository.SpaceRepository
import com.example.notification.CosmoNotificationManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class SpaceUiState(
    val articles: List<SpaceArticle> = emptyList(),
    val filteredArticles: List<SpaceArticle> = emptyList(),
    val selectedCategory: SpaceCategory = SpaceCategory.ALL,
    val searchQuery: String = "",
    val launches: List<RocketLaunch> = emptyList(),
    val keywords: List<SubscriptionKeywordEntity> = emptyList(),
    val alerts: List<AlertHistoryEntity> = emptyList(),
    val unreadAlertCount: Int = 0,
    val bookmarkedIds: Set<String> = emptySet(),
    val isRefreshing: Boolean = false,
    val lastRefreshedText: String = "刚刚",
    val selectedArticle: SpaceArticle? = null,
    val hasNotificationPermission: Boolean = false,
    val userBannerMessage: String? = null
)

class SpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val notificationManager = CosmoNotificationManager(application)
    private val database = AppDatabase.getInstance(application)
    private val repository = SpaceRepository(database.subscriptionDao(), notificationManager)

    private val _selectedCategory = MutableStateFlow(SpaceCategory.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _isRefreshing = MutableStateFlow(false)
    private val _lastRefreshedText = MutableStateFlow("刚刚")
    private val _selectedArticle = MutableStateFlow<SpaceArticle?>(null)
    private val _hasNotificationPermission = MutableStateFlow(notificationManager.hasNotificationPermission())
    private val _userBannerMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SpaceUiState> = combine(
        repository.articles,
        repository.launches,
        repository.subscribedKeywords,
        repository.alertHistory,
        repository.bookmarks,
        _selectedCategory,
        _searchQuery,
        _isRefreshing,
        _lastRefreshedText,
        _selectedArticle,
        _hasNotificationPermission,
        _userBannerMessage
    ) { params ->
        @Suppress("UNCHECKED_CAST")
        val articles = params[0] as List<SpaceArticle>
        @Suppress("UNCHECKED_CAST")
        val launches = params[1] as List<RocketLaunch>
        @Suppress("UNCHECKED_CAST")
        val keywords = params[2] as List<SubscriptionKeywordEntity>
        @Suppress("UNCHECKED_CAST")
        val alerts = params[3] as List<AlertHistoryEntity>
        @Suppress("UNCHECKED_CAST")
        val bookmarks = params[4] as List<com.example.data.local.entity.BookmarkEntity>
        val category = params[5] as SpaceCategory
        val query = params[6] as String
        val isRefreshing = params[7] as Boolean
        val lastRefreshed = params[8] as String
        val selectedArticle = params[9] as? SpaceArticle
        val hasPermission = params[10] as Boolean
        val bannerMessage = params[11] as? String

        val filtered = articles.filter { article ->
            val matchCat = category == SpaceCategory.ALL || article.category == category
            val matchQuery = query.isBlank() ||
                    article.title.contains(query, ignoreCase = true) ||
                    article.summary.contains(query, ignoreCase = true) ||
                    article.tags.any { it.contains(query, ignoreCase = true) }
            matchCat && matchQuery
        }

        val unreadCount = alerts.count { !it.isRead }
        val bookmarkedSet = bookmarks.map { it.articleId }.toSet()

        SpaceUiState(
            articles = articles,
            filteredArticles = filtered,
            selectedCategory = category,
            searchQuery = query,
            launches = launches,
            keywords = keywords,
            alerts = alerts,
            unreadAlertCount = unreadCount,
            bookmarkedIds = bookmarkedSet,
            isRefreshing = isRefreshing,
            lastRefreshedText = lastRefreshed,
            selectedArticle = selectedArticle,
            hasNotificationPermission = hasPermission,
            userBannerMessage = bannerMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SpaceUiState()
    )

    init {
        viewModelScope.launch {
            repository.initializeDefaultKeywordsIfEmpty()
        }
    }

    fun setCategory(category: SpaceCategory) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectArticle(article: SpaceArticle?) {
        _selectedArticle.value = article
    }

    fun dismissBannerMessage() {
        _userBannerMessage.value = null
    }

    fun updateNotificationPermission(granted: Boolean) {
        _hasNotificationPermission.value = granted
    }

    fun refreshNow() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val triggeredKeywords = repository.refreshArticles(simulateNewBreakthrough = true)
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.CHINA).format(Date())
                _lastRefreshedText.value = "已于 $timeStr 刷新"
                if (triggeredKeywords.isNotEmpty()) {
                    _userBannerMessage.value = "雷达扫描到新动态！已命中关键词【${triggeredKeywords.joinToString("、")}】并发送推送"
                } else {
                    _userBannerMessage.value = "航天前沿数据库已更新至最新"
                }
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun addKeyword(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            repository.addKeyword(trimmed)
            _userBannerMessage.value = "已成功添加订阅关键词【$trimmed】"
        }
    }

    fun removeKeyword(keyword: String) {
        viewModelScope.launch {
            repository.removeKeyword(keyword)
            _userBannerMessage.value = "已取消订阅【$keyword】"
        }
    }

    fun toggleKeyword(keyword: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleKeyword(keyword, enabled)
        }
    }

    fun testSimulatePush(keyword: String) {
        viewModelScope.launch {
            val success = repository.testSimulatePush(keyword)
            if (success) {
                _userBannerMessage.value = "已成功向系统状态栏发送【$keyword】测试推送通知！"
            } else {
                _userBannerMessage.value = "系统通知权限受限或未授权，请检查通知设置"
            }
        }
    }

    fun scanAndTriggerAllMatches() {
        viewModelScope.launch {
            val count = repository.scanAndTriggerAllMatches()
            if (count > 0) {
                _userBannerMessage.value = "全盘雷达扫描完成，已向系统发送 $count 条关键词预警推送！"
            } else {
                _userBannerMessage.value = "雷达扫描完成，暂无新匹配动态"
            }
        }
    }

    fun toggleBookmark(article: SpaceArticle) {
        viewModelScope.launch {
            val isSaved = uiState.value.bookmarkedIds.contains(article.id)
            repository.toggleBookmark(article, isSaved)
            _userBannerMessage.value = if (isSaved) "已取消收藏" else "已收藏至本地数据库"
        }
    }

    fun markAlertRead(alertId: Long) {
        viewModelScope.launch {
            repository.markAlertRead(alertId)
        }
    }

    fun clearAllAlerts() {
        viewModelScope.launch {
            repository.clearAllAlerts()
            _userBannerMessage.value = "已清空推送历史"
        }
    }
}
