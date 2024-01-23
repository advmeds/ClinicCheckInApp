package com.advmeds.cliniccheckinapp.utils

import android.content.Context
import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import kotlinx.serialization.Serializable

@Serializable
sealed class NativeText {
    @Serializable
    data class Simple(val text: String) : NativeText()
    @Serializable
    data class Resource(@StringRes val id: Int) : NativeText()
    @Serializable
    data class Plural(@PluralsRes val id: Int, val number: Int, val args: List<String>) : NativeText()
    @Serializable
    data class Arguments(@StringRes val id: Int, val args: List<String>) : NativeText()
    @Serializable
    data class Multi(val text: List<NativeText>) : NativeText()
    @Serializable
    data class ArgumentsMulti(@StringRes val id: Int, val text: List<NativeText>) : NativeText()
}

fun NativeText.toCharSequence(context: Context): CharSequence {
    return when(this) {
        is NativeText.Arguments -> context.getString(id, *args.toTypedArray())
        is NativeText.Multi -> {
            val builder = StringBuilder()
            for (t in text) {
                builder.append(t.toCharSequence(context))
            }
            builder.toString()
        }
        is NativeText.Plural -> context.resources.getQuantityString(id, number, *args.toTypedArray())
        is NativeText.Resource -> context.getString(id)
        is NativeText.Simple -> text
        is NativeText.ArgumentsMulti -> {
            val arg = text.joinToString("、") {
                it.toCharSequence(context)
            }
            context.getString(id, arg)
        }
    }
}
