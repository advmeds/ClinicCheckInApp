package com.advmeds.cliniccheckinapp.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.view.isGone
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.EditCheckInItemFragmentBinding
import kotlinx.serialization.Serializable


class EditCheckInItemDialog(
    val onConfirmClick: (EditCheckInItem) -> Unit
) : AppCompatDialogFragment() {
    private var _binding: EditCheckInItemFragmentBinding? = null
    /** This property is only valid between onCreateView and onDestroyView. */
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(
            STYLE_NORMAL,
            R.style.MyApp_DialogFragmentTheme
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        dialog?.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.argb(
                    (255 * 0.2).toInt(),
                    0,
                    0,
                    0
                )
            )
        )

        _binding = EditCheckInItemFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.typeRadioGroup.setOnCheckedChangeListener { _, i ->
            binding.editTitleTl.isGone = i != R.id.custom_radio_button
            binding.editDoctorTl.isGone = i != R.id.custom_radio_button
            binding.editRoomTl.isGone = i != R.id.custom_radio_button
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.confirmButton.setOnClickListener {
            onConfirmClick(
                EditCheckInItem(
                    type = when(binding.typeRadioGroup.checkedRadioButtonId) {
                        R.id.manual_radio_button -> CheckInItemType.MANUAL_INPUT
                        R.id.custom_radio_button -> CheckInItemType.CUSTOM
                        R.id.virtual_radio_button -> CheckInItemType.VIRTUAL_CARD
                        else -> CheckInItemType.MANUAL_INPUT
                    },
                    title = binding.editTitleEt.text.toString(),
                    divisionId = binding.editRoomEt.text.toString(),
                    doctorId = binding.editDoctorEt.text.toString()
                )
            )

            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    /** 直接取號類別 */
    @Serializable
    enum class CheckInItemType {
        /** 手動輸入身分證字號 */
        MANUAL_INPUT,
        /** 客製化 */
        CUSTOM,
        /** 虛擬健保卡 */
        VIRTUAL_CARD;
    }

    @Serializable
    data class EditCheckInItem(
        /** 直接取號類別 */
        val type: CheckInItemType,
        /** 門診名稱 */
        val title: String = "",
        /** 診別ID */
        val divisionId: String = "",
        /** 醫生ID */
        val doctorId: String = ""
    )
}