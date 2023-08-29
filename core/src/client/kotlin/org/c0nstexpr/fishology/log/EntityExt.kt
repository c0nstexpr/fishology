package org.c0nstexpr.fishology.log

import net.minecraft.entity.Entity

val Entity.inClient get() = world.isClient
