package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.toMutableText

class CaughtChat(
    client: MinecraftClient,
    private val caught: Observable<ItemEntity>,
) : ChatInteraction(client, "caught_on_chat") {
    var lootsFilter = setOf<FishingLoot>()
        set(value) {
            field = value
            logger.d<CaughtChat> { "Change chat on caught loot filter" }
        }

    private fun getCaughtItemTxt(stack: ItemStack, name: Text): MutableText {
        val txt = name.toMutableText()
        val enchantTxt = EnchantmentHelper.get(stack)
            .map { (enchantment, level) -> enchantment.getName(level).string }
            .takeIf { it.isNotEmpty() }
            ?: return txt

        txt.append(
            enchantTxt.joinToString(
                Text.translatable("$modId.comma").string,
                Text.translatable("$modId.left_brace").string,
                Text.translatable("$modId.right_brace").string,
            ),
        )

        return txt
    }

    override fun onEnable(): Disposable {
        logger.d<CaughtChat> { "enable caught chat interaction" }
        return caught.map { Pair(it.stack, it.stack.getLoot()) }
            .filter { lootsFilter.contains(it.second) }
            .tryOn()
            .subscribe { notify(getCaughtItemTxt(it.first, it.second.translate())) }
    }
}
