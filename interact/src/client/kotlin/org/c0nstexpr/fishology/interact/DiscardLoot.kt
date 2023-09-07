package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.subject.publish.PublishSubject
import net.minecraft.entity.ItemEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import org.c0nstexpr.fishology.config.FishingLoot.Companion.getLoot
import org.c0nstexpr.fishology.events.SlotUpdateEvent
import org.c0nstexpr.fishology.logger
import org.c0nstexpr.fishology.modId
import org.c0nstexpr.fishology.msg
import org.c0nstexpr.fishology.utils.SwitchDisposable
import org.c0nstexpr.fishology.utils.isSame
import org.c0nstexpr.fishology.utils.observableStep

class DiscardLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    var lootsFilter: Set<org.c0nstexpr.fishology.config.FishingLoot> = setOf(),
) : SwitchDisposable() {
    private var notified = false

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        notified = false

        val pickSub = PublishSubject<FishingLoot>()
        val dropCancelSub = PublishSubject<Unit?>()

        return disposableScope {
            observableStep(pickSub) {
                if (!pick(rod.client.interactionManager, rod.rodItem) { rodSelectedNotify() }) {
                    dropCancelSub.onNext(null)
                }
            }
                .concatMaybe({ dropMaybe(dropCancelSub) })
                .subscribeScoped { }

            observableStep(
                caught.filter { lootsFilter.contains(it.stack.getLoot()) }.map { it.stack.copy() },
            )
                .switchMaybe(
                    {
                        SlotUpdateEvent.observable.mapNotNull { mapSlopUpdate(it) }
                            .firstOrComplete()
                    },
                ) {
                    logger.d("detected excluded loots")
                    pickSub.onNext(this)
                }
                .tryOn()
                .subscribeScoped { }
        }
    }

    private fun rodSelectedNotify() {
        if (!notified) {
            rod.client.msg(Text.translatable("$modId.discard_loots_notification"))
            notified = true
        }
    }

    private fun ItemStack.mapSlopUpdate(arg: SlotUpdateEvent.Arg): FishingLoot? {
        val player = rod.player ?: run {
            logger.w("client player is null")
            return null
        }

        return if (arg.stack.isSame(this) && arg.syncId == player.playerScreenHandler
                .syncId
        ) {
            FishingLoot(
                player,
                player.playerScreenHandler.getSlot(arg.slot).index,
                this,
            )
        } else {
            logger.w("client player is null")
            null
        }
    }
}
