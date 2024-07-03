package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.observable.subscribe
import com.badoo.reaktive.observable.switchMap
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import org.c0nstexpr.fishology.MOD_ID
import org.c0nstexpr.fishology.events.ClientTickEvent
import org.c0nstexpr.fishology.events.SetFishHookEvent
import org.c0nstexpr.fishology.log.d
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.utils.SwitchDisposable

class BobberStatus(val client: MinecraftClient) : SwitchDisposable() {
    override fun onEnable(): Disposable {
        var isOpenWater = false

        return SetFishHookEvent.observable.mapNotNull { it.bobber }
            .switchMap { bobber ->
                ClientTickEvent.observable.map { bobber.isOpenOrWaterAround(bobber.blockPos) }
                    .filter { it != isOpenWater }
            }
            .subscribe {
                val txt = if (it) InOpenWater else NotInOpenWater

                isOpenWater = it
                logger.d<BobberStatus> { txt.string }
                client.inGameHud.setOverlayMessage(txt, true)
            }
    }

    companion object {
        private val InOpenWater = Text.translatable("$MOD_ID.bobber_in_open_water")
            .apply { style = Style.EMPTY.withColor(Formatting.AQUA) }

        private val NotInOpenWater = Text.translatable("$MOD_ID.bobber_not_in_open_water")
            .apply { style = Style.EMPTY.withColor(Formatting.YELLOW) }
    }
}
