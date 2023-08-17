package org.c0nstexpr.fishology.config

import io.wispforest.owo.config.Option
import io.wispforest.owo.config.ui.component.ConfigTextBox
import io.wispforest.owo.config.ui.component.OptionValueProvider
import io.wispforest.owo.config.ui.component.SearchAnchorComponent
import io.wispforest.owo.ops.TextOps
import io.wispforest.owo.ui.component.ButtonComponent
import io.wispforest.owo.ui.component.Components
import io.wispforest.owo.ui.component.DropdownComponent
import io.wispforest.owo.ui.component.LabelComponent
import io.wispforest.owo.ui.component.TextBoxComponent
import io.wispforest.owo.ui.container.Containers
import io.wispforest.owo.ui.container.FlowLayout
import io.wispforest.owo.ui.core.Component.FocusSource
import io.wispforest.owo.ui.core.CursorStyle
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.ParentComponent
import io.wispforest.owo.ui.core.Positioning
import io.wispforest.owo.ui.core.Sizing
import io.wispforest.owo.ui.core.VerticalAlignment
import io.wispforest.owo.ui.event.MouseDown
import io.wispforest.owo.ui.event.MouseEnter
import io.wispforest.owo.ui.event.MouseLeave
import io.wispforest.owo.ui.util.UISounds
import io.wispforest.owo.util.NumberReflection
import io.wispforest.owo.util.ReflectionUtils
import net.minecraft.client.gui.widget.ButtonWidget
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import java.util.*
import java.util.function.Consumer
import java.util.stream.Collectors

class FishingLootDropdown<T : Enum<T>>(option: Option<Set<T>>) :
    DropdownComponent(Sizing.fill(100)), OptionValueProvider {
    private val backingOption = option
    private val backingList: MutableSet<T> = HashSet(option.value())

    val resetButton: ButtonWidget =
        Components.button(Text.literal("⇄")) { button: ButtonComponent ->
            backingList.clear()
            backingList.addAll(option.defaultValue())
            refreshOptions()
            button.active = false
        }.apply {
            margins(Insets.right(10))
            positioning(Positioning.relative(100, 50))
        }

    val titleLayout: FlowLayout =
        Containers.horizontalFlow(Sizing.content(), Sizing.content()).apply {
            padding(Insets.of(5, 5, 5, 0))
            horizontalSizing(Sizing.fill(100))
            verticalSizing(Sizing.fixed(30))
            verticalAlignment(VerticalAlignment.CENTER)
            if (!option.detached()) {
                child(
                    Components.label(
                        Text.translatable("text.owo.config.list.add_entry")
                            .formatted(Formatting.GRAY)
                    ).configure<LabelComponent> { label: LabelComponent ->
                        label.apply {
                            cursorStyle(CursorStyle.HAND)
                            mouseEnter().subscribe {
                                text(
                                    text().copy().styled { style: Style ->
                                        style.withColor(Formatting.YELLOW)
                                    }
                                )
                            }
                            mouseLeave().subscribe {
                                text(
                                    text().copy().styled { style: Style ->
                                        style.withColor(Formatting.GRAY)
                                    }
                                )
                            }
                            mouseDown().subscribe { _, _, _: Int ->
                                UISounds.playInteractionSound()

                                if (!this.expanded) this.toggleExpansion()

                                refreshOptions()

                                val lastEntry =
                                    this.collapsibleChildren.get(this.collapsibleChildren.size - 1) as ParentComponent

                                focusHandler()?.focus(
                                    lastEntry.children()[lastEntry.children().size - 1],
                                    FocusSource.MOUSE_CLICK
                                )
                                true
                            }
                        }
                    })
            }

            child(resetButton)
            child(SearchAnchorComponent(
                this,
                option.key(),
                {
                    I18n.translate(
                        "text.config.${option.configName()}.option.${option.key().asString()}"
                    )
                },
                {
                    backingList.stream().map { o: T? -> o.toString() }.collect(Collectors.joining())
                }
            ))
        }

    init {
        val titleText = Text.translatable(
            "text.config." + option.configName() + ".option." + option.key().asString()
        )

        padding(padding.get().add(0, 5, 0, 0))

        refreshResetButton()
        refreshOptions()
    }

    fun refreshOptions() {
        collapsibleChildren.clear()
        val listType = ReflectionUtils.getTypeArgument(
            backingOption.backingField().field().getGenericType(), 0
        )
        for (i in backingList.indices) {
            val container = Containers.horizontalFlow(Sizing.fill(100), Sizing.content())
            container.verticalAlignment(VerticalAlignment.CENTER)
            val optionIndex: Int = i
            val label = Components.label(TextOps.withFormatting("- ", Formatting.GRAY))
            label.margins(Insets.left(10))
            if (!backingOption.detached()) {
                label.cursorStyle(CursorStyle.HAND)
                label.mouseEnter().subscribe(MouseEnter {
                    label.text(
                        TextOps.withFormatting(
                            "x ",
                            Formatting.GRAY
                        )
                    )
                })
                label.mouseLeave().subscribe(MouseLeave {
                    label.text(
                        TextOps.withFormatting(
                            "- ",
                            Formatting.GRAY
                        )
                    )
                })
                label.mouseDown()
                    .subscribe(MouseDown { mouseX: Double, mouseY: Double, button: Int ->
                        backingList.removeAt(optionIndex)
                        refreshResetButton()
                        refreshOptions()
                        UISounds.playInteractionSound()
                        true
                    })
            }
            container.child(label)
            val box = ConfigTextBox()
            box.text = backingList.get(i).toString()
            box.setCursorToStart()
            box.setDrawsBackground(false)
            box.margins(Insets.vertical(2))
            box.horizontalSizing(Sizing.fill(95))
            box.verticalSizing(Sizing.fixed(8))
            if (!backingOption.detached()) {
                box.onChanged().subscribe(TextBoxComponent.OnChanged { s: String? ->
                    if (!box.isValid) return@subscribe
                    backingList.set(optionIndex, box.parsedValue() as T)
                    refreshResetButton()
                })
            } else {
                box.active = false
            }
            if (NumberReflection.isNumberType(listType)) {
                box.configureForNumber(listType as Class<out Number?>?)
            }
            container.child(box)
            this.collapsibleChildren.add(container)
        }
        this.contentLayout.configure<FlowLayout>(Consumer { layout: FlowLayout ->
            layout.clearChildren()
            if (this.expanded) layout.children(this.collapsibleChildren)
        })
        refreshResetButton()
    }

    fun refreshResetButton() {
        resetButton.active =
            !backingOption.detached() && backingList != backingOption.defaultValue()
    }

    override fun shouldDrawTooltip(mouseX: Double, mouseY: Double) =
        mouseY - y <= titleLayout.height() && super.shouldDrawTooltip(
            mouseX,
            mouseY
        )

    override fun isValid() = true

    override fun parsedValue() = backingList
}
