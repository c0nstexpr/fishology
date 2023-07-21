package org.c0nstexpr.fishology.interact

import net.minecraft.entity.Entity
import net.minecraft.text.Text

class HookedInteraction {
    var entity: Entity? = null
        private set

    fun getItemName(): Text? = entity?.displayName
}
