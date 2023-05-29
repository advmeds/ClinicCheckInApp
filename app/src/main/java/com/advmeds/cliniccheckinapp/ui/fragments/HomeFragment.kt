package com.advmeds.cliniccheckinapp.ui.fragments

import android.app.ActionBar.LayoutParams
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.utils.Converter
import com.advmeds.cliniccheckinapp.utils.showOnly
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.android.synthetic.main.format_checked_list.*
import kotlinx.android.synthetic.main.queueing_board_setting_dialog.*
import kotlinx.android.synthetic.main.queueing_machine_setting_dialog.*
import kotlinx.android.synthetic.main.text_input_dialog.*
import kotlinx.android.synthetic.main.version_setting_dialog.*
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
                addAction(SharedPreferencesRepo.DEPT_ID)
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

                    changeIsCheckInLayoutWeightIfItEmpty(viewModel.checkInItemList.size)
                }
            ).showNow(childFragmentManager, null)
            return@setOnLongClickListener true
        }

        changeIsCheckInLayoutWeightIfItEmpty(viewModel.checkInItemList.size)

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
                        6 -> onSetDeptIDItemClicked()
                        7 -> onSetQueueingBoardSettingItemClicked()
                        8 -> onSetQueueingMachineSettingItemClicked()
                        9 -> onSetVersionSettingItemClicked()
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
        itemList.forEachIndexed { index, checkInItem ->
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
                            (resources.getDimension(R.dimen.screen_panel_margin) / resources.displayMetrics.density).toInt()

                        val topMargin = if (index == 0) 0 else margin

                        val bottomMargin = if (index == (itemList.size - 1)) 0 else margin

                        setMargins(margin, topMargin, margin, bottomMargin)

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

                            changeIsCheckInLayoutWeightIfItEmpty(viewModel.checkInItemList.size)
                        }
                        .setNegativeButton(R.string.cancel, null)
                        .showOnly()

                    true
                }
            }
        }
    }

    private fun changeIsCheckInLayoutWeightIfItEmpty(size: Int) {

        val weight = if (size == 0) 0f else 2f

        val oldSize = binding.checkInLayout.childCount

        if (oldSize > 0 && size > 0)
            return

        val params = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            weight
        )

        binding.checkInLayout.layoutParams = params
    }

    private fun onSetServerDomainItemClicked() {

        val hint = requireContext().getString(R.string.customize_url_hint)
        val inputTextLabel = requireContext().getString(R.string.dialog_url_label)

        showTextInputDialog(
            titleResId = R.string.clinic_panel_url,
            inputText = viewModel.mSchedulerServerDomain,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            inputTextLabel = inputTextLabel,
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

        val inputTextLabel = requireContext().getString(R.string.dialog_id_label)

        showTextInputDialog(
            titleResId = R.string.org_id,
            inputTextLabel = inputTextLabel,
            inputText = viewModel.orgId,
            onConfirmClick = { id ->
                if (id.isNotBlank()) {
                    viewModel.orgId = id
                }
            })
    }

    private fun onSetDoctorsItemClicked() {

        val inputTextLabel = requireContext().getString(R.string.dialog_id_label)

        showTextInputDialog(
            titleResId = R.string.doctors,
            inputTextLabel = inputTextLabel,
            showDescription = true,
            inputText = viewModel.doctors.joinToString(","),
            onConfirmClick = { doctors ->
                viewModel.doctors = doctors.split(",").filter { it.isNotBlank() }.toSet()
            }
        )
    }

    private fun onSetRoomsItemClicked() {

        val inputTextLabel = requireContext().getString(R.string.dialog_id_label)

        showTextInputDialog(
            titleResId = R.string.rooms,
            inputTextLabel = inputTextLabel,
            inputText = viewModel.rooms.joinToString(","),
            onConfirmClick = { rooms ->
                viewModel.rooms = rooms.split(",").filter { it.isNotBlank() }.toSet()
            })
    }

    private fun onSetPanelModeItemClicked() {

        val hint = requireContext().getString(R.string.customize_url_hint)

        showTextInputDialog(
            titleResId = R.string.clinic_panel_url,
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI,
            hint = hint,
            inputTextLabel = "",
            inputText = viewModel.clinicPanelUrl ?: ""
        ) { clinicPanelUrl ->
            if (clinicPanelUrl.isNotBlank()) {
                viewModel.clinicPanelUrl = clinicPanelUrl
            }
        }
    }

    private fun onSetFormatCheckedListItemClicked() {
        val choiceItems = CreateAppointmentRequest.NationalIdFormat.values()
        val checkedItems = choiceItems.map { viewModel.formatCheckedList.contains(it) }

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.format_checked_list)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.fcl_title.setText(R.string.format_checked)

        setListOfCheckBox(
            container = dialog.fcl_check_box_container,
            names = choiceItems,
            checks = checkedItems
        )

        dialog.fcl_save_btn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onSetDeptIDItemClicked() {

        val inputTextLabel = requireContext().getString(R.string.dialog_id_label)

        showTextInputDialog(
            titleResId = R.string.dept_id,
            inputTextLabel = inputTextLabel,
            inputText = viewModel.deptId.joinToString(","),
            showDescription = true,
            onConfirmClick = { id ->
                if (id.isNotBlank()) {
                    viewModel.deptId = id.split(",").filter { it.isNotBlank() }.toSet()
                }
            })
    }

    private fun showTextInputDialog(
        titleResId: Int,
        inputTextLabel: String,
        inputText: String = "",
        hint: String = "",
        inputType: Int = InputType.TYPE_CLASS_TEXT,
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
        dialog.dialog_input_field.hint = hint
        dialog.dialog_input_field.inputType = inputType

        if (showDescription) {
            dialog.dialog_description.visibility = View.VISIBLE
        }

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

            var inputData = dialog.dialog_input_field.text.toString().trim()

            if (showRadioButton) {
                inputData = when (dialog.dialog_radio_group.checkedRadioButtonId) {
                    R.id.domain_service_official_site -> "https://www.mscheduler.com"
                    R.id.domain_service_testing_site -> "https://test.mscheduler.com"
                    R.id.domain_service_customize -> dialog.dialog_input_field.text.toString()
                        .trim()
                    else -> BuildConfig.MS_DOMAIN
                }
            }

            onConfirmClick(inputData)

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onSetQueueingBoardSettingItemClicked() {
        //TODO set correct data
        val items = arrayListOf("Portrait", "Landscape")

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.queueing_board_setting_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val adapter =
            ArrayAdapter(requireContext(), R.layout.support_simple_spinner_dropdown_item, items)
        dialog.queueing_board_setting_auto_complete_tv.setAdapter(adapter)


        dialog.queueing_board_setting_switcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                dialog.queueing_board_setting_container.visibility = View.VISIBLE
            else
                dialog.queueing_board_setting_container.visibility = View.GONE
        }

        val saveButton = dialog.btn_qbs_dialog_save
        val cancelButton = dialog.btn_qbs_dialog_cancel

        saveButton.setOnClickListener {
            val domain = dialog.et_qbs_irl_input.text.toString().trim()


//            TODO ask about that field
//            try {
//                dialog.dismiss()
//                HttpUrl.get(domain)
//
//                viewModel.mSchedulerServerDomain = domain
//
//                val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)
//
//                LocalBroadcastManager.getInstance(requireContext())
//                    .sendBroadcast(intent)
//
//            } catch (e: Exception) {
//                dialog.dismiss()
//                AlertDialog.Builder(requireContext())
//                    .setMessage(e.message)
//                    .setPositiveButton(R.string.confirm, null)
//                    .showOnly()
//            }

            dialog.dismiss()

        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onSetQueueingMachineSettingItemClicked() {

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.queueing_machine_setting_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val queueingMachineSettingModel = viewModel.queueingMachineSettings

        dialog.qms_cb_organization.isChecked = queueingMachineSettingModel.organization
        dialog.qms_cb_doctor.isChecked = queueingMachineSettingModel.doctor
        dialog.qms_cb_dept.isChecked = queueingMachineSettingModel.dept
        dialog.qms_cb_time.isChecked = queueingMachineSettingModel.time

        dialog.queueing_machine_setting_switcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                dialog.queueing_machine_setting_container.visibility = View.VISIBLE
            else
                dialog.queueing_machine_setting_container.visibility = View.GONE
        }

        val saveButton = dialog.btn_qms_dialog_save
        val cancelButton = dialog.btn_qms_dialog_cancel

        saveButton.setOnClickListener {

            val organization: Boolean = dialog.qms_cb_organization.isChecked
            val doctor: Boolean = dialog.qms_cb_doctor.isChecked
            val dept: Boolean = dialog.qms_cb_dept.isChecked
            val time: Boolean = dialog.qms_cb_time.isChecked

            dialog.dismiss()

            val queueingMachineSettingModelForSave = QueueingMachineSettingModel(
                organization = organization,
                doctor = doctor,
                dept = dept,
                time = time
            )

            if (queueingMachineSettingModelForSave.isSame(viewModel.queueingMachineSettings))
                return@setOnClickListener

            viewModel.queueingMachineSettings = queueingMachineSettingModelForSave
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

    }

    private fun onSetVersionSettingItemClicked() {

        val items = arrayOf("1.0", "2.0", "3.0")
        var currentLanguage = viewModel.language
        var currentVersion = items[0]

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.version_setting_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // language setting
        val languageDropDownAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                resources.getStringArray(R.array.language_items)
            )

        dialog.version_setting_select_language_tv.setAdapter(languageDropDownAdapter)

        dialog.version_setting_select_language_tv.setText(
            Converter.language_lang_code_to_name(
                requireContext(),
                viewModel.language
            ), false
        )

        dialog.version_setting_select_language_tv.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val item = parent.getItemAtPosition(position).toString()
                currentLanguage = Converter.language_name_to_lang_code(requireContext(), item)
            }


        // version

        val versionDropDownAdapter =
            ArrayAdapter(
                requireContext(),
                R.layout.support_simple_spinner_dropdown_item,
                items
            )

        dialog.version_setting_select_version_tv.setAdapter(versionDropDownAdapter)
        dialog.version_setting_select_version_tv.setText(currentVersion, false)

        dialog.version_setting_select_version_tv.onItemClickListener =
            AdapterView.OnItemClickListener { parent, _, position, _ ->
                val item = parent.getItemAtPosition(position).toString()
                currentVersion = item
            }

        val saveButton = dialog.btn_version_setting_dialog_save
        val cancelButton = dialog.btn_version_setting_dialog_cancel

        saveButton.setOnClickListener {
            if (currentLanguage != viewModel.language) {
                (requireActivity() as MainActivity).setLanguage(language = currentLanguage)
                viewModel.language = currentLanguage
            }

            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun Context.getDimensionFrom(attr: Int): Int {
        val typedValue = TypedValue()
        return if (this.theme.resolveAttribute(attr, typedValue, true))
            TypedValue.complexToDimensionPixelSize(typedValue.data, this.resources.displayMetrics)
        else 0
    }

    private fun setListOfCheckBox(
        container: LinearLayout?,
        names: Array<CreateAppointmentRequest.NationalIdFormat>,
        checks: List<Boolean>
    ) {
        for (i in 0 until names.size) {
            container?.addView(createCheckBox(title = names[i], value = checks[i]))
        }
    }

    private fun createCheckBox(
        title: CreateAppointmentRequest.NationalIdFormat,
        value: Boolean
    ): LinearLayout {

        val layoutParameters = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)

        val linearLayout = LinearLayout(requireContext())
        linearLayout.layoutParams = layoutParameters
        linearLayout.gravity = Gravity.CENTER_VERTICAL
        linearLayout.orientation = LinearLayout.HORIZONTAL

        val checkBox = MaterialCheckBox(requireContext())

        checkBox.layoutParams = layoutParameters
        checkBox.gravity = Gravity.CENTER
        checkBox.scaleX = 1.0f
        checkBox.scaleY = 1.0f
        checkBox.isChecked = value

        checkBox.setOnCheckedChangeListener { _, isChecked ->

            val list = viewModel.formatCheckedList.toMutableList()

            if (isChecked) {
                list.add(title)
            } else {
                list.remove(title)
            }
            viewModel.formatCheckedList = list
        }

        val outValue = TypedValue()
        resources.getValue(R.dimen.font_size_h4_float, outValue, true)
        val textSize = outValue.float

        val textView = TextView(requireContext())

        textView.layoutParams = layoutParameters
        textView.setTextColor(Color.BLACK)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize)
        textView.setText(title.description)

        linearLayout.addView(checkBox)
        linearLayout.addView(textView)

        return linearLayout
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