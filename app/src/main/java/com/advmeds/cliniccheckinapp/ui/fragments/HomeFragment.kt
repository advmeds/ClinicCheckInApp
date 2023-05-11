package com.advmeds.cliniccheckinapp.ui.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.utils.showOnly
import okhttp3.HttpUrl

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: HomeFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val reloadClinicLogoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clinicLogoUrl = intent?.getStringExtra(SharedPreferencesRepo.LOGO_URL)
            binding.logoImageView.load(clinicLogoUrl)
        }
    }

    private val reloadRightCardViewReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            setupUI()
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
            IntentFilter(SharedPreferencesRepo.LOGO_URL)
        )

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(
            reloadRightCardViewReceiver,
            IntentFilter(SharedPreferencesRepo.ROOMS).apply {
                addAction(SharedPreferencesRepo.DOCTORS)
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.logoImageView.load(viewModel.logoUrl)
        binding.logoImageView.setOnLongClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.setting)
                .setItems(R.array.setting_items) { _, index ->
                    when (index) {
                        0 -> onSetServerDomainItemClicked()
                        1 -> onSetOrgIDItemClicked()
                        2 -> onSetDoctorsItemClicked()
                        3 -> onSetRoomsItemClicked()
                        4 -> onSetPanelModeItemClicked()
                        5 -> onSetFormatCheckedListItemClicked()
                    }
                }
                .showOnly()

            return@setOnLongClickListener true
        }

        val arg = getString(R.string.health_card)
        val text = String.format(getString(R.string.present_health_card), arg)
        val textColor = ContextCompat.getColor(
            requireContext(),
            R.color.error
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

        binding.presentTitleTextView.text = spannable

        binding.homeRightTopCardView.isGone = when(BuildConfig.BUILD_TYPE) {
            "rende" -> viewModel.rooms.isNotEmpty() &&
                    !viewModel.rooms.contains(GetScheduleResponse.ScheduleBean.RENDE_VACCINE.division)
            else -> false
        }
        binding.homeRightTopCardView.setOnClickListener {
            when(BuildConfig.BUILD_TYPE) {
                "rende" -> {
                    (requireActivity() as MainActivity).createFakeAppointment(
                        schedule = GetScheduleResponse.ScheduleBean.RENDE_VACCINE
                    )
                }
                else -> {
                    findNavController().navigate(R.id.manualInputFragment)
                }
            }
        }

        binding.homeRoghtBottomCardView.isGone = when(BuildConfig.BUILD_TYPE) {
            "ptch" -> viewModel.doctors.isNotEmpty() &&
                    !viewModel.doctors.contains(GetScheduleResponse.ScheduleBean.PTCH_BABY.doctor)
            "rende" -> viewModel.rooms.isNotEmpty() &&
                    !viewModel.rooms.contains(GetScheduleResponse.ScheduleBean.RENDE_CHECK_UP.division)
            else -> false
        }
        binding.homeRoghtBottomCardView.setOnClickListener {
            when(BuildConfig.BUILD_TYPE) {
                "ptch" -> {
                    (requireActivity() as MainActivity).createFakeAppointment(
                        schedule = GetScheduleResponse.ScheduleBean.PTCH_BABY
                    )
                }
                "rende" -> {
                    (requireActivity() as MainActivity).createFakeAppointment(
                        schedule = GetScheduleResponse.ScheduleBean.RENDE_CHECK_UP
                    )
                }
                else -> {
                    (requireActivity() as MainActivity).checkInWithVirtualCard()
                }
            }
        }
    }

    private fun onSetServerDomainItemClicked() {
        showTextInputDialog(
            titleResId = R.string.clinic_panel_url,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            hint = "https://example.com",
            defaultText = viewModel.mSchedulerServerDomain
        ) { domain ->
            try {
                HttpUrl.get(domain)

                viewModel.mSchedulerServerDomain = domain
            } catch (e: Exception) {
                AlertDialog.Builder(requireContext())
                    .setMessage(e.message)
                    .setPositiveButton(R.string.confirm, null)
                    .showOnly()
            }
        }
    }

    private fun onSetOrgIDItemClicked() {
        showTextInputDialog(
            titleResId = R.string.org_id,
            defaultText = viewModel.orgId
        ) { id ->
            if (id.isNotBlank()) {
                viewModel.orgId = id
            }
        }
    }

    private fun onSetDoctorsItemClicked() {
        showTextInputDialog(
            titleResId = R.string.doctors,
            defaultText = viewModel.doctors.joinToString(",")
        ) { rooms ->
            viewModel.doctors = rooms.split(",").filter { it.isNotBlank() }.toSet()
        }
    }

    private fun onSetRoomsItemClicked() {
        showTextInputDialog(
            titleResId = R.string.rooms,
            defaultText = viewModel.rooms.joinToString(",")
        ) { rooms ->
            viewModel.rooms = rooms.split(",").filter { it.isNotBlank() }.toSet()
        }
    }

    private fun onSetPanelModeItemClicked() {
        showTextInputDialog(
            titleResId = R.string.clinic_panel_url,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            hint = "https://example.com",
            defaultText = viewModel.clinicPanelUrl ?: ""
        ) { clinicPanelUrl ->
            if (clinicPanelUrl.isNotBlank()) {
                viewModel.clinicPanelUrl = clinicPanelUrl
            }
        }
    }

    private fun onSetFormatCheckedListItemClicked() {
        val choiceItems = CreateAppointmentRequest.NationalIdFormat.values()
        val checkedItems = choiceItems.map { viewModel.formatCheckedList.contains(it) }

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.format_checked)
            .setMultiChoiceItems(
                choiceItems.map { getString(it.description) }.toTypedArray(),
                checkedItems.toBooleanArray()
            ) { _, index, isChecked ->
                val list = viewModel.formatCheckedList.toMutableList()
                val format = choiceItems[index]

                if (isChecked) {
                    list.add(format)
                } else {
                    list.remove(format)
                }

                viewModel.formatCheckedList = list
            }
            .setPositiveButton(R.string.confirm, null)
            .showOnly()
    }

    private fun showTextInputDialog(
        titleResId: Int,
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        hint: String = "",
        defaultText: String = "",
        onConfirmClick: (String) -> Unit
    ) {
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

        editText.inputType = inputType
        editText.hint = hint
        editText.setText(defaultText)

        val layout = LinearLayout(requireContext())
        val padding = requireContext().getDimensionFrom(R.attr.dialogPreferredPadding)
        layout.setPaddingRelative(padding, 0, padding, 0)
        layout.addView(editText)

        AlertDialog.Builder(requireContext())
            .setTitle(titleResId)
            .setView(layout)
            .setPositiveButton(
                R.string.confirm
            ) { _, _ ->
                onConfirmClick(editText.text.toString().trim())
            }
            .setNegativeButton(R.string.cancel, null)
            .showOnly()
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
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadRightCardViewReceiver)

        _binding = null
    }
}