package com.advmeds.cliniccheckinapp.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.compose.ui.platform.ComposeView
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.CheckingDialogFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.screen.CheckingDialogFragmentScreen

class CheckingDialogFragment : AppCompatDialogFragment() {

    private lateinit var composeView: ComposeView
    private var _binding: CheckingDialogFragmentBinding? = null

    /** This property is only valid between onCreateView and onDestroyView. */
    private val binding get() = _binding!!

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
        dialog?.setOnKeyListener { _, keyCode, _ ->
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                true
            } else {
                true
            }
        }

        _binding = CheckingDialogFragmentBinding.inflate(inflater, container, false)

        //return binding.root
        return ComposeView(requireContext()).also {
            composeView = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        composeView.setContent {
            CheckingDialogFragmentScreen()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}