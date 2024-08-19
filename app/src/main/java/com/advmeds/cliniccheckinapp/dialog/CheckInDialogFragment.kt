package com.advmeds.cliniccheckinapp.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.ContextCompat
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.CheckInDialogFragmentBinding
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo

class CheckInDialogFragment : AppCompatDialogFragment() {
    private var _binding: CheckInDialogFragmentBinding? = null

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

        _binding = CheckInDialogFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferencesRepo = SharedPreferencesRepo(requireContext().applicationContext)

        val healthCard = getString(R.string.health_card)
        val nationId = sharedPreferencesRepo.formatCheckedList.joinToString("、") { getString(it.description) }
        val text = String.format(
            getString(R.string.make_appointment_message),
            healthCard,
            nationId
        )
        val spannable = SpannableString(text)

        val textColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorPrimary
        )

        text.indexOf(healthCard).also { textStart ->
            val textEnd = textStart + healthCard.length
            spannable.setSpan(
                ForegroundColorSpan(textColor),
                textStart,
                textEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        text.indexOf(nationId).also { textStart ->
            val textEnd = textStart + nationId.length
            spannable.setSpan(
                ForegroundColorSpan(textColor),
                textStart,
                textEnd,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        binding.fragmentMessageTv.text = spannable

        binding.gotItButton.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}