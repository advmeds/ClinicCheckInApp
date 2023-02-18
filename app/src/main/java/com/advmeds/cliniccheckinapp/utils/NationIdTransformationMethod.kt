package com.advmeds.cliniccheckinapp.utils

import android.text.method.PasswordTransformationMethod
import android.view.View

class NationIdTransformationMethod : PasswordTransformationMethod() {
    override fun getTransformation(source: CharSequence?, view: View?): CharSequence {
        return source?.let { NationIdCharSequence(it) } ?: super.getTransformation(source, view)
    }

    class NationIdCharSequence(private val source: CharSequence) : CharSequence {
        override val length: Int
            get() = source.length

        override fun get(index: Int): Char = if (2 < index && index < length - 3) {
            '*'
        } else {
            source[index]
        }

        override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = source.subSequence(startIndex, endIndex)
    }
}