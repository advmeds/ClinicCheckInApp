package com.advmeds.cliniccheckinapp.ui

import com.advmeds.cliniccheckinapp.ui.presentations.WebPresentation

class PanelActivity : CommonActivity() {
    override fun showPresentation() {
        sharedPresentation = WebPresentation.newInstance(this)?.also {
            it.show()
        }
    }
}