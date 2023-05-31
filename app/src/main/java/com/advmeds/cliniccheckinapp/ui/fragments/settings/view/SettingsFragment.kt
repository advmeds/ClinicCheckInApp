package com.advmeds.cliniccheckinapp.ui.fragments.settings.view

import android.app.ActionBar
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isGone
import androidx.fragment.app.ListFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.SettingsFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.request.CreateAppointmentRequest
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharedPreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.ui.fragments.home.adapter.LanguageAdapter
import com.advmeds.cliniccheckinapp.ui.fragments.home.model.LanguageModel
import com.advmeds.cliniccheckinapp.ui.fragments.home.model.combineArrays
import com.advmeds.cliniccheckinapp.ui.fragments.settings.adapter.SettingsAdapter
import com.advmeds.cliniccheckinapp.ui.fragments.settings.viewModel.SettingsViewModel
import com.advmeds.cliniccheckinapp.utils.Converter
import com.advmeds.cliniccheckinapp.utils.showOnly
import com.google.android.material.checkbox.MaterialCheckBox
import kotlinx.android.synthetic.main.format_checked_list.*
import kotlinx.android.synthetic.main.language_setting_dialog.*
import kotlinx.android.synthetic.main.queueing_board_setting_dialog.*
import kotlinx.android.synthetic.main.queueing_machine_setting_dialog.*
import kotlinx.android.synthetic.main.text_input_dialog.*
import kotlinx.android.synthetic.main.ui_setting_dialog.*
import okhttp3.HttpUrl

class SettingsFragment : ListFragment() {

    private val viewModel: SettingsViewModel by viewModels()

    private var _binding: SettingsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var toolbar: Toolbar


    private lateinit var dialog: Dialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SettingsFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        toolbar = binding.toolbar
        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        toolbar.title = resources.getString(R.string.setting)
        toolbar.setNavigationOnClickListener {
            findNavController().navigate(R.id.homeFragment)
        }

        val adapter = SettingsAdapter(
            mContext = requireContext(),
            settingItems = resources.getStringArray(R.array.setting_items)
        )

        listAdapter = adapter
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        when (position) {
            0 -> onSetUiSettingsItemClicked()
            1 -> onSetServerDomainItemClicked()
            2 -> onSetOrgIDItemClicked()
            3 -> onSetDoctorsItemClicked()
            4 -> onSetRoomsItemClicked()
            5 -> onSetPanelModeItemClicked()
            6 -> onSetFormatCheckedListItemClicked()
            7 -> onSetDeptIDItemClicked()
            8 -> onSetQueueingBoardSettingItemClicked()
            9 -> onSetQueueingMachineSettingItemClicked()
            10 -> onSetLanguageSettingItemClicked()
        }
    }


    private fun onSetUiSettingsItemClicked() {

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.ui_setting_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val checkInItems = EditCheckInItemDialog.toObject(viewModel.checkInItemList)

        dialog.ui_settings_manual_input.isChecked = checkInItems.manualInput.isShow
        dialog.ui_settings_virtual_nhi_card.isChecked = checkInItems.virtualCard.isShow

        dialog.ui_settings_customized_one.isChecked = checkInItems.customOne.isShow
        dialog.ui_settings_customized_one_container.isGone = !checkInItems.customOne.isShow

        dialog.ui_settings_customized_two.isChecked = checkInItems.customTwo.isShow
        dialog.ui_settings_customized_two_container.isGone = !checkInItems.customTwo.isShow


        dialog.ui_settings_manual_input.setOnCheckedChangeListener { _, isChecked ->
            checkInItems.manualInput.isShow = isChecked
        }

        dialog.ui_settings_virtual_nhi_card.setOnCheckedChangeListener { _, isChecked ->
            checkInItems.virtualCard.isShow = isChecked
        }

        dialog.ui_settings_customized_one.setOnCheckedChangeListener { _, isChecked ->
            checkInItems.customOne.isShow = isChecked
            dialog.ui_settings_customized_one_container.isGone = !isChecked
        }

        dialog.ui_settings_customized_two.setOnCheckedChangeListener { _, isChecked ->

            checkInItems.customTwo.isShow = isChecked
            dialog.ui_settings_customized_two_container.isGone = !isChecked
        }

        dialog.ui_settings_save_btn.setOnClickListener {
            viewModel.checkInItemList = EditCheckInItemDialog.toList(checkInItems)

            dialog.dismiss()
        }

        dialog.ui_settings_cancel_btn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
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
        dialog.dialog_input_field.editText?.setText(inputText)
        dialog.dialog_input_field.placeholderText = hint
        dialog.dialog_input_field.hint = inputTextLabel
        dialog.dialog_input_field.editText?.inputType = inputType

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

            var inputData = dialog.dialog_input_field.editText?.text.toString().trim()

            if (showRadioButton) {
                inputData = when (dialog.dialog_radio_group.checkedRadioButtonId) {
                    R.id.domain_service_official_site -> "https://www.mscheduler.com"
                    R.id.domain_service_testing_site -> "https://test.mscheduler.com"
                    R.id.domain_service_customize -> dialog.dialog_input_field.editText?.text.toString()
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

        val label = resources.getString(R.string.qbs_screen_edit_text_label)

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.queueing_board_setting_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.et_qbs_irl_input.hint = label

        dialog.queueing_board_setting_switcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked)
                dialog.queueing_board_setting_container.visibility = View.VISIBLE
            else
                dialog.queueing_board_setting_container.visibility = View.GONE
        }

        val saveButton = dialog.btn_qbs_dialog_save
        val cancelButton = dialog.btn_qbs_dialog_cancel

        saveButton.setOnClickListener {
            val qbsDomain = dialog.et_qbs_irl_input.editText?.text.toString().trim()

            try {
                HttpUrl.get(qbsDomain)

                viewModel.queueingBoardSettings = qbsDomain
            } catch (e: Exception) {
                AlertDialog.Builder(requireContext())
                    .setMessage(e.message)
                    .setPositiveButton(R.string.confirm, null)
                    .showOnly()
            }

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

    private fun onSetLanguageSettingItemClicked() {

        var currentLanguage = viewModel.language
        var languageArrayWithIsSelected = getLanguageArray(currentLanguage)


        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.language_setting_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val adapter = LanguageAdapter(
            itemList = languageArrayWithIsSelected,
            LanguageAdapter.OnClickListener { language ->
                currentLanguage = Converter.language_name_to_lang_code(requireContext(), language)
                languageArrayWithIsSelected = getLanguageArray(currentLanguage)
            })

        dialog.language_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        dialog.language_recycler_view.adapter = adapter

        val saveButton = dialog.btn_version_setting_dialog_save
        val cancelButton = dialog.btn_version_setting_dialog_cancel

        saveButton.setOnClickListener {
            if (currentLanguage != viewModel.language) {
                (requireActivity() as MainActivity).setLanguage(language = currentLanguage)
                viewModel.language = currentLanguage
                setupUI()
            }

            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun getLanguageArray(currentLanguage: String): Array<LanguageModel> {
        val longLanguage = Converter.language_lang_code_to_name(requireContext(), currentLanguage)

        val languageArray = resources.getStringArray(R.array.language_items)
        val isSelectedArray = languageArray.map { it.contains(longLanguage) }

        return combineArrays(languageArray, isSelectedArray)
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

        val layoutParameters = ActionBar.LayoutParams(
            ActionBar.LayoutParams.WRAP_CONTENT,
            ActionBar.LayoutParams.WRAP_CONTENT
        )

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

        _binding = null
    }
}