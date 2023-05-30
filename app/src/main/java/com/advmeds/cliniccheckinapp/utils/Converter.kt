package com.advmeds.cliniccheckinapp.utils

import android.content.Context
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R

object Converter {

    fun language_lang_code_to_name(
        context: Context,
        langCode: String = BuildConfig.DEFAULT_LANGUAGE
    ): String {
        return when(langCode) {
            "en" -> context.getString(R.string.en_language)
            "zh" -> context.getString(R.string.zh_tw_language)
            else -> throw IllegalArgumentException("The language code has no similar languages")
        }
    }

    fun language_name_to_lang_code(context: Context, name: String) : String {
        return when(name) {
            context.getString(R.string.en_language) -> "en"
            context.getString(R.string.zh_tw_language) -> "zh"
            else -> throw IllegalArgumentException("The language has no similar language codes")
        }
    }
}