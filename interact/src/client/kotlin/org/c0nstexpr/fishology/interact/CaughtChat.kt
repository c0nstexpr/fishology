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
import net.minecraft.text.Text
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.utils.SwitchDisposable

class CaughtChat(
    private val client: MinecraftClient,
    private val caught: Observable<ItemEntity>,
) : SwitchDisposable() {
    var lootsFilter = setOf<FishingLoot>()

    private fun onCaughtChat(stack: ItemStack, name: Text) {
        val txt = Text.translatable("$modId.${"caught_on_chat"}")
            .append(name)

        EnchantmentHelper.get(stack).apply {
            if (isNotEmpty()) {
                txt.append(
                    map { (enchantment, level) -> enchantment.getName(level).string }
                        .joinToString(
                            Text.translatable("$modId.comma").string,
                            Text.translatable("$modId.left_brace").string,
                            Text.translatable("$modId.right_brace").string,
                        ),
                )
            }
        }

        client.chat(txt.string, logger)
    }

    override fun onEnable(): Disposable {
        logger.d("enable caught chat interaction")
        return caught.map { Pair(it.stack, it.stack.getLoot()) }
            .filter { lootsFilter.contains(it.second) }
            .subscribe { onCaughtChat(it.first, it.second.translate()) }
    }
}
