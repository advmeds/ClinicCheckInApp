package com.advmeds.cliniccheckinapp.ui.home

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.advmeds.cliniccheckinapp.R
import com.advmeds.cliniccheckinapp.databinding.HomeFragmentBinding
import com.advmeds.cliniccheckinapp.ui.MainActivity
import com.advmeds.cliniccheckinapp.utils.showOnly
import okhttp3.HttpUrl

class HomeFragment : Fragment() {
    private val viewModel: HomeViewModel by viewModels()

    private lateinit var composeView: ComposeView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).also {
            composeView = it
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {
        val arg = getString(R.string.national_id)
        val text = String.format(getString(R.string.national_id_input_title), arg)

        val textStart = text.indexOf(arg)
        val textEnd = textStart + arg.length

        composeView.setContent {
            HomeScreen(
                onNextClick = {
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
                },
                onMadeRequest = {
                    makeGetPatientRequest(it)
                },
                input_title =
                buildAnnotatedString {
                    append(text.substring(0, textStart))
                    withStyle(style = SpanStyle(colorResource(id = R.color.colorPrimary))) {
                        append(text.substring(textStart, textEnd))
                    }
                },

                String.format(getString(R.string.national_id_input_hint), arg)
            )
        }
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
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .showOnly()
    }

    private fun makeGetPatientRequest(patient:String) {
        if (patient.isNotBlank()) {
            (requireActivity() as MainActivity).getPatients(patient)
        }
    }

    private fun Context.getDimensionFrom(attr: Int): Int {
        val typedValue = TypedValue()
        return if (this.theme.resolveAttribute(attr, typedValue, true))
            TypedValue.complexToDimensionPixelSize(typedValue.data, this.resources.displayMetrics)
        else 0
    }
}