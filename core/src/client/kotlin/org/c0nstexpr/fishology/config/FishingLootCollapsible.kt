package org.c0nstexpr.fishology.config

import io.wispforest.owo.config.Option
import io.wispforest.owo.config.annotation.Expanded
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.core.Component.FocusSource
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class FishingLootCollapsible(private val option: Option<Set<FishingLoot>>) :
    CollapsibleContainer(
        Sizing.fill(100),
        Sizing.content(),
        Text.translatable(option.translationKey()),
        option.backingField().field().isAnnotationPresent(Expanded::class.java),
    ),
    OptionValueProvider {
    private val dropdown = FishingLootDropdown().apply { valueSet.addAll(option.value()) }

    private var valueSet: Set<FishingLoot>
        get() = dropdown.valueSet
        set(value) {
            dropdown.valueSet.clear()
            dropdown.valueSet.addAll(value)
            resetButton.active = isActive()
        }

    val resetButton: ButtonWidget =
        Components.button(Text.of("⇄")) {
            dropdown.valueSet = HashSet<FishingLoot>().apply { addAll(option.defaultValue()) }
            it.active = false
        }.apply {
            margins(Insets.right(10))
            positioning(Positioning.relative(100, 50))
            active = isActive()
        }

    private fun isActive() = option.run {
        !detached() && defaultValue().run { (count() != valueSet.count()) || containsAll(valueSet) }
    }

    init {
        titleLayout.apply {
            horizontalSizing(Sizing.fill(100))
            verticalSizing(Sizing.fixed(30))
            verticalAlignment(VerticalAlignment.CENTER)
            children(
                listOf(
                    resetButton,
                    SearchAnchorComponent(
                        this,
                        option.key(),
                        { I18n.translate(option.translationKey()) },
                    ),
                ),
            )
        }

        collapsibleChildren.add(dropdown)

        onToggled().subscribe {
            if (it) {
                dropdown.valueSet = HashSet<FishingLoot>().apply { addAll(option.value()) }
                focusHandler()?.focus(dropdown.children().last(), FocusSource.MOUSE_CLICK)
            } else {
                option.set(dropdown.valueSet)
            }
        }
    }

    override fun shouldDrawTooltip(
        mouseX: Double,
        mouseY: Double,
    ) = mouseY - y <= titleLayout.height() && super.shouldDrawTooltip(mouseX, mouseY)

    override fun isValid() = true

    override fun parsedValue(): Set<FishingLoot> = valueSet

    companion object {
        private fun LabelComponent.setTextColor(f: Formatting) = text(text().copy().styled { it.withColor(f) })
    }
}
