package com.advmeds.cliniccheckinapp.utils

import androidx.appcompat.app.AlertDialog

private var _alert: AlertDialog? = null

/** 希望畫面上永遠只顯示一個AlertDialog */
fun AlertDialog.showOnly() {
    _alert?.run {
        dismiss()
    }

    _alert = this
    show()
}

/** @see AlertDialog.showOnly */
fun AlertDialog.Builder.showOnly(): AlertDialog {
    val dialog = create()
    dialog.showOnly()
    return dialog
}