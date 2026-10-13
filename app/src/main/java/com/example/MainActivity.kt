package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.ArticleDetailSheet
import com.example.ui.screens.BookmarksScreen
import com.example.ui.screens.LaunchScreen
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.SubscriptionScreen
import com.example.ui.theme.CosmicBackground
import com.example.ui.theme.CosmicVoid
import com.example.ui.theme.CosmoFrontierTheme
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PropulsionOrange
import com.example.ui.theme.SpaceBorder
import com.example.ui.theme.SpaceSurface
import com.example.ui.theme.SpaceSurfaceElevated
import com.example.ui.theme.SpaceSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SpaceViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialArticleId = intent.getStringExtra("EXTRA_ARTICLE_ID")

        setContent {
            CosmoFrontierTheme {
                MainApp(initialArticleId = initialArticleId)
            }
        }
    }
}

enum class MainTab(val label: String) {
    NEWS("资讯雷达"),
    LAUNCHES("发射中心"),
    SUBSCRIPTIONS("订阅推送"),
    BOOKMARKS("收藏库")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    initialArticleId: String? = null,
    viewModel: SpaceViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(MainTab.NEWS) }
    val context = LocalContext.current

    // Notification Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.updateNotificationPermission(isGranted)
    }

    // Auto-check notification permission on launch
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            viewModel.updateNotificationPermission(granted)
            if (!granted) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.updateNotificationPermission(true)
        }
    }

    // Handle initial article id from notification click
    LaunchedEffect(initialArticleId, state.articles) {
        if (!initialArticleId.isNullOrEmpty() && state.selectedArticle == null) {
            val found = state.articles.firstOrNull { it.id == initialArticleId }
            if (found != null) {
                viewModel.selectArticle(found)
            }
        }
    }

    // Handle back button
    BackHandler(enabled = selectedTab != MainTab.NEWS || state.selectedArticle != null) {
        if (state.selectedArticle != null) {
            viewModel.selectArticle(null)
        } else if (selectedTab != MainTab.NEWS) {
            selectedTab = MainTab.NEWS
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CosmicBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(ElectricCyan.copy(alpha = 0.15f))
                                .border(1.dp, ElectricCyan, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = ElectricCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "星穹航天",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                color = TextPrimary
                            )
                            Text(
                                text = "COSMO FRONTIER",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                color = ElectricCyan,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                },
                actions = {
                    // Quick Refresh Button
                    IconButton(
                        onClick = { viewModel.refreshNow() },
                        modifier = Modifier.testTag("topbar_refresh_button")
                    ) {
                        if (state.isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = ElectricCyan,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "刷新前沿",
                                tint = ElectricCyan
                            )
                        }
                    }

                    // Alerts Bell with Badge
                    IconButton(
                        onClick = { selectedTab = MainTab.SUBSCRIPTIONS },
                        modifier = Modifier.testTag("topbar_alerts_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (state.unreadAlertCount > 0) {
                                    Badge(
                                        containerColor = PropulsionOrange,
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = "${state.unreadAlertCount}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "订阅推送",
                                tint = if (selectedTab == MainTab.SUBSCRIPTIONS) ElectricCyan else TextSecondary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicVoid,
                    titleContentColor = TextPrimary
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = CosmicVoid,
                contentColor = TextSecondary,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == MainTab.NEWS,
                    onClick = { selectedTab = MainTab.NEWS },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "资讯雷达"
                        )
                    },
                    label = { Text("资讯雷达", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicVoid,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_news")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.LAUNCHES,
                    onClick = { selectedTab = MainTab.LAUNCHES },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = "发射中心"
                        )
                    },
                    label = { Text("发射中心", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicVoid,
                        selectedTextColor = PropulsionOrange,
                        indicatorColor = PropulsionOrange,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_launches")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.SUBSCRIPTIONS,
                    onClick = { selectedTab = MainTab.SUBSCRIPTIONS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.unreadAlertCount > 0) {
                                    Badge(containerColor = PropulsionOrange) {
                                        Text("${state.unreadAlertCount}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "订阅推送"
                            )
                        }
                    },
                    label = { Text("订阅推送", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicVoid,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_subscriptions")
                )

                NavigationBarItem(
                    selected = selectedTab == MainTab.BOOKMARKS,
                    onClick = { selectedTab = MainTab.BOOKMARKS },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (state.bookmarkedIds.isNotEmpty()) {
                                    Badge(containerColor = SpaceBorder) {
                                        Text("${state.bookmarkedIds.size}")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = "收藏库"
                            )
                        }
                    },
                    label = { Text("收藏库", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = CosmicVoid,
                        selectedTextColor = ElectricCyan,
                        indicatorColor = ElectricCyan,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_bookmarks")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.NEWS -> NewsScreen(
                    state = state,
                    onCategorySelected = viewModel::setCategory,
                    onSearchQueryChange = viewModel::setSearchQuery,
                    onRefresh = viewModel::refreshNow,
                    onArticleClick = viewModel::selectArticle,
                    onToggleBookmark = viewModel::toggleBookmark
                )

                MainTab.LAUNCHES -> LaunchScreen(
                    state = state
                )

                MainTab.SUBSCRIPTIONS -> SubscriptionScreen(
                    state = state,
                    onRequestNotificationPermission = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    onAddKeyword = viewModel::addKeyword,
                    onRemoveKeyword = viewModel::removeKeyword,
                    onToggleKeyword = viewModel::toggleKeyword,
                    onTestSimulatePush = viewModel::testSimulatePush,
                    onScanAllMatches = viewModel::scanAndTriggerAllMatches,
                    onSelectArticleById = { articleId ->
                        val article = state.articles.firstOrNull { it.id == articleId }
                        if (article != null) viewModel.selectArticle(article)
                    },
                    onClearAllAlerts = viewModel::clearAllAlerts
                )

                MainTab.BOOKMARKS -> BookmarksScreen(
                    state = state,
                    onArticleClick = viewModel::selectArticle,
                    onToggleBookmark = viewModel::toggleBookmark
                )
            }

            // Floating Banner Toast Notification
            AnimatedVisibility(
                visible = state.userBannerMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                state.userBannerMessage?.let { msg ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SpaceSurfaceElevated)
                            .border(1.dp, ElectricCyan, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = msg,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                            IconButton(
                                onClick = viewModel::dismissBannerMessage,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "关闭提示",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Article Detail Modal Bottom Sheet
    state.selectedArticle?.let { article ->
        ArticleDetailSheet(
            article = article,
            isBookmarked = state.bookmarkedIds.contains(article.id),
            onDismiss = { viewModel.selectArticle(null) },
            onToggleBookmark = { viewModel.toggleBookmark(article) }
        )
    }
}
