package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import org.c0nstexpr.fishology.chat
import org.c0nstexpr.fishology.config.FishingLoot
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.config.NotifyLevel
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable

class CaughtChat(
    private val client: MinecraftClient,
    private val caught: Observable<ItemEntity>,
) : SwitchDisposable() {
    var notifyLevel = NotifyLevel.None

    var lootsFilter = setOf<FishingLoot>()
    var fmt = ""
        set(value) {
            field = value.validateMsgFormat()
        }

    private fun String.validateMsgFormat(): String {
        try {
            format("test")
        } catch (e: Exception) {
            logger.d("invalid msg format: ${e.message}")
            return defaultMsg.string
        }

        return this
    }

    private fun getCaughtItemTxt(stack: ItemStack, name: Text): MutableText {
        val txt = Text.empty().append(name)

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

        return txt
    }

    private fun onCaughtChat(stack: ItemStack, name: Text) = when (notifyLevel) {
        NotifyLevel.HUD -> {
            { client.msg(defaultMsg.append(getCaughtItemTxt(stack, name))) }
        }

        NotifyLevel.Chat -> {
            { client.chat(fmt.format(getCaughtItemTxt(stack, name).string), logger) }
        }

        else -> null
    }

    override fun onEnable(): Disposable {
        logger.d("enable caught chat interaction")
        return caught.map { Pair(it.stack, it.stack.getLoot()) }
            .filter { lootsFilter.contains(it.second) }
            .mapNotNull { onCaughtChat(it.first, it.second.translate()) }
            .tryOn()
            .subscribe { it() }
    }

    companion object {
        private val defaultMsg get() = Text.translatable("$modId.${"caught_on_chat"}")
    }
}
