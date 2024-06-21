package org.c0nstexpr.fishology.config

import net.minecraft.client.resource.language.I18n
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.BowItem
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.FishingRodItem
import net.minecraft.item.InkSacItem
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.item.NameTagItem
import net.minecraft.item.PotionItem
import net.minecraft.item.SaddleItem
import net.minecraft.text.Text
import org.c0nstexpr.fishology.CORE_MOD_ID

enum class FishingLoot(val lootType: FishingLootType, val item: Item) {
    NameTag(FishingLootType.Treasure, Items.NAME_TAG),
    Bow(
        FishingLootType.Treasure,
        Items.BOW,
    ),
    Saddle(FishingLootType.Treasure, Items.SADDLE),
    EnchantedFishingRod(
        FishingLootType.Treasure,
        Items.FISHING_ROD,
    ),
    Book(FishingLootType.Treasure, Items.ENCHANTED_BOOK),
    NautilusShell(
        FishingLootType.Treasure,
        Items.NAUTILUS_SHELL,
    ),

    Cod(FishingLootType.Fish, Items.COD),
    Salmon(FishingLootType.Fish, Items.SALMON),
    Tropical(
        FishingLootType.Fish,
        Items.TROPICAL_FISH,
    ),
    Puffer(FishingLootType.Fish, Items.PUFFERFISH),

    LilyPad(FishingLootType.Junk, Items.LILY_PAD),
    LeatherBoots(
        FishingLootType.Junk,
        Items.LEATHER_BOOTS,
    ),
    Leather(FishingLootType.Junk, Items.LEATHER),
    Bone(FishingLootType.Junk, Items.BONE),
    Potion(
        FishingLootType.Junk,
        Items.POTION,
    ),
    String(FishingLootType.Junk, Items.STRING),
    FishingRod(
        FishingLootType.Junk,
        Items.FISHING_ROD,
    ),
    Bowl(FishingLootType.Junk, Items.BOWL),
    Stick(FishingLootType.Junk, Items.STICK),
    InkSac(
        FishingLootType.Junk,
        Items.INK_SAC,
    ),
    TripwireHook(FishingLootType.Junk, Items.TRIPWIRE_HOOK),
    RottenFlesh(
        FishingLootType.Junk,
        Items.ROTTEN_FLESH,
    ),
    Bamboo(FishingLootType.Junk, Items.BAMBOO),
    Unknown(FishingLootType.Junk, Items.AIR), ;

    companion object {
        fun ItemStack.getLoot() = when (item) {
            is NameTagItem -> NameTag
            is BowItem -> Bow
            is SaddleItem -> Saddle
            is FishingRodItem ->
                if (EnchantmentHelper.hasEnchantments(this)) {
                    FishingRod
                } else {
                    EnchantedFishingRod
                }

            is EnchantedBookItem -> Book
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

    fun translate(): Text {
        val key = "$CORE_MOD_ID.${FishingLoot::class.simpleName}.$name"
        return if (I18n.hasTranslation(key)) Text.translatable(key) else item.name
    }
}
