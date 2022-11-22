package com.advmeds.cliniccheckinapp.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.ui.platform.ComposeView
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.ErrorDialogFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.screen.ErrorDialogFragmentScreen

class ErrorDialogFragment(
    private val message: CharSequence
) : AppCompatDialogFragment() {

    private lateinit var composeView: ComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(
            STYLE_NORMAL,
            R.style.MyApp_DialogFragmentTheme
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.argb((255 * 0.5).toInt(), 0 , 0, 0)))

        return ComposeView(requireContext()).also {
            composeView = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        composeView.setContent {
            ErrorDialogFragmentScreen(
                message = message.toString(),
                closeDialog = {
                    dismiss()
                }
            )
        }
    }
}