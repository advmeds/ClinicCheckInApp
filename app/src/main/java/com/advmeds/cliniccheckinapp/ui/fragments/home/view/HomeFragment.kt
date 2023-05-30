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
import androidx.appcompat.app.AlertDialog
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
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.advmeds.cliniccheckinapp.repositories.SharedPreferencesRepo
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.ui.fragments.home.viewmodel.HomeViewModel
import com.advmeds.cliniccheckinapp.utils.showOnly
import kotlinx.android.synthetic.main.text_input_dialog.*


class HomeFragment : Fragment() {

    private lateinit var dialog: Dialog

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

            val inputTextLabel = requireContext().getString(R.string.password)

            showTextInputDialog(
                titleResId = R.string.advanced_settings,
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                inputTextLabel = inputTextLabel,
                positiveButtonTextResId = R.string.dialog_ok_button,
                onConfirmClick = {
                    if (it == viewModel.password)
                        findNavController().navigate(R.id.settingsFragment)
                    else
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.password_is_incorrect),
                            Toast.LENGTH_LONG
                        ).show()
                }
            )

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

        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadClinicLogoReceiver)
        LocalBroadcastManager.getInstance(requireContext())
            .unregisterReceiver(reloadRightCardViewReceiver)

        _binding = null
    }
}