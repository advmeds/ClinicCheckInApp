package com.advmeds.cliniccheckinapp.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.ui.inputPage.InputPageFragment


class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val reloadClinicLogoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clinicLogoUrl = intent?.getStringExtra(InputPageFragment.CLINIC_LOGO_URL_KEY)
            binding.logoImageView.load(clinicLogoUrl)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = HomeFragmentBinding.inflate(inflater, container, false)

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            reloadClinicLogoReceiver,
            IntentFilter(InputPageFragment.RELOAD_CLINIC_LOGO_ACTION)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {

//        binding.logoImageView.setOnLongClickListener {
//            AlertDialog.Builder(requireContext())
//                .setTitle(R.string.setting)
//                .setItems(R.array.setting_items) { _, index ->
//                    when (index) {
//                        0 -> {
//                            onSetServerDomainItemClicked()
//                        }
//                        1 -> {
//                            onSetOrgIDItemClicked()
//                        }
//                    }
//                }
//                .showOnly()
//
//            return@setOnLongClickListener true
//        }

        val cardInfoText = getString(R.string.home_page_card_info_label)
        val cardInfoArg = getString(R.string.home_page_card_info_label_arg)
        val cardInfoSpannable = setRedText(cardInfoText, cardInfoArg)

        val manuallyInputText = getString(R.string.home_page_manually_input_card_label)
        val manuallyInputArg = getString(R.string.home_page_manually_input_card_label_arg)
        val manuallyInputSpannable = setRedText(manuallyInputText, manuallyInputArg)

        val pcuCheckText = getString(R.string.home_page_pcu_check_label)
        val pcuCheckArg = getString(R.string.home_page_pcu_check_label_arg)
        val pcuCheckSpannable = setRedText(pcuCheckText, pcuCheckArg)

        binding.homePageCardInfoTitle.text = cardInfoSpannable
        binding.homePageManuallyInputTitle.text = manuallyInputSpannable
        binding.homePagePcuCheckTitle.text = pcuCheckSpannable

        binding.manuallyInputCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToInputPageFragment()
            findNavController().navigate(action)
        }
    }

    private fun setRedText(rawText: String, arg: String): SpannableString {
        val text = String.format(rawText, arg)
        val textColor = ContextCompat.getColor(
            requireContext(),
            R.color.red
        )
        val spannable = SpannableString(text)
        val textStart = text.indexOf(arg)
        val textEnd = textStart + arg.length
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            textStart,
            textEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return spannable
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadClinicLogoReceiver)

        _binding = null
    }
}