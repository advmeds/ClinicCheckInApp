package com.advmeds.cliniccheckinapp.ui.home

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.ui.inputPage.InputPageFragment
import com.advmeds.cliniccheckinapp.utils.showOnly
import kotlinx.android.synthetic.main.change_domain_dialog.*
import okhttp3.HttpUrl


class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null

    private val viewModel: HomeViewModel by viewModels()

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

        // Text Views

        val cardInfoText = getString(R.string.home_page_card_info_label)
        val cardInfoArg = getString(R.string.home_page_card_info_label_arg)
        val cardInfoSpannable = getTextWithRedPiece(cardInfoText, cardInfoArg)

        val manuallyInputText = getString(R.string.home_page_manually_input_card_label)
        val manuallyInputArg = getString(R.string.home_page_manually_input_card_label_arg)
        val manuallyInputSpannable = getTextWithRedPiece(manuallyInputText, manuallyInputArg)

        val pcuCheckText = getString(R.string.home_page_pcu_check_label)
        val pcuCheckArg = getString(R.string.home_page_pcu_check_label_arg)
        val pcuCheckSpannable = getTextWithRedPiece(pcuCheckText, pcuCheckArg)

        binding.homePageCardInfoTitle.text = cardInfoSpannable
        binding.homePageManuallyInputTitle.text = manuallyInputSpannable
        binding.homePagePcuCheckTitle.text = pcuCheckSpannable


        // Buttons

        binding.logoImageView.setOnLongClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.setting)
                .setItems(R.array.setting_items) { _, index ->
                    when (index) {
                        0 -> {
                            onSetServerDomainItemClicked()
                        }
                        1 -> {
                            onSetOrgIDItemClicked()
                        }
                    }
                }
                .showOnly()

            return@setOnLongClickListener true
        }

        binding.manuallyInputCard.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToInputPageFragment()
            findNavController().navigate(action)
        }

        binding.pcuCheckInCard.setOnClickListener {
            (requireActivity() as MainActivity).createAppointment()
        }
    }

    private fun onSetServerDomainItemClicked() {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_domain_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val radioGroup = dialog.domain_service_radio_group
        val urlContainer = dialog.domain_service_url_input_container

        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { radioGroup, checkedId ->
                when (checkedId) {
                    R.id.domain_service_official_site -> urlContainer.visibility = View.GONE
                    R.id.domain_service_testing_site -> urlContainer.visibility = View.GONE
                    R.id.domain_service_customize -> urlContainer.visibility = View.VISIBLE
                }
            }
        )

        val saveButton = dialog.btn_domain_service_save
        val cancelButton = dialog.btn_domain_service_cancel

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {

            val domain = when (radioGroup.checkedRadioButtonId) {
                R.id.domain_service_official_site -> "https://www.mscheduler.com"
                R.id.domain_service_testing_site -> "https://test.mscheduler.com"
                R.id.domain_service_customize ->
                    dialog.et_domain_service_url_input.text.toString().trim()
                else -> "https://www.mscheduler.com"
            }

            try {
                HttpUrl.get(domain)

                viewModel.mSchedulerServerDomain = domain

                val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(intent)

            } catch (e: Exception) {
                AlertDialog.Builder(requireContext())
                    .setMessage(e.message)
                    .setPositiveButton(R.string.confirm, null)
                    .showOnly()
            }
        }

        dialog.show()
    }

    private fun onSetOrgIDItemClicked() {
        val editText = EditText(requireContext())
        editText.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextAppearance(R.style.TextAppearance_AppCompat_Subhead)
        } else {
            editText.setTextAppearance(requireContext(), R.style.TextAppearance_AppCompat_Subhead)
        }

        editText.setText(viewModel.orgId)

        val layout = LinearLayout(requireContext())
        val padding = requireContext().getDimensionFrom(R.attr.dialogPreferredPadding)
        layout.setPaddingRelative(padding, 0, padding, 0)
        layout.addView(editText)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.org_id)
            .setView(layout)
            .setPositiveButton(
                R.string.confirm
            ) { _, _ ->
                val id = editText.text.toString().trim()

                if (id.isNotBlank()) {
                    viewModel.orgId = id

                    val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                    LocalBroadcastManager.getInstance(requireContext())
                        .sendBroadcast(intent)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .showOnly()
    }


    private fun getTextWithRedPiece(rawText: String, arg: String): SpannableString {
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

    private fun Context.getDimensionFrom(attr: Int): Int {
        val typedValue = TypedValue()
        return if (this.theme.resolveAttribute(attr, typedValue, true))
            TypedValue.complexToDimensionPixelSize(typedValue.data, this.resources.displayMetrics)
        else 0
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadClinicLogoReceiver)

        _binding = null
    }
}