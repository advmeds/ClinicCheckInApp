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
        /** 虛擬健保卡 */
        VIRTUAL_CARD,

        /** 客製化 */
        CUSTOM,
        /** Custom one */
        CUSTOM_ONE,
        /** Custom two */
        CUSTOM_TWO,
        /** Custom three */
        CUSTOM_THREE,
        /** Custom four */
        CUSTOM_FOUR;
    }

    @Serializable
    data class EditCheckInItem(
        /** 直接取號類別 */
        val type: CheckInItemType,
        /** 門診名稱 */
        var title: String = "",
        /** action of the custom block*/
        var action: String = "",
        /** 診別ID */
        var divisionId: String = "",
        /** 醫生ID */
        var doctorId: String = "",
        /** this field is responsible for showing the panel on the screen or not*/
        var isShow: Boolean = false
    )

    companion object {
        val getEmptyCheckInItem = listOf(
            EditCheckInItem(type = CheckInItemType.MANUAL_INPUT),
            EditCheckInItem(type = CheckInItemType.VIRTUAL_CARD),
            EditCheckInItem(type = CheckInItemType.CUSTOM_ONE),
            EditCheckInItem(type = CheckInItemType.CUSTOM_TWO),
            EditCheckInItem(type = CheckInItemType.CUSTOM_THREE),
            EditCheckInItem(type = CheckInItemType.CUSTOM_FOUR),
        )

        fun toObject(list: List<EditCheckInItem>) : EditCheckInItems {
            return EditCheckInItems(
                manualInput = list.find { it.type == CheckInItemType.MANUAL_INPUT }?: EditCheckInItem(type = CheckInItemType.MANUAL_INPUT),
                virtualCard = list.find { it.type == CheckInItemType.VIRTUAL_CARD }?: EditCheckInItem(type = CheckInItemType.VIRTUAL_CARD),
                customOne = list.find { it.type == CheckInItemType.CUSTOM_ONE }?: EditCheckInItem(type = CheckInItemType.CUSTOM_ONE),
                customTwo = list.find { it.type == CheckInItemType.CUSTOM_TWO }?: EditCheckInItem(type = CheckInItemType.CUSTOM_TWO),
                customThree = list.find { it.type == CheckInItemType.CUSTOM_THREE }?: EditCheckInItem(type = CheckInItemType.CUSTOM_THREE),
                customFour = list.find { it.type == CheckInItemType.CUSTOM_FOUR }?: EditCheckInItem(type = CheckInItemType.CUSTOM_FOUR),
            )
        }

        fun toList(checkIn: EditCheckInItems): List<EditCheckInItem> {
            return listOf(
                checkIn.manualInput,
                checkIn.virtualCard,
                checkIn.customOne,
                checkIn.customTwo,
                checkIn.customThree,
                checkIn.customFour,
            )
        }
    }

    @Serializable
    data class EditCheckInItems(
        val manualInput: EditCheckInItem =  EditCheckInItem(type = CheckInItemType.MANUAL_INPUT),
        val virtualCard: EditCheckInItem =  EditCheckInItem(type = CheckInItemType.VIRTUAL_CARD),
        val customOne: EditCheckInItem =  EditCheckInItem(type = CheckInItemType.CUSTOM_ONE),
        val customTwo: EditCheckInItem =  EditCheckInItem(type = CheckInItemType.CUSTOM_TWO),
        val customThree: EditCheckInItem =  EditCheckInItem(type = CheckInItemType.CUSTOM_THREE),
        val customFour: EditCheckInItem =  EditCheckInItem(type = CheckInItemType.CUSTOM_FOUR),
    )
}