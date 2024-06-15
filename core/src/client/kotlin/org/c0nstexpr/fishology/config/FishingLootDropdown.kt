package org.c0nstexpr.fishology.config

import io.wispforest.owo.ui.component.DropdownComponent
import io.wispforest.owo.ui.core.Insets
import io.wispforest.owo.ui.core.Sizing
import net.minecraft.text.Text
import org.c0nstexpr.fishology.CORE_MOD_ID
import java.util.function.Consumer
import kotlin.enums.EnumEntries

class FishingLootDropdown :
    DropdownComponent(Sizing.fill(100)) {
    var valueSet: MutableSet<FishingLoot> = HashSet()
        set(value) {
            entries.children().mapNotNull { it as? FishingLootCheckBox }
                .forEach { it.mutableState = value.contains(it.loot) }
            field = value
        }

    init {
        FishingLoot.entries.apply {
            addLoots(FishingLootType.Treasure)
            addLoots(FishingLootType.Fish)
            addLoots(FishingLootType.Junk)
        }
    }

    private fun EnumEntries<FishingLoot>.addLoots(lootType: FishingLootType) {
        text(Text.translatable("$CORE_MOD_ID.${FishingLootType::class.simpleName}.${lootType.name}"))
        filter { it.lootType == lootType }.forEach(::addValues)
    }

    private fun addValues(it: FishingLoot) =
        entries.child(
            FishingLootCheckBox(this) { selected ->
                if (selected) {
                    valueSet.add(it)
                } else {
                    valueSet.remove(it)
                }
            }.apply {
                loot = it
                margins(Insets.of(2))
            },
        )

    companion object {
        private class FishingLootCheckBox(
            dropdown: FishingLootDropdown,
            onClick: Consumer<Boolean>,
        ) : Checkbox(dropdown, Text.empty(), false, onClick) {
            var loot = FishingLoot.Unknown
                set(value) {
                    text = value.translate()
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
