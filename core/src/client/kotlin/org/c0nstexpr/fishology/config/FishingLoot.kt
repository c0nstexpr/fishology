package org.c0nstexpr.fishology.config

import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.BookItem
import net.minecraft.item.FishingRodItem
import net.minecraft.item.InkSacItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.NameTagItem
import net.minecraft.item.PotionItem
import net.minecraft.item.SaddleItem

enum class FishingLoot(val lootType: FishingLootType) {
    Unknown(FishingLootType.Junk),

    NameTag(FishingLootType.Treasure),
    Bow(FishingLootType.Treasure),
    Saddle(FishingLootType.Treasure),
    EnchantedFishingRod(FishingLootType.Treasure),
    Book(FishingLootType.Treasure),
    NautilusShell(FishingLootType.Treasure),

    Cod(FishingLootType.Fish),
    Salmon(FishingLootType.Fish),
    Tropical(FishingLootType.Fish),
    Puffer(FishingLootType.Fish),

    LilyPad(FishingLootType.Junk),
    LeatherBoots(FishingLootType.Junk),
    Leather(FishingLootType.Junk),
    Bone(FishingLootType.Junk),
    Potion(FishingLootType.Junk),
    String(FishingLootType.Junk),
    FishingRod(FishingLootType.Junk),
    Bowl(FishingLootType.Junk),
    Stick(FishingLootType.Junk),
    InkSac(FishingLootType.Junk),
    TripwireHook(FishingLootType.Junk),
    RottenFlesh(FishingLootType.Junk),
    Bamboo(FishingLootType.Junk),
    ;

    companion object {
        fun ItemStack.getLoot() = when (item) {
            is NameTagItem -> NameTag
            Items.BOW -> Bow
            is SaddleItem -> Saddle
            is FishingRodItem -> if (EnchantmentHelper.get(this).isEmpty()) {
                FishingRod
            } else {
                EnchantedFishingRod
            }
            is BookItem -> Book
            Items.NAUTILUS_SHELL -> NautilusShell
            Items.COD -> Cod
            Items.SALMON -> Salmon
            Items.TROPICAL_FISH -> Tropical
            Items.PUFFERFISH -> Puffer
            Items.LILY_PAD -> LilyPad
            Items.LEATHER_BOOTS -> LeatherBoots
            Items.LEATHER -> Leather
            Items.BONE -> Bone
            is PotionItem -> Potion
            Items.STRING -> String
            Items.BOWL -> Bowl
            Items.STICK -> Stick
            is InkSacItem -> InkSac
            Items.TRIPWIRE_HOOK -> TripwireHook
            Items.ROTTEN_FLESH -> RottenFlesh
            Items.BAMBOO -> Bamboo
            else -> Unknown
        }
    }
}
