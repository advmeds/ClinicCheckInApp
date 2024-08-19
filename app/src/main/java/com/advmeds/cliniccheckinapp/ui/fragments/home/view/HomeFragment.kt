package com.advmeds.cliniccheckinapp.ui.fragments.home.view

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import coil.load
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.EditCheckInItemDialog
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.repositories.AnalyticsRepositoryImpl
import com.advmeds.cliniccheckinapp.repositories.RoomRepositories
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.ui.fragments.home.eventLogger.HomeEventLogger
import com.advmeds.cliniccheckinapp.ui.fragments.home.viewmodel.HomeViewModel
import com.advmeds.cliniccheckinapp.ui.fragments.home.viewmodel.HomeViewModelFactory
import kotlinx.android.synthetic.main.text_input_dialog.*


class HomeFragment : Fragment() {

    private lateinit var dialog: Dialog

    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(
            application = requireActivity().application,
            homeEventLogger = HomeEventLogger(
                AnalyticsRepositoryImpl.getInstance(
                    RoomRepositories.eventsRepository,
                    SharedPreferencesRepo.getInstance(requireContext().applicationContext)
                )
            )
        )
    }

    private var _binding: HomeFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val reloadTitle = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val title = intent?.getStringExtra(SharedPreferencesRepo.MACHINE_TITLE)
            binding.appCompatTextView.text =
                if (title.isNullOrEmpty()) getString(R.string.app_name) else title
        }
    }

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

    private val reloadPresentCardTextReceiver = object : BroadcastReceiver() {
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

        LocalBroadcastManager.getInstance(requireContext().applicationContext).registerReceiver(
            reloadClinicLogoReceiver,
            IntentFilter(SharedPreferencesRepo.LOGO_URL)
        )

        LocalBroadcastManager.getInstance(requireContext().applicationContext).registerReceiver(
            reloadTitle,
            IntentFilter(SharedPreferencesRepo.MACHINE_TITLE)
        )

        LocalBroadcastManager.getInstance(requireContext().applicationContext).registerReceiver(
            reloadRightCardViewReceiver,
            IntentFilter(SharedPreferencesRepo.ROOMS).apply {
                addAction(SharedPreferencesRepo.DOCTORS)
                addAction(SharedPreferencesRepo.CHECK_IN_ITEM_LIST)
                addAction(SharedPreferencesRepo.DEPT_ID)
            }
        )

        LocalBroadcastManager.getInstance(requireContext().applicationContext).registerReceiver(
            reloadPresentCardTextReceiver,
            IntentFilter(SharedPreferencesRepo.AUTOMATIC_APPOINTMENT_SETTING)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {

        binding.logoImageView.load(viewModel.logoUrl)
        binding.appCompatTextView.text =
            viewModel.machineTitle.ifEmpty { getString(R.string.app_name) }

        binding.settingsIcon.setOnClickListener {

            val inputTextLabel = requireContext().getString(R.string.password)

            showTextInputDialog(
                titleResId = R.string.advanced_settings,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                inputTextLabel = inputTextLabel,
                positiveButtonTextResId = R.string.dialog_ok_button,
                onConfirmClick = {
                    if (it == viewModel.password) {
                        findNavController().navigate(R.id.settingsFragment)
                        viewModel.eventOpenSettingScreen()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.password_is_incorrect),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )

            return@setOnClickListener
        }

        val firstArg = getString(R.string.health_card)
        val secondArg =
            if (viewModel.queueingMachineSettingIsEnable)
                getString(R.string.present_health_card_arg_take_no)
            else
                getString(R.string.present_health_card_arg_check_in)

        val automaticAppointmentAddition =
            if (viewModel.automaticAppointmentSetting.isEnabled) {
                getString(R.string.present_health_card_auto_appointment_addition)
            } else {
                ""
            }


        val presentHealthText = getString(R.string.present_health_card)

        val text =
            "${String.format(presentHealthText, firstArg, secondArg)}$automaticAppointmentAddition"

        val isShowInsertNHICardAnimation = viewModel.isShowInsertNHICardAnimation

        val textColor = ContextCompat.getColor(
            requireContext(),
            R.color.error
        )
        val textStart = text.indexOf(firstArg)
        val textEnd = textStart + firstArg.length
        val spannable = SpannableString(text)
        spannable.setSpan(
            ForegroundColorSpan(textColor),
            textStart,
            textEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.presentTitleTextView.text = spannable

        binding.checkInLayout.removeAllViews()

        binding.insertNhiCardAnimation.isGone = !isShowInsertNHICardAnimation

        val itemList = viewModel.checkInItemList.filter {
            it.isShow
        }

        itemList.forEachIndexed { index, checkInItem ->
            val resource = if (itemList.size == 1 && isShowInsertNHICardAnimation) {
                R.layout.check_in_item_card_view_vertical
            } else {
                R.layout.check_in_item_card_view_horizontal
            }

            layoutInflater.inflate(
                resource, null, false
            ).apply {
                val itemImg = findViewById<ImageView>(R.id.item_image_view)
                val itemTitle = findViewById<TextView>(R.id.item_title_tv)
                val itemBody = findViewById<TextView>(R.id.item_body_tv)

                when (checkInItem.type) {
                    EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT -> {
                        itemImg.setImageResource(R.drawable.ic_baseline_keyboard)
                        itemTitle.setText(R.string.check_in_item_manual_title)
                        itemBody.text = (secondArg)
                    }

                    EditCheckInItemDialog.CheckInItemType.VIRTUAL_CARD -> {
                        itemImg.setImageResource(R.drawable.ic_baseline_qr_code)
                        itemTitle.setText(R.string.check_in_item_virtual_title)
                        itemBody.text = (secondArg)
                    }

                    EditCheckInItemDialog.CheckInItemType.CUSTOM_ONE,
                    EditCheckInItemDialog.CheckInItemType.CUSTOM_TWO,
                    EditCheckInItemDialog.CheckInItemType.CUSTOM_THREE,
                    EditCheckInItemDialog.CheckInItemType.CUSTOM_FOUR -> {
                        itemImg.setImageResource(R.drawable.ic_baseline_how_to_reg)
                        itemTitle.text = checkInItem.title
                        itemBody.text = checkInItem.action
                    }

                    else -> {}
                }

                if (isShowInsertNHICardAnimation) {
                    binding.checkInLayoutHorizontal.isGone = true
                    binding.checkInLayout.isGone = false

                    binding.checkInLayout.addView(
                        this,
                        LinearLayoutCompat.LayoutParams(
                            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                            0,
                            1f
                        ).apply {
                            val margin =
                                resources.getDimension(R.dimen.distance_between_panel_x_1_5).toInt()

                            val bottomMargin = if (index == (itemList.size - 1)) 0 else margin
                            val leftMargin = if (itemList.isNotEmpty()) margin else 0

                            setMargins(leftMargin, 0, 0, bottomMargin)
                        }
                    )
                } else {
                    binding.checkInLayoutHorizontal.isGone = false
                    binding.checkInLayout.isGone = true

                    binding.checkInLayoutHorizontalRight.isGone = itemList.size == 1

                    when (itemList.size) {
                        1 -> {
                            binding.checkInLayoutHorizontalLeft.addView(
                                this,
                                LinearLayoutCompat.LayoutParams(
                                    LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                    0,
                                    1f
                                )
                            )
                        }

                        2 -> {
                            if ((index + 1) % 2 != 0) {
                                binding.checkInLayoutHorizontalLeft.addView(
                                    this,
                                    LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    ).apply {
                                        val margin =
                                            resources.getDimension(R.dimen.distance_between_panel_x_1_5)
                                                .toInt()

                                        setMargins(0, 0, margin, 0)
                                    }
                                )
                            } else {
                                binding.checkInLayoutHorizontalRight.addView(
                                    this,
                                    LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    ).apply {
                                        val margin =
                                            resources.getDimension(R.dimen.distance_between_panel_x_1_5)
                                                .toInt()

                                        setMargins(margin, 0, 0, 0)
                                    }
                                )
                            }
                        }

                        3 -> {
                            if (index == 0) {
                                binding.checkInLayoutHorizontalLeft.addView(
                                    this,
                                    LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    ).apply {
                                        val margin =
                                            resources.getDimension(R.dimen.distance_between_panel_x_1_5)
                                                .toInt()

                                        setMargins(0, 0, margin, 0)
                                    }
                                )
                            } else {
                                binding.checkInLayoutHorizontalRight.addView(
                                    this,
                                    LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    ).apply {
                                        val margin =
                                            resources.getDimension(R.dimen.distance_between_panel_x_1_5)
                                                .toInt()

                                        val bottomMargin = if (index == 1) margin else 0
                                        val topMargin = if (index == 2) margin else 0

                                        setMargins(margin, topMargin, 0, bottomMargin)
                                    }
                                )
                            }
                        }

                        4 -> {
                            if (index <= 1) {
                                binding.checkInLayoutHorizontalLeft.addView(
                                    this,
                                    LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    ).apply {
                                        val margin =
                                            resources.getDimension(R.dimen.distance_between_panel_x_1_5)
                                                .toInt()

                                        val bottomMargin = if (index == 0) margin else 0
                                        val topMargin = if (index == 1) margin else 0

                                        setMargins(0, topMargin, margin, bottomMargin)
                                    }
                                )
                            } else {
                                binding.checkInLayoutHorizontalRight.addView(
                                    this,
                                    LinearLayoutCompat.LayoutParams(
                                        LinearLayoutCompat.LayoutParams.MATCH_PARENT,
                                        0,
                                        1f
                                    ).apply {
                                        val margin =
                                            resources.getDimension(R.dimen.distance_between_panel_x_1_5)
                                                .toInt()

                                        val bottomMargin = if (index == 2) margin else 0
                                        val topMargin = if (index == 3) margin else 0

                                        setMargins(margin, topMargin, 0, bottomMargin)
                                    }
                                )
                            }
                        }
                    }
                }
                setOnClickListener {
                    when (checkInItem.type) {
                        EditCheckInItemDialog.CheckInItemType.MANUAL_INPUT -> {
                            findNavController().navigate(R.id.manualInputFragment)
                        }

                        EditCheckInItemDialog.CheckInItemType.VIRTUAL_CARD -> {
                            (requireActivity() as MainActivity).checkInWithVirtualCard()
                        }

                        EditCheckInItemDialog.CheckInItemType.CUSTOM_ONE,
                        EditCheckInItemDialog.CheckInItemType.CUSTOM_TWO,
                        EditCheckInItemDialog.CheckInItemType.CUSTOM_THREE,
                        EditCheckInItemDialog.CheckInItemType.CUSTOM_FOUR -> {
                            viewModel.userClickOnCustomizedButton(checkInItem)
                            (requireActivity() as MainActivity).createFakeAppointment(
                                schedule = GetScheduleResponse.ScheduleBean(
                                    doctor = checkInItem.doctorId,
                                    division = checkInItem.divisionId
                                )
                            )
                        }

                        else -> {}
                    }
                }
            }
        }

        changeIsCheckInLayoutWeightIfItEmpty(itemList.size)
    }

    private fun changeIsCheckInLayoutWeightIfItEmpty(size: Int) {

        val endMarginForLeftBlock =
            if (size == 0) 0 else (resources.getDimension(R.dimen.distance_between_panel_x_1_5) / resources.displayMetrics.density).toInt()

        val weight = if (size == 0) 0f else 2f

        val oldSize = binding.checkInLayout.childCount

        if (oldSize > 0 && size > 0)
            return

        val rightPartParams = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            weight
        )

        binding.checkInLayout.layoutParams = rightPartParams

        val leftPartParams = LinearLayoutCompat.LayoutParams(
            0,
            LinearLayoutCompat.LayoutParams.MATCH_PARENT,
            3f
        )

        leftPartParams.setMargins(0, 0, endMarginForLeftBlock, 0)

        binding.insertNhiCardAnimation.layoutParams = leftPartParams
    }

    private fun showTextInputDialog(
        titleResId: Int,
        inputTextLabel: String,
        inputText: String = "",
        hint: String = "",
        inputType: Int = InputType.TYPE_CLASS_TEXT,
        showDescription: Boolean = false,
        positiveButtonTextResId: Int = R.string.dialog_save_button,
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
        dialog.dialog_save_btn.setText(positiveButtonTextResId)

        if (showDescription) {
            dialog.dialog_description.visibility = View.VISIBLE
        }

        dialog.dialog_cancel_btn.setOnClickListener {
            dialog.dismiss()
        }

        dialog.dialog_save_btn.setOnClickListener {

            val inputData = dialog.dialog_input_field.editText?.text.toString().trim()

            onConfirmClick(inputData)

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


    override fun onDestroyView() {
        super.onDestroyView()

        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .unregisterReceiver(reloadClinicLogoReceiver)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .unregisterReceiver(reloadRightCardViewReceiver)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .unregisterReceiver(reloadTitle)
        LocalBroadcastManager.getInstance(requireContext().applicationContext)
            .unregisterReceiver(reloadPresentCardTextReceiver)

        _binding = null
    }
}