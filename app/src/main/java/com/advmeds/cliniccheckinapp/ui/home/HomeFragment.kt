package com.advmeds.cliniccheckinapp.ui.home

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.sharePreferences.QueueingMachineSettingModel
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.ui.inputPage.InputPageFragment
import com.advmeds.cliniccheckinapp.utils.Converter
import com.advmeds.cliniccheckinapp.utils.showOnly
import kotlinx.android.synthetic.main.change_clinic_id_dialog.*
import kotlinx.android.synthetic.main.change_dept_id_dialog.*
import kotlinx.android.synthetic.main.change_doctor_id_dialog.*
import kotlinx.android.synthetic.main.change_domain_dialog.*
import kotlinx.android.synthetic.main.change_room_id_dialog.*
import kotlinx.android.synthetic.main.queueing_board_setting_dialog.*
import kotlinx.android.synthetic.main.queueing_machine_setting_dialog.*
import kotlinx.android.synthetic.main.settings_dialog.*
import kotlinx.android.synthetic.main.version_setting_dialog.*
import okhttp3.HttpUrl


class HomeFragment : Fragment() {

    private var _binding: HomeFragmentBinding? = null

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var dialog: Dialog

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val reloadClinicLogoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clinicLogoUrl = intent?.getStringExtra(InputPageFragment.CLINIC_LOGO_URL_KEY)
            setLogo(clinicLogoUrl)
            viewModel.logo = clinicLogoUrl
        }
    }

    private fun setLogo(clinicLogoUrl: String?) {
        binding.logoImageView.load(clinicLogoUrl)
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

        setLogo(viewModel.logo)

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

            dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.settings_dialog)

            if (dialog.window == null)
                return@setOnLongClickListener true

            dialog.window!!.setGravity(Gravity.CENTER)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val settingItems = requireContext().resources.getStringArray(R.array.setting_items)

            val arrayAdapter =
                ArrayAdapter(requireContext(), R.layout.settings_list_item, settingItems)

            dialog.setting_options_list_view.adapter = arrayAdapter

            dialog.setting_options_list_view.setOnItemClickListener { parent, view, position, id ->

                dialog.dismiss()

                when (position) {
                    0 -> onSetServerDomainItemClicked()
                    1 -> onSetOrgIDItemClicked()
                    2 -> onSetDoctorIDItemClicked()
                    3 -> onSetRoomIDItemClicked()
                    4 -> onSetDeptIDItemClicked()
                    5 -> onSetQueueingBoardSettingItemClicked()
                    6 -> onSetQueueingMachineSettingItemClicked()
                    7 -> onSetVersionSettingItemClicked()
                }
            }

            dialog.show()

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

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_domain_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.et_domain_service_url_input.setText(viewModel.mSchedulerServerDomain)

        val radioGroup = dialog.domain_service_radio_group
        val urlContainer = dialog.domain_service_url_input_container

        radioGroup.setOnCheckedChangeListener(
            RadioGroup.OnCheckedChangeListener { _, checkedId ->
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
                else -> "https://test.mscheduler.com"
            }

            dialog.dismiss()

            if (domain == viewModel.mSchedulerServerDomain)
                return@setOnClickListener

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

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_clinic_id_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.et_clinic_id_input.setText(viewModel.orgId)

        val saveButton = dialog.btn_change_clinic_id_dialog_save
        val cancelButton = dialog.btn_change_clinic_id_dialog_cancel

        saveButton.setOnClickListener {
            val id = dialog.et_clinic_id_input.text.toString().trim()
            dialog.dismiss()

            if (id.isNotBlank()) {

                if (viewModel.orgId == id)
                    return@setOnClickListener

                viewModel.orgId = id

                val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(intent)
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onSetDoctorIDItemClicked() {

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_doctor_id_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //dialog.et_clinic_id_input.setText(viewModel.orgId)

        val saveButton = dialog.btn_change_doctor_id_dialog_save
        val cancelButton = dialog.btn_change_doctor_id_dialog_cancel

        saveButton.setOnClickListener {
            val editText = dialog.et_doctor_id_input.text.toString().trim()
            dialog.dismiss()

            if (editText.isNotBlank()) {
                val doctorSpaceLess = editText.replace(" ", "")
                val doctorList = doctorSpaceLess.split(",")

                if (viewModel.doctorIds == doctorList)
                    return@setOnClickListener

                viewModel.doctorIds = doctorList

                val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(intent)
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun onSetRoomIDItemClicked() {

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_room_id_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        //dialog.et_clinic_id_input.setText(viewModel.orgId)

        val saveButton = dialog.btn_change_room_id_dialog_save
        val cancelButton = dialog.btn_change_room_id_dialog_cancel

        saveButton.setOnClickListener {

            val editText = dialog.et_room_id_input.text.toString().trim()
            dialog.dismiss()

            if (editText.isNotBlank()) {
                val roomSpaceLess = editText.replace(" ", "")
                val roomList = roomSpaceLess.split(",")

                if (viewModel.roomIds == roomList)
                    return@setOnClickListener

                viewModel.roomIds = roomList

                val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(intent)
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onSetDeptIDItemClicked() {

        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.change_dept_id_dialog)

        if (dialog.window == null)
            return

        dialog.window!!.setGravity(Gravity.CENTER)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val saveButton = dialog.btn_change_dept_id_dialog_save
        val cancelButton = dialog.btn_change_dept_id_dialog_cancel

        saveButton.setOnClickListener {
            val editText = dialog.et_dept_id_input.text.toString().trim()
            dialog.dismiss()

            if (editText.isNotBlank()) {
                if (viewModel.deptId == editText)
                    return@setOnClickListener

                viewModel.deptId = editText

                val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                LocalBroadcastManager.getInstance(requireContext())
                    .sendBroadcast(intent)
            }
        }

        cancelButton.setOnClickListener {
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

            val queueingMachineSettingModel = QueueingMachineSettingModel(
                organization = organization,
                doctor = doctor,
                dept = dept,
                time = time
            )

            if (!queueingMachineSettingModel.isNotSame(viewModel.queueingMachineSettings))
                return@setOnClickListener

            viewModel.queueingMachineSettings = queueingMachineSettingModel

            val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

            LocalBroadcastManager.getInstance(requireContext())
                .sendBroadcast(intent)
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

        // version setting
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
            TypedValue.complexToDimensionPixelSize(
                typedValue.data,
                this.resources.displayMetrics
            )
        else 0
    }

    override fun onDestroyView() {
        super.onDestroyView()

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadClinicLogoReceiver)

        _binding = null
    }
}