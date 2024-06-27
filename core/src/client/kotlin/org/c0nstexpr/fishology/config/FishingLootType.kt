package org.c0nstexpr.fishology.config

import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.c0nstexpr.fishology.CORE_MOD_ID

enum class FishingLootType {
    Treasure,
    Fish,
    Junk;

    fun translate(): MutableText =
        Text.translatable("$CORE_MOD_ID.${FishingLootType::class.simpleName}.$name")
}
