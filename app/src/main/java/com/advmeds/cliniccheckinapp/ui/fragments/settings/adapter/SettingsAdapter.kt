package com.advmeds.cliniccheckinapp.ui.fragments.settings.adapter

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.advmeds.cliniccheckinapp.R

class SettingsAdapter(val mContext: Context, val settingItems: Array<String>) :
    ArrayAdapter<String>(mContext, R.layout.settings_fragment_list_item, settingItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        val view = super.getView(position, convertView, parent)

        if (view is TextView) {
            if (settingItems[position] == mContext.getString(R.string.exit_app)) {
                view.setTextColor(Color.RED)
            } else {
                view.setTextColor(Color.BLACK)
            }
        }

        return view
    }

}