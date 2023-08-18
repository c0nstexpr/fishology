package org.c0nstexpr.fishology.config

import io.wispforest.owo.config.Option
import io.wispforest.owo.config.annotation.Expanded
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.container.CollapsibleContainer
import io.wispforest.owo.ui.core.Component.FocusSource
import io.wispforest.owo.ui.core.CursorStyle
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.ui.util.UISounds
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.Text
import net.minecraft.util.Formatting

class FishingLootCollapsible(private val option: Option<Set<FishingLoot>>) :
    CollapsibleContainer(
        Sizing.fill(100),
        Sizing.content(),
        Text.translatable(option.translationKey()),
        option.backingField().field().isAnnotationPresent(Expanded::class.java)
    ), OptionValueProvider {
    private val dropdown = FishingLootDropdown(option::translationKey)
    private val valueSet get() = dropdown.valueSet

    val resetButton: ButtonWidget =
        Components.button(Text.literal("⇄")) {
            dropdown.valueSet = HashSet<FishingLoot>().apply { addAll(option.defaultValue()) }
            it.active = false
        }.apply {
            margins(Insets.right(10))
            positioning(Positioning.relative(100, 50))
        }

    init {
        titleLayout.apply {
            horizontalSizing(Sizing.fill(100))
            verticalSizing(Sizing.fixed(30))
            verticalAlignment(VerticalAlignment.CENTER)
            children(
                listOf(
                    titleLabel(),
                    resetButton,
                    SearchAnchorComponent(
                        this,
                        option.key(),
                        { I18n.translate(option.translationKey()) })
                )
            )
        }

        collapsibleChildren.add(dropdown)

        onToggled().subscribe {
            if (it) {
                dropdown.valueSet = HashSet<FishingLoot>().apply { addAll(option.value()) }
                focusHandler()?.focus(dropdown.children().last(), FocusSource.MOUSE_CLICK)
            } else {
                resetButton.active =
                    !option.detached() && option.defaultValue().containsAll(valueSet)
            }
        }
    }

    private fun titleLabel(): LabelComponent = Components.label(
        Text.translatable("text.owo.config.list.add_entry").formatted(Formatting.GRAY)
    ).configure {
        it.apply {
            if (option.detached()) return@apply

            cursorStyle(CursorStyle.HAND)

            mouseEnter().subscribe { setTextColor(Formatting.YELLOW) }
            mouseLeave().subscribe { setTextColor(Formatting.GRAY) }
            mouseDown().subscribe { _, _, _ ->
                UISounds.playInteractionSound()
                toggleExpansion()
                true
            }
        }
    }

    override fun shouldDrawTooltip(mouseX: Double, mouseY: Double) =
        mouseY - y <= titleLayout.height() && super.shouldDrawTooltip(mouseX, mouseY)

    override fun isValid() = true

    override fun parsedValue(): Set<FishingLoot> = valueSet

    companion object {
        private fun LabelComponent.setTextColor(f: Formatting) =
            text(text().copy().styled { it.withColor(f) })
    }
}
