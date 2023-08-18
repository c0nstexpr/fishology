package org.c0nstexpr.fishology.config

import io.wispforest.owo.ui.component.DropdownComponent
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.client.resource.language.I18n
import net.minecraft.text.Text
import java.util.function.Consumer
import java.util.function.Supplier

class FishingLootDropdown(val getTranslationKey: Supplier<String>) :
    DropdownComponent(Sizing.fill(100)) {
    var valueSet: MutableSet<FishingLoot> = HashSet()
        set(value) {
            children().mapNotNull { it as? FishingLootCheckBox }
                .forEach { it.mutableState = value.contains(it.loot) }
            field = value
        }

    fun translate(loot: FishingLoot): Text {
        val key = "${getTranslationKey.get()}.value.${loot.name}"
        return if (I18n.hasTranslation(key)) Text.translatable(key)
        else loot.item.name
    }

    init {
        FishingLoot.entries.apply {
            filter { it.lootType == FishingLootType.Treasure }.forEach(::addValues)
            divider()
            filter { it.lootType == FishingLootType.Fish }.forEach(::addValues)
            divider()
            filter { it.lootType == FishingLootType.Junk }.forEach(::addValues)
        }
    }

    private fun addValues(it: FishingLoot) = entries.child(
        FishingLootCheckBox(this) { selected ->
            if (selected) {
                valueSet.add(it)
            } else {
                valueSet.remove(it)
            }
        }.apply {
            loot = it
            margins(Insets.of(2))
        })

    companion object {
        private class FishingLootCheckBox(
            private val dropdown: FishingLootDropdown,
            onClick: Consumer<Boolean>
        ) : Checkbox(dropdown, Text.empty(), false, onClick) {
            var loot = FishingLoot.Unknown
                set(value) {
                    text = dropdown.translate(value)
                    field = value
                }

            var mutableState
                get() = super.state
                set(value) {
                    super.state = value
                }
        }
    }
}
