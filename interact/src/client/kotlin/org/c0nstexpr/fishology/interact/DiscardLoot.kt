package org.c0nstexpr.fishology.interact

import com.badoo.reaktive.coroutinesinterop.maybeFromCoroutine
import com.badoo.reaktive.disposable.Disposable
import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.maybe.Maybe
import com.badoo.reaktive.maybe.subscribe
import com.badoo.reaktive.observable.Observable
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.firstOrComplete
import com.badoo.reaktive.observable.map
import com.badoo.reaktive.observable.mapNotNull
import com.badoo.reaktive.subject.publish.PublishSubject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.newCoroutineContext
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
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
import java.util.concurrent.Future
import kotlin.collections.ArrayDeque
import kotlin.coroutines.CoroutineContext

class DiscardLoot(
    private val rod: Rod,
    private val caught: Observable<ItemEntity>,
    var lootsFilter: Set<org.c0nstexpr.fishology.config.FishingLoot> = setOf(),
) : SwitchDisposable() {
    private var notified = false

    val dropMutex = Mutex()
    var currentDrop = Disposable().apply { dispose() }
    val dropQueue = ArrayDeque<Maybe<Unit>>(3)
    private val scope = CoroutineDispatcher()

    private val channel = Channel<Maybe<Unit>>(Channel.UNLIMITED)

    fun replaceCurrentDrop(maybe: Maybe<Unit>) {
        if (currentDrop.isDisposed) {
            currentDrop = maybe.subscribe {
                synchronized(dropMutex) {
                    currentDrop.dispose()
                    replaceCurrentDrop(dropQueue.removeFirst())
                }
            }
        } else {
            dropQueue.add(maybe)

        }
    }

    override fun onEnable(): Disposable {
        logger.d("enable throw loot interaction")

        notified = false

        Dispatchers.Main.newCoroutineContext(){

        }

        channel.receive()

        return disposableScope {
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
                    synchronized(dropMutex) { replaceCurrentDrop(dropMaybe()) }

                    runBlocking {
                        channel.send(dropMaybe())
                    }
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
