package org.c0nstexpr.fishology

import net.minecraft.text.MutableText
import net.minecraft.text.Style
import net.minecraft.text.Text
import net.minecraft.util.Formatting

fun Text.toMutableText(): MutableText = this as? MutableText ?: Text.empty().append(this)

fun styledText(
    style: Style,
    vararg txt: Text,
): MutableText = Text.empty().setStyle(style).apply { txt.forEach(::append) }

fun styledText(
    style: Style,
    vararg txt: String,
): MutableText = Text.empty().setStyle(style).apply { txt.forEach(::append) }

fun coloredText(
    color: Formatting,
    vararg txt: Text,
) = styledText(Style.EMPTY.withColor(color), *txt)

fun coloredText(
    color: Formatting,
    vararg txt: String,
) = styledText(Style.EMPTY.withColor(color), *txt)

fun MutableText.appendTranslatable(
    key: String,
    vararg args: Any,
): MutableText = append(Text.translatable(key, *args))

fun MutableText.appendStyled(
    style: Style,
    vararg txt: Text,
): MutableText = append(styledText(style, *txt))

fun MutableText.appendStyled(
    style: Style,
    vararg txt: String,
): MutableText = append(styledText(style, *txt))
