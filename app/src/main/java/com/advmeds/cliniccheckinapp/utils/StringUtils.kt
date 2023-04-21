package com.advmeds.cliniccheckinapp.utils

import android.text.Editable
import java.util.regex.Pattern

/** 是否為台灣身分證或是居留證 */
val String.isNationId: Boolean
    get() {
        val regExp = "^[A-Z][A-Z\\d]\\d{8}$"
        val p = Pattern.compile(regExp)
        return p.matcher(this).matches()
    }

/** 是否為屏基病例號 */
val String.isPTCHCaseId: Boolean
    get() = true

fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)