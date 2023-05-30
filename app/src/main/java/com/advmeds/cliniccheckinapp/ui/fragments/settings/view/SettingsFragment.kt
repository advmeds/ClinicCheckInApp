package com.advmeds.cliniccheckinapp.ui.fragments.settings.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.ListFragment
import androidx.navigation.fragment.findNavController
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.SettingsFragmentBinding

class SettingsFragment : ListFragment() {

    private var _binding: SettingsFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var toolbar: Toolbar

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

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            resources.getStringArray(R.array.setting_items)
        )

        listAdapter = adapter
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        super.onListItemClick(l, v, position, id)

        Log.d("check---", "onListItemClick: position - $position")
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}