package me.weishu.kernelsu.ui.screen.settings

import me.weishu.kernelsu.ui.theme.CustomTextGroup

/**
 * Which sections of a theme package take part in an import or export.
 *
 * Every flag defaults to true so an unfiltered package round-trips unchanged. The dialog masks
 * media flags with the sections the package actually carries, while the settings flags stay
 * user-controlled.
 */
data class ThemePackageOptions(
    val colors: Boolean = true,
    val backgroundImage: Boolean = true,
    val backgroundAppearance: Boolean = true,
    val homeCardImage: Boolean = true,
    val stickers: Boolean = true,
    val homeCardDisplay: Boolean = true,
    val appIcon: Boolean = true,
    val customTexts: Boolean = true,
    val customTextGroups: Set<CustomTextGroup> = CustomTextGroup.entries.toSet(),
    val interfaceMode: Boolean = true,
    val interfaceSwitches: Boolean = true,
    val interactionBehavior: Boolean = true,
    val moduleDisplay: Boolean = true,
    val customColors: Boolean = true,
    val imageEffects: Boolean = true,
    val bottomBarIcons: Boolean = true,
)
