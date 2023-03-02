package com.advmeds.cliniccheckinapp.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.ManualInputFragmentBinding
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.utils.NationIdTransformationMethod

class ManualInputFragment : Fragment() {

    private val viewModel: ManualInputViewModel by viewModels()

    private var _binding: ManualInputFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val reloadClinicLogoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clinicLogoUrl = intent?.getStringExtra(HomeFragment.CLINIC_LOGO_URL_KEY)
            binding.logoImageView.load(clinicLogoUrl)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ManualInputFragmentBinding.inflate(inflater, container, false)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            reloadClinicLogoReceiver,
            IntentFilter(HomeFragment.RELOAD_CLINIC_LOGO_ACTION)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.logoImageView.load(viewModel.logoUrl)

        binding.dismissButton.setOnClickListener {
            findNavController().navigateUp()
        }

        val arg = getString(R.string.national_id)
        val text = String.format(getString(R.string.national_id_input_title), arg)
        val textColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorPrimary
        )
        val textStart = text.indexOf(arg)
        val textEnd = textStart + arg.length
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            textStart,
            textEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.idInputTitleTv.text = spannable
        binding.idInputEt.hint = String.format(getString(R.string.national_id_input_hint), arg)
        binding.idInputEt.transformationMethod = NationIdTransformationMethod()

        setupKeyboard()
    }

    private fun setupKeyboard() {
        val onKeyClicked = View.OnClickListener {
            val currentText = binding.idInputEt.text.toString()
            val key = (it as Button).text.toString()

            binding.idInputEt.setText(currentText + key)
        }

        binding.enPadLayout.children.forEach { children ->
            when (children) {
                is ViewGroup -> {
                    children.children.forEach {
                        if (it is Button) {
                            it.setOnClickListener(onKeyClicked)
                        }
                    }
                }
                is Button -> {
                    children.setOnClickListener(onKeyClicked)
                }
            }
        }

        binding.numberPadLayout.children.forEach { children ->
            when (children) {
                is ViewGroup -> {
                    children.children.forEach {
                        if (it is Button) {
                            it.setOnClickListener(onKeyClicked)
                        }
                    }
                }
                is Button -> {
                    children.setOnClickListener(onKeyClicked)
                }
            }
        }

        binding.backspaceButton.setOnClickListener {
            val currentText = binding.idInputEt.text.toString()

            binding.idInputEt.setText(currentText.dropLast(1))
        }

        binding.enterButton.setOnClickListener {
            val patient = binding.idInputEt.text.toString().trim()
            (requireActivity() as MainActivity).getPatients(patient) {
                binding.idInputEt.text = null
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadClinicLogoReceiver)

        _binding = null
    }
}