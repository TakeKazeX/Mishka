package top.yukonga.mishka.ui.screen.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import mishka.shared.generated.resources.Res
import mishka.shared.generated.resources.common_back
import mishka.shared.generated.resources.common_cancel
import mishka.shared.generated.resources.common_confirm
import mishka.shared.generated.resources.settings_predictive_back
import mishka.shared.generated.resources.settings_predictive_back_summary
import mishka.shared.generated.resources.settings_theme_accent_blue
import mishka.shared.generated.resources.settings_theme_accent_default
import mishka.shared.generated.resources.settings_theme_accent_green
import mishka.shared.generated.resources.settings_theme_accent_orange
import mishka.shared.generated.resources.settings_theme_accent_pink
import mishka.shared.generated.resources.settings_theme_accent_purple
import mishka.shared.generated.resources.settings_theme_accent_red
import mishka.shared.generated.resources.settings_theme_accent_teal
import mishka.shared.generated.resources.settings_theme_accent_title
import mishka.shared.generated.resources.settings_theme_accent_yellow
import mishka.shared.generated.resources.settings_theme_bottom_bar_blur
import mishka.shared.generated.resources.settings_theme_bottom_bar_blur_summary
import mishka.shared.generated.resources.settings_theme_bottom_bar_icon_and_text
import mishka.shared.generated.resources.settings_theme_bottom_bar_icon_only
import mishka.shared.generated.resources.settings_theme_bottom_bar_mode
import mishka.shared.generated.resources.settings_theme_blur
import mishka.shared.generated.resources.settings_theme_blur_summary
import mishka.shared.generated.resources.settings_theme_dark
import mishka.shared.generated.resources.settings_theme_density_scale
import mishka.shared.generated.resources.settings_theme_density_scale_range
import mishka.shared.generated.resources.settings_theme_density_scale_summary
import mishka.shared.generated.resources.settings_theme_floating_bottom_bar
import mishka.shared.generated.resources.settings_theme_floating_bottom_bar_style
import mishka.shared.generated.resources.settings_theme_floating_bottom_bar_style_ios_like
import mishka.shared.generated.resources.settings_theme_floating_bottom_bar_style_miuix
import mishka.shared.generated.resources.settings_theme_floating_bottom_bar_summary
import mishka.shared.generated.resources.settings_theme_group_color
import mishka.shared.generated.resources.settings_theme_group_interface
import mishka.shared.generated.resources.settings_theme_group_navigation
import mishka.shared.generated.resources.settings_theme_light
import mishka.shared.generated.resources.settings_theme_mode
import mishka.shared.generated.resources.settings_theme_monet
import mishka.shared.generated.resources.settings_theme_monet_summary
import mishka.shared.generated.resources.settings_theme_palette_expressive
import mishka.shared.generated.resources.settings_theme_palette_content
import mishka.shared.generated.resources.settings_theme_palette_fidelity
import mishka.shared.generated.resources.settings_theme_palette_fruit_salad
import mishka.shared.generated.resources.settings_theme_palette_monochrome
import mishka.shared.generated.resources.settings_theme_palette_neutral
import mishka.shared.generated.resources.settings_theme_palette_rainbow
import mishka.shared.generated.resources.settings_theme_palette_style
import mishka.shared.generated.resources.settings_theme_palette_tonal_spot
import mishka.shared.generated.resources.settings_theme_palette_vibrant
import mishka.shared.generated.resources.settings_theme_pure_black
import mishka.shared.generated.resources.settings_theme_pure_black_summary
import mishka.shared.generated.resources.settings_theme_system
import mishka.shared.generated.resources.settings_theme_title
import org.jetbrains.compose.resources.stringResource
import top.yukonga.mishka.platform.PlatformStorage
import top.yukonga.mishka.platform.StorageKeys
import top.yukonga.mishka.ui.component.AdaptiveTopAppBar
import top.yukonga.mishka.ui.component.CardItem
import top.yukonga.mishka.ui.component.blur.BlurredBar
import top.yukonga.mishka.ui.component.blur.rememberBlurBackdrop
import top.yukonga.mishka.ui.component.groupedCardItems
import top.yukonga.mishka.ui.theme.BottomBarMode
import top.yukonga.mishka.ui.theme.FloatingBottomBarStyle
import top.yukonga.mishka.ui.theme.MaxDensityScale
import top.yukonga.mishka.ui.theme.MinDensityScale
import top.yukonga.mishka.ui.theme.ThemeAccentColor
import top.yukonga.mishka.ui.theme.ThemeConfig
import top.yukonga.mishka.ui.theme.ThemePaletteStyles
import top.yukonga.mishka.ui.theme.normalizeDensityScale
import top.yukonga.mishka.ui.theme.writeThemeConfig
import top.yukonga.mishka.ui.util.horizontalCutoutPadding
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic
import top.yukonga.miuix.kmp.window.WindowDialog
import kotlin.math.roundToInt

@Composable
fun ThemeSettingsScreen(
    storage: PlatformStorage,
    themeConfig: ThemeConfig,
    onThemeConfigChange: (ThemeConfig) -> Unit,
    onPredictiveBackChange: ((Boolean) -> Unit)? = null,
    onBack: () -> Unit = {},
) {
    val scrollBehavior = MiuixScrollBehavior()
    var isPredictiveBackEnabled by remember {
        mutableStateOf(storage.getString(StorageKeys.PREDICTIVE_BACK, "false") == "true")
    }
    var densityScaleDraft by remember { mutableStateOf((themeConfig.densityScale * 100f).roundToInt().toFloat()) }
    var showDensityScaleDialog by remember { mutableStateOf(false) }
    val densityScaleTextState = rememberTextFieldState()

    LaunchedEffect(themeConfig.densityScale) {
        densityScaleDraft = (themeConfig.densityScale * 100f).roundToInt().toFloat()
    }

    fun updateTheme(next: ThemeConfig) {
        writeThemeConfig(storage, next)
        onThemeConfigChange(next)
    }

    fun updateDensityScale(percent: Float) {
        val nextPercent = percent.coerceIn(
            MinDensityScale * 100f,
            MaxDensityScale * 100f,
        )
        val nextScale = normalizeDensityScale(nextPercent / 100f)
        densityScaleDraft = nextScale * 100f
        updateTheme(themeConfig.copy(densityScale = nextScale))
    }

    fun openDensityScaleDialog() {
        densityScaleTextState.setTextAndPlaceCursorAtEnd(formatDensityScaleInput(densityScaleDraft))
        showDensityScaleDialog = true
    }

    val themeItems = listOf(
        stringResource(Res.string.settings_theme_system),
        stringResource(Res.string.settings_theme_light),
        stringResource(Res.string.settings_theme_dark),
    )
    val paletteStyles = ThemePaletteStyles
    val paletteItems = paletteStyles.map { style -> style.label() }
    val selectedPaletteIndex = paletteStyles.indexOf(themeConfig.paletteStyle).coerceAtLeast(0)
    val accentOptions = ThemeAccentColor.entries.toList()
    val accentItems = accentOptions.map { accent -> accent.label() }
    val selectedAccentIndex = accentOptions.indexOf(themeConfig.accentColor).coerceAtLeast(0)
    val floatingBottomBarStyles = FloatingBottomBarStyle.entries.toList()
    val floatingBottomBarStyleItems = floatingBottomBarStyles.map { style -> style.label() }
    val selectedFloatingBottomBarStyleIndex =
        floatingBottomBarStyles.indexOf(themeConfig.floatingBottomBarStyle).coerceAtLeast(0)
    val bottomBarModes = BottomBarMode.entries.toList()
    val bottomBarModeItems = bottomBarModes.map { mode -> mode.label() }
    val selectedBottomBarModeIndex = bottomBarModes.indexOf(themeConfig.bottomBarMode).coerceAtLeast(0)
    val isBlurSupported = isRuntimeShaderSupported()

    val backdrop = rememberBlurBackdrop()
    val blurActive = backdrop != null
    val barColor = if (blurActive) Color.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop = backdrop, blurActive = blurActive) {
                AdaptiveTopAppBar(
                    title = stringResource(Res.string.settings_theme_title),
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = stringResource(Res.string.common_back),
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .horizontalCutoutPadding()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier)
                .scrollEndHaptic()
                .overScrollVertical()
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = innerPadding.calculateTopPadding()),
        ) {
            item { SmallTitle(text = stringResource(Res.string.settings_theme_group_color)) }
            groupedCardItems(
                keyPrefix = "theme_color",
                items = listOf(
                    CardItem("mode") {
                        OverlayDropdownPreference(
                            title = stringResource(Res.string.settings_theme_mode),
                            summary = themeItems.getOrElse(themeConfig.colorMode) { themeItems.first() },
                            items = themeItems,
                            selectedIndex = themeConfig.colorMode,
                            onSelectedIndexChange = { index ->
                                updateTheme(themeConfig.copy(colorMode = index))
                            },
                        )
                    },
                    CardItem("monet") {
                        SwitchPreference(
                            title = stringResource(Res.string.settings_theme_monet),
                            summary = stringResource(Res.string.settings_theme_monet_summary),
                            checked = themeConfig.useMonet,
                            onCheckedChange = { checked ->
                                updateTheme(themeConfig.copy(useMonet = checked))
                            },
                        )
                        AnimatedVisibility(
                            visible = themeConfig.useMonet,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        ) {
                            Column {
                                OverlayDropdownPreference(
                                    title = stringResource(Res.string.settings_theme_palette_style),
                                    summary = paletteItems.getOrElse(selectedPaletteIndex) { paletteItems.first() },
                                    items = paletteItems,
                                    selectedIndex = selectedPaletteIndex,
                                    onSelectedIndexChange = { index ->
                                        updateTheme(themeConfig.copy(paletteStyle = paletteStyles[index]))
                                    },
                                )
                                OverlayDropdownPreference(
                                    title = stringResource(Res.string.settings_theme_accent_title),
                                    summary = accentItems.getOrElse(selectedAccentIndex) { accentItems.first() },
                                    items = accentItems,
                                    selectedIndex = selectedAccentIndex,
                                    onSelectedIndexChange = { index ->
                                        updateTheme(themeConfig.copy(accentColor = accentOptions[index]))
                                    },
                                )
                                SwitchPreference(
                                    title = stringResource(Res.string.settings_theme_pure_black),
                                    summary = stringResource(Res.string.settings_theme_pure_black_summary),
                                    checked = themeConfig.pureBlack,
                                    onCheckedChange = { checked ->
                                        updateTheme(themeConfig.copy(pureBlack = checked))
                                    },
                                )
                            }
                        }
                    },
                ),
            )

            item { SmallTitle(text = stringResource(Res.string.settings_theme_group_interface)) }
            groupedCardItems(
                keyPrefix = "theme_interface",
                items = buildList {
                    add(CardItem("blur") {
                        SwitchPreference(
                            title = stringResource(Res.string.settings_theme_blur),
                            summary = stringResource(Res.string.settings_theme_blur_summary),
                            checked = themeConfig.blurEnabled && isBlurSupported,
                            onCheckedChange = { checked ->
                                updateTheme(themeConfig.copy(blurEnabled = checked))
                            },
                            enabled = isBlurSupported,
                        )
                    })
                    if (onPredictiveBackChange != null) {
                        add(CardItem("predictiveBack") {
                            SwitchPreference(
                                title = stringResource(Res.string.settings_predictive_back),
                                summary = stringResource(Res.string.settings_predictive_back_summary),
                                checked = isPredictiveBackEnabled,
                                onCheckedChange = { checked ->
                                    storage.putString(StorageKeys.PREDICTIVE_BACK, checked.toString())
                                    isPredictiveBackEnabled = checked
                                    onPredictiveBackChange(checked)
                                },
                            )
                        })
                    }
                    add(CardItem("densityScale") {
                        DensityScalePreference(
                            title = stringResource(Res.string.settings_theme_density_scale),
                            summary = stringResource(Res.string.settings_theme_density_scale_summary),
                            value = densityScaleDraft.coerceIn(
                                MinDensityScale * 100f,
                                MaxDensityScale * 100f,
                            ),
                            valueText = formatDensityScalePercent(densityScaleDraft),
                            valueRange = (MinDensityScale * 100f)..(MaxDensityScale * 100f),
                            keyPoints = listOf(80f, 90f, 100f, 110f),
                            onClick = ::openDensityScaleDialog,
                            onValueChange = { value ->
                                densityScaleDraft = value
                            },
                            onValueChangeFinished = {
                                updateDensityScale(densityScaleDraft)
                            },
                        )
                    })
                },
            )

            item { SmallTitle(text = stringResource(Res.string.settings_theme_group_navigation)) }
            groupedCardItems(
                keyPrefix = "theme_navigation",
                items = listOf(
                    CardItem("floating") {
                        SwitchPreference(
                            title = stringResource(Res.string.settings_theme_floating_bottom_bar),
                            summary = stringResource(Res.string.settings_theme_floating_bottom_bar_summary),
                            checked = themeConfig.floatingBottomBar,
                            onCheckedChange = { checked ->
                                updateTheme(themeConfig.copy(floatingBottomBar = checked))
                            },
                        )
                        AnimatedVisibility(
                            visible = themeConfig.floatingBottomBar,
                            enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                        ) {
                            OverlayDropdownPreference(
                                title = stringResource(Res.string.settings_theme_floating_bottom_bar_style),
                                summary = floatingBottomBarStyleItems.getOrElse(
                                    selectedFloatingBottomBarStyleIndex,
                                ) { floatingBottomBarStyleItems.first() },
                                items = floatingBottomBarStyleItems,
                                selectedIndex = selectedFloatingBottomBarStyleIndex,
                                onSelectedIndexChange = { index ->
                                    updateTheme(
                                        themeConfig.copy(
                                            floatingBottomBarStyle = floatingBottomBarStyles[index],
                                        ),
                                    )
                                },
                            )
                        }
                    },
                    CardItem("mode") {
                        OverlayDropdownPreference(
                            title = stringResource(Res.string.settings_theme_bottom_bar_mode),
                            summary = bottomBarModeItems.getOrElse(
                                selectedBottomBarModeIndex,
                            ) { bottomBarModeItems.first() },
                            items = bottomBarModeItems,
                            selectedIndex = selectedBottomBarModeIndex,
                            onSelectedIndexChange = { index ->
                                updateTheme(
                                    themeConfig.copy(
                                        bottomBarMode = bottomBarModes[index],
                                    ),
                                )
                            },
                        )
                    },
                    CardItem("blur") {
                        SwitchPreference(
                            title = stringResource(Res.string.settings_theme_bottom_bar_blur),
                            summary = stringResource(Res.string.settings_theme_bottom_bar_blur_summary),
                            checked = themeConfig.bottomBarBlurEnabled && themeConfig.blurEnabled && isBlurSupported,
                            onCheckedChange = { checked ->
                                updateTheme(themeConfig.copy(bottomBarBlurEnabled = checked))
                            },
                            enabled = themeConfig.blurEnabled && isBlurSupported,
                        )
                    },
                ),
            )
            item { Spacer(Modifier.height(24.dp).navigationBarsPadding()) }

        }
    }

    DensityScaleDialog(
        show = showDensityScaleDialog,
        textState = densityScaleTextState,
        onDismiss = { showDensityScaleDialog = false },
        onConfirm = { percent ->
            updateDensityScale(percent)
            showDensityScaleDialog = false
        },
    )
}

@Composable
private fun DensityScalePreference(
    title: String,
    summary: String,
    value: Float,
    valueText: String,
    valueRange: ClosedFloatingPointRange<Float>,
    keyPoints: List<Float>,
    onClick: () -> Unit,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
) {
    BasicComponent(
        bottomAction = {
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                valueRange = valueRange,
                onValueChangeFinished = onValueChangeFinished,
                showKeyPoints = true,
                keyPoints = keyPoints,
            )
        },
        endActions = {
            Text(
                text = valueText,
                modifier = Modifier.clickable(onClick = onClick),
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantActions,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
        ) {
            Text(
                text = title,
                fontSize = MiuixTheme.textStyles.headline1.fontSize,
                color = MiuixTheme.colorScheme.onBackground,
            )
            Text(
                text = summary,
                fontSize = MiuixTheme.textStyles.body2.fontSize,
                color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            )
        }
    }
}

@Composable
private fun DensityScaleDialog(
    show: Boolean,
    textState: TextFieldState,
    onDismiss: () -> Unit,
    onConfirm: (Float) -> Unit,
) {
    WindowDialog(
        show = show,
        title = stringResource(Res.string.settings_theme_density_scale),
        summary = stringResource(Res.string.settings_theme_density_scale_summary),
        onDismissRequest = onDismiss,
    ) {
        TextField(
            state = textState,
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(Res.string.settings_theme_density_scale_range),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TextButton(
                text = stringResource(Res.string.common_cancel),
                modifier = Modifier.weight(1f),
                onClick = onDismiss,
            )
            TextButton(
                text = stringResource(Res.string.common_confirm),
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.textButtonColorsPrimary(),
                onClick = {
                    val percent = textState.text.toString().trim().removeSuffix("%").toFloatOrNull()
                    if (percent != null) {
                        onConfirm(percent.coerceIn(
                            MinDensityScale * 100f,
                            MaxDensityScale * 100f,
                        ))
                    }
                },
            )
        }
    }
}

private fun formatDensityScalePercent(value: Float): String = "${formatDensityScaleInput(value)}%"

private fun formatDensityScaleInput(value: Float): String {
    val tenths = (value * 10f).roundToInt()
    return if (tenths % 10 == 0) {
        (tenths / 10).toString()
    } else {
        "${tenths / 10}.${tenths % 10}"
    }
}

@Composable
private fun ThemePaletteStyle.label(): String = stringResource(
    when (this) {
        ThemePaletteStyle.Neutral -> Res.string.settings_theme_palette_neutral
        ThemePaletteStyle.Vibrant -> Res.string.settings_theme_palette_vibrant
        ThemePaletteStyle.Expressive -> Res.string.settings_theme_palette_expressive
        ThemePaletteStyle.Rainbow -> Res.string.settings_theme_palette_rainbow
        ThemePaletteStyle.FruitSalad -> Res.string.settings_theme_palette_fruit_salad
        ThemePaletteStyle.Monochrome -> Res.string.settings_theme_palette_monochrome
        ThemePaletteStyle.Fidelity -> Res.string.settings_theme_palette_fidelity
        ThemePaletteStyle.Content -> Res.string.settings_theme_palette_content
        else -> Res.string.settings_theme_palette_tonal_spot
    },
)

@Composable
private fun ThemeAccentColor.label(): String = stringResource(
    when (this) {
        ThemeAccentColor.Default -> Res.string.settings_theme_accent_default
        ThemeAccentColor.Blue -> Res.string.settings_theme_accent_blue
        ThemeAccentColor.Purple -> Res.string.settings_theme_accent_purple
        ThemeAccentColor.Pink -> Res.string.settings_theme_accent_pink
        ThemeAccentColor.Red -> Res.string.settings_theme_accent_red
        ThemeAccentColor.Orange -> Res.string.settings_theme_accent_orange
        ThemeAccentColor.Yellow -> Res.string.settings_theme_accent_yellow
        ThemeAccentColor.Green -> Res.string.settings_theme_accent_green
        ThemeAccentColor.Teal -> Res.string.settings_theme_accent_teal
    },
)

@Composable
private fun FloatingBottomBarStyle.label(): String = stringResource(
    when (this) {
        FloatingBottomBarStyle.Miuix -> Res.string.settings_theme_floating_bottom_bar_style_miuix
        FloatingBottomBarStyle.IosLike -> Res.string.settings_theme_floating_bottom_bar_style_ios_like
    },
)

@Composable
private fun BottomBarMode.label(): String = stringResource(
    when (this) {
        BottomBarMode.IconAndText -> Res.string.settings_theme_bottom_bar_icon_and_text
        BottomBarMode.IconOnly -> Res.string.settings_theme_bottom_bar_icon_only
    },
)
