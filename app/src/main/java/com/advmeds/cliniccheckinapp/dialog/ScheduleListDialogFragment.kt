package com.advmeds.cliniccheckinapp.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.AutomaticAppointmentDoctorItemBinding
import com.advmeds.cliniccheckinapp.databinding.AutomaticAppointmentDoctorListDialogFragmentBinding
import com.advmeds.cliniccheckinapp.models.remote.mScheduler.response.GetScheduleResponse
import com.bumptech.glide.Glide

private const val MAX_COUNTER_DOWN_VALUE = 60

class ScheduleListDialogFragment(
    val schedules: List<GetScheduleResponse.ScheduleBean> = emptyList(),
    val currentLanguage: String,
    val onActionButtonClicked: ((GetScheduleResponse.ScheduleBean?) -> Unit)? = null
) : AppCompatDialogFragment() {
    private var _binding: AutomaticAppointmentDoctorListDialogFragmentBinding? = null

    /** This property is only valid between onCreateView and onDestroyView. */
    private val binding get() = _binding!!

    private val handler = Handler(Looper.getMainLooper())
    private var countDownRunnable: Runnable? = null
    private var progress: Int = MAX_COUNTER_DOWN_VALUE
    private val token = "TOKEN_FOR_HANDLER"
    private var selectedItem: GetScheduleResponse.ScheduleBean? = null

    private lateinit var adapter: SchedulesAdapter

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

        _binding =
            AutomaticAppointmentDoctorListDialogFragmentBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = SchedulesAdapter(
            context = requireContext(),
            schedules = schedules,
            currentLanguage = currentLanguage,
            delegate = object : SchedulesAdapter.SchedulesAdapterDelegate {
                override fun onItemClicked(item: GetScheduleResponse.ScheduleBean) {
                    selectedItem = item
                    restartCounter()

                    binding.listContainer.isGone = true
                    binding.detailContainer.isGone = false
                    binding.fragmentTitleTv.setText(R.string.automatic_appointment_title_detail)

                    val name = when (currentLanguage) {
                        "en" -> item.doctorAlias
                        "zh" -> item.doctorName
                        else -> item.doctorName
                    }

                    binding.detailDoctorName.text = name
                    binding.detailDivisionName.text = item.divisionName

                    Glide.with(requireContext())
                        .load(item.doctorPhoto)
                        .placeholder(R.drawable.doctor_photo_placeholder)
                        .circleCrop()
                        .into(binding.detailImageView)
                }
            }
        )

        val linearLayoutManager = LinearLayoutManager(requireContext())
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        binding.schedulesRecyclerView.layoutManager = linearLayoutManager

        val marginInPixels = resources.getDimensionPixelSize(R.dimen.space_medium)
        binding.schedulesRecyclerView.addItemDecoration(MarginItemDecoration(marginInPixels))

        binding.schedulesRecyclerView.adapter = adapter

        binding.confirmButton.setOnClickListener {
            onActionButtonClicked?.let { it1 -> it1(selectedItem) }
        }

        binding.cancelButton.setOnClickListener {
            onActionButtonClicked?.let { it1 -> it1(null) }
        }

        binding.redoButton.setOnClickListener {
            selectedItem = null
            restartCounter()

            binding.listContainer.isGone = false
            binding.detailContainer.isGone = true
            binding.fragmentTitleTv.setText(R.string.automatic_appointment_title_list)

            binding.detailDoctorName.text = ""
            binding.detailDivisionName.text = ""
            binding.detailImageView.setImageResource(R.drawable.doctor_photo_placeholder)
        }

        setUpCounter()
    }

    private fun setUpCounter() {
        countDownRunnable = object : Runnable {
            override fun run() {
                updateTextView()
                if (progress > 0) {
                    progress--

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        handler.postDelayed(this, token, 1000)
                    } else {
                        handler.postDelayed(this, 1000)
                    }
                } else {
                    onActionButtonClicked?.let {  it1 -> it1(null) }
                }
            }
        }
        countDownRunnable?.let {
            handler.post(it)
        } ?: setUpCounter()
    }

    private fun restartCounter() {
        progress = MAX_COUNTER_DOWN_VALUE
    }

    private fun stopCounter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            handler.removeCallbacksAndMessages(token)
        } else {
            countDownRunnable?.let {
                handler.removeCallbacks(it)
            }
        }
    }

    private fun updateTextView() {
        val cancelText =
            "${requireContext().getText(R.string.automatic_appointment_cancel)} (${progress}s)"
        binding.cancelButton.text = cancelText
    }

    override fun onDestroyView() {
        super.onDestroyView()

        stopCounter()

        _binding = null
    }

    class SchedulesAdapter(
        val context: Context,
        val schedules: List<GetScheduleResponse.ScheduleBean> = emptyList(),
        var currentLanguage: String,
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

        inner class ScheduleViewHolder(val binding: AutomaticAppointmentDoctorItemBinding) :
            RecyclerView.ViewHolder(binding.root) {

            fun bind(item: GetScheduleResponse.ScheduleBean) {
                val name = when (currentLanguage) {
                    "en" -> item.doctorAlias
                    "zh" -> item.doctorName
                    else -> item.doctorName
                }

                val statusResColor = when (item.status) {
                    0 -> R.color.doctor_list_dialog_success
                    else -> R.color.doctor_list_dialog_gray_400
                }

                val color = ResourcesCompat.getColor(
                    context.resources,
                    statusResColor,
                    null
                )

                val statusText = when (item.status) {
                    0 -> R.string.scheduler_dialog_status_appointable
                    1 -> R.string.scheduler_dialog_status_closed
                    2 -> R.string.scheduler_dialog_status_full
                    3 -> R.string.scheduler_dialog_status_stop_appoint
                    else -> R.string.scheduler_dialog_status_stop_appoint
                }

                binding.doctorName.text = name
                binding.divisionName.text = item.divisionName
                binding.status.setText(statusText)
                binding.status.setBackgroundColor(color)

                Glide.with(context)
                    .load(item.doctorPhoto)
                    .placeholder(R.drawable.doctor_photo_placeholder)
                    .circleCrop()
                    .into(binding.imageView)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder =
            ScheduleViewHolder(
                AutomaticAppointmentDoctorItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

        override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
            val schedule = schedules[position]
            holder.bind(schedule)

            if (schedule.status == 0) {
                holder.binding.automaticAppointmentItemCardView.setOnClickListener {
                    delegate?.onItemClicked(schedule)
                }
            }
        }

        override fun getItemCount(): Int = schedules.size
    }

    class MarginItemDecoration(private val margin: Int) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position = parent.getChildAdapterPosition(view)
            if (position == 0) {
                outRect.left = margin
            }
        }
    }
}