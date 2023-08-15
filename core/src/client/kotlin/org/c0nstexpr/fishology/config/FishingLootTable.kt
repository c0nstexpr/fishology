package org.c0nstexpr.fishology.config

import net.minecraft.item.BookItem
import net.minecraft.item.FishingRodItem
import net.minecraft.item.Item
import net.minecraft.item.NameTagItem
import net.minecraft.item.SaddleItem
import kotlin.reflect.KClass

class FishingLootTable<T : Item>(val lootType: FishingLootType, val itemType: KClass<T>) {
    companion object {
        val List = listOf(
            FishingLootTable(FishingLootType.Treasure, NameTagItem::class),
            FishingLootTable(FishingLootType.Treasure, SaddleItem::class),
            FishingLootTable(FishingLootType.Treasure, FishingRodItem::class),
            FishingLootTable(FishingLootType.Treasure, BookItem::class),
        )
    }
}

fun foo() {
}
