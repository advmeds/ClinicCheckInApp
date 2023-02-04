package com.advmeds.cliniccheckinapp.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.ScheduleItemBinding
import com.advmeds.cliniccheckinapp.databinding.SchedulesFragmentBinding
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse

class ScheduleListDialogFragment(
    val schedules: List<GetScheduleResponse.ScheduleBean> = emptyList(),
    val onActionButtonClicked: ((GetScheduleResponse.ScheduleBean?) -> Unit)? = null
) : AppCompatDialogFragment() {
    private var _binding: SchedulesFragmentBinding? = null

    /** This property is only valid between onCreateView and onDestroyView. */
    private val binding get() = _binding!!

    private val checkedList: MutableList<GetScheduleResponse.ScheduleBean> = mutableListOf()

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

        _binding = SchedulesFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.root.setOnClickListener {
            dismiss()
        }

        val adapter = SchedulesAdapter(
            schedules = schedules,
            checkedList = checkedList,
            delegate = object : SchedulesAdapter.SchedulesAdapterDelegate {
                override fun onItemClicked(item: GetScheduleResponse.ScheduleBean) {
                    checkedList.clear()
                    checkedList.add(item)
                    binding.schedulesRecyclerView.adapter?.notifyDataSetChanged()
                }
            }
        )

        binding.schedulesRecyclerView.adapter = adapter

        binding.confirmButton.setOnClickListener {
            onActionButtonClicked?.let { it1 -> it1(checkedList.firstOrNull()) }
        }

        binding.cancelButton.setOnClickListener {
            onActionButtonClicked?.let { it1 -> it1(null) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }

    class SchedulesAdapter(
        val schedules: List<GetScheduleResponse.ScheduleBean> = emptyList(),
        var checkedList: List<GetScheduleResponse.ScheduleBean> = emptyList(),
        var delegate: SchedulesAdapterDelegate? = null
    ) : RecyclerView.Adapter<SchedulesAdapter.ScheduleViewHolder>() {

        /** 使用者觸發的交互動作 */
        interface SchedulesAdapterDelegate {

            /**
             * 點擊圖片時觸發該方法
             * @param item 被點擊的單詞
             */
            fun onItemClicked(item: GetScheduleResponse.ScheduleBean)
        }

        inner class ScheduleViewHolder(val binding: ScheduleItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: GetScheduleResponse.ScheduleBean) {
                val name = when(BuildConfig.BUILD_TYPE) {
                    "ptch" -> item.doctorName
                    else -> item.divisionName
                }

                binding.toggleButton.text = name
                binding.toggleButton.textOn = name
                binding.toggleButton.textOff = name
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder =
            ScheduleViewHolder(
                ScheduleItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            val schedule = schedules[position]
            holder.bind(schedule)
            holder.binding.toggleButton.isChecked = checkedList.contains(schedule)
            holder.binding.toggleButton.setOnClickListener {
                delegate?.onItemClicked(schedule)
            }
        }

        override fun getItemCount(): Int = schedules.size
    }
}