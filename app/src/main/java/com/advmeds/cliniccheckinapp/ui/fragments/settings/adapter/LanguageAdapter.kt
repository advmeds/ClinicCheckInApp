package com.advmeds.cliniccheckinapp.ui.fragments.settings.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.ui.fragments.settings.model.LanguageModel

class LanguageAdapter(
    val itemList: Array<LanguageModel>,
    private val onClickListener: OnClickListener,
    private var selectedItemPosition: Int = RecyclerView.NO_POSITION
) :
    RecyclerView.Adapter<LanguageAdapter.LanguageViewHolder>() {

    private var previousSelectedItemPosition = RecyclerView.NO_POSITION

    init {
        for (i in itemList.indices) {
            if (itemList[i].isSelected) {
                selectedItemPosition = i
                break
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LanguageViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.language_setting_item, parent, false)
        return LanguageViewHolder(view)
    }

    override fun onBindViewHolder(holder: LanguageViewHolder, position: Int) {
        holder.languageTextView.text = itemList[position].name
        holder.isCheckedImageView.visibility =
            if (itemList[position].isSelected) View.VISIBLE else View.INVISIBLE
        holder.itemView.setOnClickListener {

            val clickedPosition = holder.adapterPosition

            if (selectedItemPosition != clickedPosition) {
                onClickListener.onClick(itemList[clickedPosition].name)
                previousSelectedItemPosition = selectedItemPosition
                selectedItemPosition = clickedPosition

                itemList[selectedItemPosition].isSelected = true
                itemList[previousSelectedItemPosition].isSelected = false

                notifyItemChanged(previousSelectedItemPosition)
                notifyItemChanged(selectedItemPosition)
            }
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    inner class LanguageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var languageTextView: TextView
        var isCheckedImageView: ImageView

        init {

            languageTextView = itemView.findViewById(R.id.language) as TextView
            isCheckedImageView = itemView.findViewById(R.id.is_selected) as ImageView

        }
    }

    class OnClickListener(val clickListener: (language: String) -> Unit) {
        fun onClick(language: String) = clickListener(language)
    }
}