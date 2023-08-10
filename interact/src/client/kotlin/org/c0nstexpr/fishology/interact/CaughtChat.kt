package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.ItemEntity
import net.minecraft.text.Text
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.utils.SwitchDisposable

class CaughtChat(
    private val client: MinecraftClient,
    private val caught: Observable<ItemEntity>,
) : SwitchDisposable() {
    private fun onCaughtChat(it: ItemEntity) {
        val enchantText = EnchantmentHelper.get(it.stack)
            .map { (enchantment, level) -> enchantment.getName(level).string }.joinToString(
                Text.translatable("$modId.comma").string,
                Text.translatable("$modId.left_brace").string,
                Text.translatable("$modId.right_brace").string,
            )

        client.chat(
            Text.translatable("$modId.${"caught_on_chat"}")
                .append(it.displayName)
                .append(enchantText)
                .string,
            logger,
        )
    }

    override fun onEnable() = caught.subscribe { onCaughtChat(it) }
}
