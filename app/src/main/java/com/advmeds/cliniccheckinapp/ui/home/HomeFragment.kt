package com.advmeds.cliniccheckinapp.ui.home

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import coil.load
import com.advmeds.cliniccheckinapp.BuildConfig
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.dialog.CheckInDialogFragment
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.ui.MainViewModel
import com.advmeds.cliniccheckinapp.utils.NationIdTransformationMethod
import com.advmeds.cliniccheckinapp.utils.isNationId
import com.advmeds.cliniccheckinapp.utils.showOnly
import okhttp3.HttpUrl

class HomeFragment : Fragment() {
    companion object {
        const val RELOAD_CLINIC_LOGO_ACTION = "reload_clinic_logo_action"
        const val CLINIC_LOGO_URL_KEY = "clinic_logo_url"
    }

    private val viewModel: HomeViewModel by viewModels()
    private val activityViewModel: MainViewModel by activityViewModels()

    private var _binding: HomeFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val reloadClinicLogoReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val clinicLogoUrl = intent?.getStringExtra(CLINIC_LOGO_URL_KEY)
            binding.logoImageView.load(clinicLogoUrl)
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
            IntentFilter(RELOAD_CLINIC_LOGO_ACTION)
        )

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        binding.checkInLayout.visibility =
            if (BuildConfig.PRINT_ENABLED) View.VISIBLE else View.GONE

        binding.logoImageView.setOnLongClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle(R.string.setting)
                .setItems(R.array.setting_items) { _, index ->
                    when (index) {
                        0 -> {
                            onSetServerDomainItemClicked()
                        }
                        1 -> {
                            onSetOrgIDItemClicked()
                        }
                    }
                }
                .showOnly()

            return@setOnLongClickListener true
        }

        binding.checkInButton.setOnClickListener {
            val activity = requireActivity() as MainActivity
            activity.dialog?.dismiss()
            activity.dialog = CheckInDialogFragment()
            activity.dialog?.showNow(parentFragmentManager, null)
        }

        val arg = getString(R.string.national_id)
        val text = String.format(getString(R.string.national_id_input_title), arg)
        val textColor = ContextCompat.getColor(
            requireContext(),
            R.color.colorPrimary
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

        binding.idInputTitleTv.text = spannable
        binding.idInputEt.hint = String.format(getString(R.string.national_id_input_hint), arg)
        binding.idInputEt.transformationMethod = NationIdTransformationMethod()

        setupKeyboard()
    }

    private fun onSetServerDomainItemClicked() {
        val editText = EditText(requireContext())
        editText.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextAppearance(R.style.TextAppearance_AppCompat_Subhead)
        } else {
            editText.setTextAppearance(requireContext(), R.style.TextAppearance_AppCompat_Subhead)
        }
        editText.inputType =
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        editText.hint = "https://example.com"
        editText.setText(viewModel.mSchedulerServerDomain)

        val layout = LinearLayout(requireContext())
        val padding = requireContext().getDimensionFrom(R.attr.dialogPreferredPadding)
        layout.setPaddingRelative(padding, 0, padding, 0)
        layout.addView(editText)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.server_domain)
            .setView(layout)
            .setPositiveButton(
                R.string.confirm
            ) { _, _ ->
                val domain = editText.text.toString().trim()

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
            .setNegativeButton(R.string.cancel, null)
            .showOnly()
    }

    private fun onSetOrgIDItemClicked() {
        val editText = EditText(requireContext())
        editText.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            editText.setTextAppearance(R.style.TextAppearance_AppCompat_Subhead)
        } else {
            editText.setTextAppearance(requireContext(), R.style.TextAppearance_AppCompat_Subhead)
        }

        editText.setText(viewModel.orgId)

        val layout = LinearLayout(requireContext())
        val padding = requireContext().getDimensionFrom(R.attr.dialogPreferredPadding)
        layout.setPaddingRelative(padding, 0, padding, 0)
        layout.addView(editText)

        AlertDialog.Builder(requireContext())
            .setTitle(R.string.org_id)
            .setView(layout)
            .setPositiveButton(
                R.string.confirm
            ) { _, _ ->
                val id = editText.text.toString().trim()

                if (id.isNotBlank()) {
                    viewModel.orgId = id

                    val intent = Intent(MainActivity.RELOAD_CLINIC_DATA_ACTION)

                    LocalBroadcastManager.getInstance(requireContext())
                        .sendBroadcast(intent)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .showOnly()
    }

    private fun setupKeyboard() {
        val onKeyClicked = View.OnClickListener {
            val currentText = binding.idInputEt.text.toString()
            val key = (it as Button).text.toString()

            binding.idInputEt.setText(currentText + key)
        }

        binding.enPadLayout.children.forEach { children ->
            when (children) {
                is ViewGroup -> {
                    children.children.forEach {
                        if (it is Button) {
                            it.setOnClickListener(onKeyClicked)
                        }
                    }
                }
                is Button -> {
                    children.setOnClickListener(onKeyClicked)
                }
            }
        }

        binding.numberPadLayout.children.forEach { children ->
            when (children) {
                is ViewGroup -> {
                    children.children.forEach {
                        if (it is Button) {
                            it.setOnClickListener(onKeyClicked)
                        }
                    }
                }
                is Button -> {
                    children.setOnClickListener(onKeyClicked)
                }
            }
        }

        binding.backspaceButton.setOnClickListener {
            val currentText = binding.idInputEt.text.toString()

            binding.idInputEt.setText(currentText.dropLast(1))
        }

        binding.enterButton.setOnClickListener {
            val patient = binding.idInputEt.text.toString().trim()
            (requireActivity() as MainActivity).getPatients(patient) {
                binding.idInputEt.text = null
            }
        }
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

        _binding = null
    }
}