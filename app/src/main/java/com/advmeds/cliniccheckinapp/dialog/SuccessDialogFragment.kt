package com.advmeds.cliniccheckinapp.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.lifecycle.lifecycleScope
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.SuccessDialogFragmentBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SuccessDialogFragment(
    private val title: CharSequence,
    private val message: CharSequence,
) : AppCompatDialogFragment() {

    private var _binding: SuccessDialogFragmentBinding? = null

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
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.argb(
                    (255 * 0.2).toInt(),
                    0,
                    0,
                    0
                )
            )
        )

        _binding = SuccessDialogFragmentBinding.inflate(inflater, container, false)

        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                delay(10000)
            }

            dismiss()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setOnClickListener {
            dismiss()
        }
        binding.fragmentTitleTv.visibility = if (title.isNotBlank()) View.VISIBLE else View.GONE
        binding.fragmentTitleTv.text = title
        binding.fragmentMessageTv.visibility = if (message.isNotBlank()) View.VISIBLE else View.GONE
        binding.fragmentMessageTv.text = message
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}