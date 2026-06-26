// MainNavigationScaffold.kt
// Location: app/src/main/java/com/cemcakmak/hydrotracker/presentation/common/MainNavigationScaffold.kt

package com.cemcakmak.hydrotracker.presentation.common

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShortNavigationBar
import androidx.compose.material3.ShortNavigationBarItem
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.containerCornerRadius
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.containerSize
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.material3.rememberTooltipState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import com.cemcakmak.hydrotracker.R
import com.cemcakmak.hydrotracker.data.models.NavBarLabelMode

/**
 * Composition local used by SettingsHubScreen to communicate its animated
 * depth-blur value to the common top app bar hosted in MainNavigationScaffold.
 */
val LocalSettingsHubBlur = compositionLocalOf<MutableState<Dp>> {
    error("LocalSettingsHubBlur must be provided by MainNavigationScaffold")
}

private const val NAV_BAR_ENTER_DURATION_MS = 250
private const val NAV_BAR_EXIT_DURATION_MS = 200

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainNavigationScaffold(
    backStack: NavBackStack<NavKey>,
    currentKey: NavigationRoutes,
    snackbarHostState: SnackbarHostState,
    isHistoryDaySelected: Boolean = false,
    onAddCustomClick: () -> Unit = {},
    onAddBeverageClick: () -> Unit = {},
    onAddContainerClick: () -> Unit = {},
    onTabSwitch: () -> Unit = {},
    autoHideNavBar: Boolean = false,
    navBarLabelMode: NavBarLabelMode = NavBarLabelMode.ALWAYS,
    content: @Composable (PaddingValues) -> Unit,
) {
    val shouldShowBottomBar = currentKey in setOf(
        NavigationRoutes.Home,
        NavigationRoutes.History,
        NavigationRoutes.Settings
    )

    // Auto-hide on scroll: hide the bar when scrolling down, reveal when scrolling up.
    val barVisibleByScroll = remember { mutableStateOf(true) }
    LaunchedEffect(currentKey) { barVisibleByScroll.value = true }
    val autoHideConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -0.5f) barVisibleByScroll.value = false
                else if (available.y > 0.5f) barVisibleByScroll.value = true
                return Offset.Zero
            }
        }
    }

    // Scroll behaviours remembered per route so collapsed state survives tab switches
    val historyScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val settingsScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    val nestedScrollModifier = run {
        val base = when (currentKey) {
            NavigationRoutes.History -> Modifier.nestedScroll(historyScrollBehavior.nestedScrollConnection)
            NavigationRoutes.Settings -> Modifier.nestedScroll(settingsScrollBehavior.nestedScrollConnection)
            else -> Modifier
        }
        if (autoHideNavBar) base.nestedScroll(autoHideConnection) else base
    }

    val settingsBlurState = remember { mutableStateOf(0.dp) }

    // Pixel height of the navigation bar, captured via onSizeChanged for the FAB offset.
    // Only used for graphicsLayer translation — not for layout constraints — so no feedback loop.
    val barHeightPx = remember { mutableFloatStateOf(0f) }
    val hideProgress by animateFloatAsState(
        targetValue = if (autoHideNavBar && !barVisibleByScroll.value) 1f else 0f,
        animationSpec = tween(
            durationMillis = if (barVisibleByScroll.value) NAV_BAR_ENTER_DURATION_MS else NAV_BAR_EXIT_DURATION_MS,
            easing = if (barVisibleByScroll.value) FastOutSlowInEasing else FastOutLinearInEasing
        ),
        label = "navBarHide"
    )

    val fabVisible = currentKey == NavigationRoutes.Home ||
            (currentKey == NavigationRoutes.History && isHistoryDaySelected)

    CompositionLocalProvider(LocalSettingsHubBlur provides settingsBlurState) {
        Scaffold(
            modifier = nestedScrollModifier,
            topBar = {
                if (currentKey == NavigationRoutes.History || currentKey == NavigationRoutes.Settings) {
                    MainTabTopAppBar(
                        titleResId = if (currentKey == NavigationRoutes.History) {
                            R.string.nav_history
                        } else {
                            R.string.nav_settings
                        },
                        scrollBehavior = if (currentKey == NavigationRoutes.History) {
                            historyScrollBehavior
                        } else {
                            settingsScrollBehavior
                        },
                        blur = if (currentKey == NavigationRoutes.Settings) {
                            settingsBlurState.value
                        } else {
                            0.dp
                        }
                    )
                }
            },
            bottomBar = {
                if (shouldShowBottomBar) {
                    HydroNavigationBar(
                        modifier = Modifier
                            .onSizeChanged { barHeightPx.floatValue = it.height.toFloat() }
                            .graphicsLayer {
                                translationY = size.height * hideProgress
                                alpha = 1f - hideProgress
                            },
                        currentKey = currentKey,
                        labelMode = navBarLabelMode,
                        onTabSwitch = onTabSwitch,
                        onTabSelected = { key ->
                            backStack.apply {
                                clear()
                                add(key)
                            }
                        }
                    )
                }
            },
            floatingActionButton = {
                FloatingActionMenuButton(
                    modifier = Modifier.graphicsLayer {
                        translationY = barHeightPx.floatValue * hideProgress + 16.dp.toPx()
                    },
                    currentKey = currentKey,
                    fabVisible = fabVisible,
                    onAddCustomClick = onAddCustomClick,
                    onAddBeverageClick = onAddBeverageClick,
                    onAddContainerClick = onAddContainerClick
                )
            },
            snackbarHost = { HydroSnackbarHost(snackbarHostState) }
        ) { paddingValues ->
            content(paddingValues)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingActionMenuButton(
    modifier: Modifier = Modifier,
    currentKey: NavigationRoutes,
    fabVisible: Boolean,
    onAddCustomClick: () -> Unit,
    onAddBeverageClick: () -> Unit,
    onAddContainerClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val focusRequester = remember { FocusRequester() }
    val toggleMenuLabel = stringResource(R.string.fab_menu_toggle_menu)

    val items = when (currentKey) {
        NavigationRoutes.Home -> listOf(
            FabMenuItem(
                icon = ImageVector.vectorResource(R.drawable.blender_filled),
                labelResId = R.string.fab_menu_new_beverage,
                onClick = onAddBeverageClick
            ),
            FabMenuItem(
                icon = ImageVector.vectorResource(R.drawable.soft_drink_filled),
                labelResId = R.string.fab_menu_new_container,
                onClick = onAddContainerClick
            ),
            FabMenuItem(
                icon = ImageVector.vectorResource(R.drawable.add_filled),
                labelResId = R.string.nav_add_custom,
                onClick = onAddCustomClick
            )
        )
        NavigationRoutes.History -> listOf(
            FabMenuItem(
                icon = ImageVector.vectorResource(R.drawable.add_filled),
                labelResId = R.string.fab_menu_add_past_entry,
                onClick = onAddCustomClick
            )
        )
        else -> emptyList()
    }

    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }

    BackHandler(fabMenuExpanded) { fabMenuExpanded = false }

    // Collapse the menu whenever the FAB itself is hidden, so items do not pop back separately.
    LaunchedEffect(fabVisible) {
        if (!fabVisible) fabMenuExpanded = false
    }

    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = fabMenuExpanded,
        button = {
            TooltipBox(
                positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
                    if (fabMenuExpanded) {
                        TooltipAnchorPosition.Start
                    } else {
                        TooltipAnchorPosition.Above
                    }
                ),
                tooltip = {
                    PlainTooltip(
                        modifier =
                            Modifier.semantics {
                                liveRegion = LiveRegionMode.Assertive
                                paneTitle = toggleMenuLabel
                            }
                    ) {
                        Text(toggleMenuLabel)
                    }
                },
                state = rememberTooltipState()
            ) {
                ToggleFloatingActionButton(
                    modifier = Modifier
                        .semantics {
                            stateDescription = toggleMenuLabel
                            contentDescription = toggleMenuLabel
                        }
                        .animateFloatingActionButton(
                            visible = fabVisible || fabMenuExpanded,
                            alignment = Alignment.BottomEnd
                        )
                        .focusRequester(focusRequester),
                    containerSize = containerSize(80.dp, 55.dp),
                    containerCornerRadius = containerCornerRadius(24.dp, 55.dp),
                    checked = fabMenuExpanded,
                    onCheckedChange = {
                        haptics.performHapticFeedback(HapticFeedbackType.ContextClick)
                        fabMenuExpanded = !fabMenuExpanded
                    }
                ) {
                    val imageVector = if (checkedProgress > 0.5f) {
                        ImageVector.vectorResource(R.drawable.close_filled)
                    } else {
                        ImageVector.vectorResource(R.drawable.add_filled)
                    }
                    Icon(
                        painter = rememberVectorPainter(imageVector),
                        contentDescription = null,
                        modifier = Modifier.animateIcon({ checkedProgress })
                    )
                }
            }
        }
    ) {
        items.forEachIndexed { index, item ->
            FloatingActionButtonMenuItem(
                modifier = Modifier
                    .semantics {
                        if (index == items.size - 1) {
                            customActions = listOf(
                                CustomAccessibilityAction(
                                    label = toggleMenuLabel,
                                    action = {
                                        fabMenuExpanded = false
                                        true
                                    }
                                )
                            )
                        }
                    }
                    .then(
                        if (index == 0) {
                            Modifier.onKeyEvent {
                                if (
                                    it.type == KeyEventType.KeyDown &&
                                    (it.key == Key.DirectionUp ||
                                            it.key == Key.NumPadDirectionUp ||
                                            (it.isShiftPressed && it.key == Key.Tab))
                                ) {
                                    focusRequester.requestFocus()
                                    return@onKeyEvent true
                                }
                                return@onKeyEvent false
                            }
                        } else {
                            Modifier
                        }
                    ),
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    fabMenuExpanded = false
                    item.onClick()
                },
                icon = { Icon(item.icon, contentDescription = null) },
                text = { Text(text = stringResource(item.labelResId)) }
            )
        }
    }
}

private data class FabMenuItem(
    val icon: ImageVector,
    @StringRes val labelResId: Int,
    val onClick: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HydroNavigationBar(
    modifier: Modifier = Modifier,
    currentKey: NavigationRoutes,
    labelMode: NavBarLabelMode = NavBarLabelMode.ALWAYS,
    onTabSelected: (NavigationRoutes) -> Unit = {},
    onTabSwitch: () -> Unit = {}
) {
    ShortNavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        NavigationItem.entries.forEach { item ->
            val isSelected = currentKey == item.key
            val showLabel = labelMode == NavBarLabelMode.ALWAYS ||
                    (labelMode == NavBarLabelMode.SELECTED && isSelected)
            val tooltipState = rememberTooltipState()
            val haptics = LocalHapticFeedback.current

            LaunchedEffect(tooltipState.isVisible) {
                if (tooltipState.isVisible) {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }

            ShortNavigationBarItem(
                icon = {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = { PlainTooltip { Text(stringResource(item.labelResId)) } },
                        state = tooltipState
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                if (isSelected) item.selectedIconRes else item.iconRes
                            ),
                            contentDescription = stringResource(item.labelResId),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = if (showLabel) {
                    {
                        Text(
                            text = stringResource(item.labelResId),
                            style = MaterialTheme.typography.labelMediumEmphasized
                        )
                    }
                } else {
                    null
                },
                selected = isSelected,
                onClick = {
                    if (!isSelected) {
                        onTabSwitch()
                        onTabSelected(item.key)
                    }
                }
            )
        }
    }
}

enum class NavigationItem(
    val key: NavigationRoutes,
    @get:StringRes val labelResId: Int,
    @get:DrawableRes val iconRes: Int,
    @get:DrawableRes val selectedIconRes: Int
) {
    HOME(
        key = NavigationRoutes.Home,
        labelResId = R.string.nav_home,
        iconRes = R.drawable.home,
        selectedIconRes = R.drawable.home_filled
    ),
    HISTORY(
        key = NavigationRoutes.History,
        labelResId = R.string.nav_history,
        iconRes = R.drawable.leaderboard,
        selectedIconRes = R.drawable.leaderboard_filled
    ),
    SETTINGS(
        key = NavigationRoutes.Settings,
        labelResId = R.string.nav_settings,
        iconRes = R.drawable.settings,
        selectedIconRes = R.drawable.settings_filled
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Preview
@Composable
fun MainNavigationScaffoldPreview() {
    val backStack = rememberNavBackStack(NavigationRoutes.Home)
    MainNavigationScaffold(
        backStack = backStack,
        currentKey = NavigationRoutes.Home,
        snackbarHostState = remember { SnackbarHostState() },
        content = { _ ->
            Text(text = "Sample Content")
        }
    )
}
