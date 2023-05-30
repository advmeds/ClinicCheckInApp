package com.advmeds.cliniccheckinapp.ui.fragments.home.model

class LanguageModel(val name: String, var isSelected: Boolean)

fun combineArrays(strings: Array<String>, booleans: List<Boolean>): Array<LanguageModel> {
    val combinedArray = mutableListOf<LanguageModel>()

    // Check if the arrays have the same length
    if (strings.size == booleans.size) {
        for (i in strings.indices) {
            val myClass = LanguageModel(strings[i], booleans[i])
            combinedArray.add(myClass)
        }
    } else {
        throw IllegalArgumentException("Arrays must have the same length")
    }

    return combinedArray.toTypedArray()
}