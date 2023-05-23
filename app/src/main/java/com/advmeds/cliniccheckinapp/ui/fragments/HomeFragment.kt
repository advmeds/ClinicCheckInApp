package com.advmeds.cliniccheckinapp.ui.fragments

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.utils.showOnly
import kotlinx.android.synthetic.main.text_input_dialog.*
import okhttp3.HttpUrl

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private var _binding: HomeFragmentBinding? = null
    private lateinit var dialog: Dialog

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
                addAction(SharedPreferencesRepo.CHECK_IN_ITEM_LIST)
            }
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.root.setOnLongClickListener {
            EditCheckInItemDialog(
                onConfirmClick = {
                    viewModel.checkInItemList = viewModel.checkInItemList.plus(it)
                }
            ).showNow(childFragmentManager, null)
            return@setOnLongClickListener true
        }

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

        binding.checkInLayout.removeAllViews()
        val itemList = viewModel.checkInItemList.filter {
            when (it.type) {
                EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT -> {
                    true
                }
                EditCheckInItemDialog.CheckInItemType.CUSTOM -> {
                    (viewModel.rooms.isEmpty() || viewModel.rooms.contains(it.divisionId)) &&
                            (viewModel.doctors.isEmpty() || viewModel.doctors.contains(it.doctorId))
                }
                EditCheckInItemDialog.CheckInItemType.VIRTUAL_CARD -> {
                    true
                }
            }
        }
        itemList.forEach { checkInItem ->
            layoutInflater.inflate(
                if (itemList.size > 1) {
                    R.layout.check_in_item_card_view_horizontal
                } else {
                    R.layout.check_in_item_card_view_vertical
                }, null, false
            ).apply {
                val itemImg = findViewById<ImageView>(R.id.item_image_view)
                val itemTitle = findViewById<TextView>(R.id.item_title_tv)
                val itemBody = findViewById<TextView>(R.id.item_body_tv)

                when (checkInItem.type) {
                    EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT -> {
                        itemImg.setImageResource(R.drawable.ic_baseline_keyboard)
                        itemTitle.setText(R.string.check_in_item_manual_title)
                        itemBody.setText(R.string.check_in_item_manual_body)
                    }
                    EditCheckInItemDialog.CheckInItemType.CUSTOM -> {
                        itemImg.setImageResource(R.drawable.ic_baseline_how_to_reg)
                        itemTitle.text = checkInItem.title
                        itemBody.setText(R.string.check_in_item_manual_body)
                    }
                    EditCheckInItemDialog.CheckInItemType.VIRTUAL_CARD -> {
                        itemImg.setImageResource(R.drawable.ic_baseline_qr_code)
                        itemTitle.setText(R.string.check_in_item_virtual_title)
                        itemBody.setText(R.string.check_in_item_virtual_body)
                    }
                }

                binding.checkInLayout.addView(
                    this,
                    LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                    ).apply {
                        val margin =
                            (resources.getDimension(R.dimen.card_view_half_spacing) / resources.displayMetrics.density).toInt()
                        setMargins(margin, margin, margin, margin)
                    }
                )

                setOnClickListener {
                    when (checkInItem.type) {
                        EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT -> {
                            findNavController().navigate(R.id.manualInputFragment)
                        }
                        EditCheckInItemDialog.CheckInItemType.CUSTOM -> {
                            (requireActivity() as MainActivity).createFakeAppointment(
                                schedule = GetScheduleResponse.ScheduleBean(
                                    doctor = checkInItem.doctorId,
                                    division = checkInItem.divisionId
                                )
                            )
                        }
                        EditCheckInItemDialog.CheckInItemType.VIRTUAL_CARD -> {
                            (requireActivity() as MainActivity).checkInWithVirtualCard()
                        }
                    }
                }

                setOnLongClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_item_title)
                        .setPositiveButton(R.string.confirm) { _, _ ->
                            viewModel.checkInItemList = viewModel.checkInItemList.minus(checkInItem)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .showOnly()

                    true
                }
            }
        }
    }

    private fun onSetServerDomainItemClicked() {

        val hint = requireContext().getString(R.string.customize_url_hint)

        showTextInputDialog(
            titleResId = R.string.clinic_panel_url,
            inputText = viewModel.mSchedulerServerDomain,
            inputTextLabel = "Url",
            hint = hint,
            showRadioButton = true,
            onConfirmClick = { domain ->

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
        )
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
            .setPositiveButton(R.string.save, null)
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
                R.string.save
            ) { _, _ ->
                onConfirmClick(editText.text.toString().trim())
            }
            .setNegativeButton(R.string.cancel, null)
            .showOnly()
    }

    private fun showTextInputDialog(
        titleResId: Int,
        inputTextLabel: String,
        inputText: String = "",
        hint: String = "",
        showRadioButton: Boolean = false,
        showDescription: Boolean = false,
        onConfirmClick: (String) -> Unit
    ) {

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.text_input_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.dialog_title.setText(titleResId)
        dialog.dialog_text_input_label.text = inputTextLabel
        dialog.dialog_input_field.setText(inputText)

        if (showRadioButton) {

            val urlContainer = dialog.dialog_input_container

            dialog.dialog_radio_group.visibility = View.VISIBLE
            urlContainer.visibility = View.GONE

            dialog.dialog_radio_group.setOnCheckedChangeListener(
                RadioGroup.OnCheckedChangeListener { _, checkedId ->
                    when (checkedId) {
                        R.id.domain_service_official_site -> urlContainer.visibility = View.GONE
                        R.id.domain_service_testing_site -> urlContainer.visibility = View.GONE
                        R.id.domain_service_customize -> urlContainer.visibility = View.VISIBLE
                    }
                }
            )
        }

        dialog.dialog_cancel_btn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialog_save_btn.setOnClickListener {
            if (showRadioButton) {

                val domain = when (dialog.dialog_radio_group.checkedRadioButtonId) {
                    R.id.domain_service_official_site -> "https://www.mscheduler.com"
                    R.id.domain_service_testing_site -> "https://test.mscheduler.com"
                    R.id.domain_service_customize -> dialog.dialog_input_field.text.toString()
                        .trim()
                    else -> "https://www.mscheduler.com"
                }
                onConfirmClick(domain)

                dialog.dismiss()
                return@setOnClickListener
            }
        }

        dialog.show()
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