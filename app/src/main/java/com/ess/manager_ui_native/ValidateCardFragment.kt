package com.ess.manager_ui_native

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.preference.PreferenceManager
import com.ess.manager_ui_native.database.LotteryCardDatabase
import com.ess.manager_ui_native.models.CardSecurity
import com.ess.manager_ui_native.models.CardStatus
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import org.json.JSONObject


class ValidateCardFragment : Fragment() {

    private lateinit var a1EditText: TextInputEditText
    private lateinit var a2EditText: TextInputEditText
    private lateinit var a3EditText: TextInputEditText
    private lateinit var a4EditText: TextInputEditText
    private lateinit var a5EditText: TextInputEditText
    private lateinit var securityCodeEditText: TextInputEditText
    private lateinit var serialNumberEditText: TextInputEditText
    private lateinit var validateButton: Button
    private lateinit var tabLayout: TabLayout
    private lateinit var requestManager: RequestManager
    private val gson = Gson()
    private val redeemViewModel: RedeemViewModel by activityViewModels()
    lateinit var lotteryCardDatabase: LotteryCardDatabase

    private val qscReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: $context Intent $intent")
            if (SCAN_ACTION == intent.action) {
                intent.getStringExtra("qsc")?.let {
                    serialNumberEditText.setText(it)
                    securityCodeEditText.requestFocus()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_validate_card, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestManager =
            RequestManager.getInstance(
                requireContext()
            )
        init()
        registerQscScanReceiver()
        addKeyListeners()
        lotteryCardDatabase = LotteryCardDatabase.getInstance(requireContext())
    }

    private fun init() {
        a1EditText = requireView().findViewById(R.id.a1_edit_input)
        a2EditText = requireView().findViewById(R.id.a2_edit_input)
        a3EditText = requireView().findViewById(R.id.a3_edit_input)
        a4EditText = requireView().findViewById(R.id.a4_edit_input)
        a5EditText = requireView().findViewById(R.id.a5_edit_input)
        serialNumberEditText = requireView().findViewById(R.id.serial_number_edit_text)
        securityCodeEditText = requireView().findViewById(R.id.security_coder_edit_text)
        validateButton = requireView().findViewById(R.id.validate_button)
        validateButton.setOnClickListener {
            handleValidateCard(
                CardSecurity(
                    a1EditText.text.toString(),
                    a2EditText.text.toString(),
                    a3EditText.text.toString(),
                    a4EditText.text.toString(),
                    a5EditText.text.toString(),
                    serialNumberEditText.text.toString(),
                    securityCodeEditText.text.toString()
                )
            )
        }
        tabLayout = requireView().findViewById(R.id.tab_layout)
        tabLayout.getTabAt(1)?.select()
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.text == getString(R.string.sales)) {
                    startActivity(Intent(requireContext(), MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    requireActivity().finish()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }
        })
    }

    private fun addKeyListeners() {
        val maxLength = 2
        a1EditText.requestFocus()
        a1EditText.setOnKeyListener { _, _, _ ->
            if (a1EditText.text.toString().length >= maxLength) {
                a2EditText.requestFocus()
            }
            false
        }
        a2EditText.setOnKeyListener { _, _, _ ->
            if (a2EditText.text.toString().length >= maxLength) {
                a3EditText.requestFocus()
            }
            false
        }
        a3EditText.setOnKeyListener { _, _, _ ->
            if (a3EditText.text.toString().length >= maxLength) {
                a4EditText.requestFocus()
            }
            false
        }
        a4EditText.setOnKeyListener { _, _, _ ->
            if (a4EditText.text.toString().length >= maxLength) {
                a5EditText.requestFocus()
            }
            false
        }
        a5EditText.setOnKeyListener { _, _, _ ->
            if (a5EditText.text.toString().length >= maxLength) {
                serialNumberEditText.requestFocus()
            }
            false
        }
    }

    private fun registerQscScanReceiver() {
        Log.d(TAG, "registerQscScanReceiver: ")
        val filter = IntentFilter()
        filter.addAction("com.android.NYX_QSC_DATA")
        requireContext().registerReceiver(qscReceiver, filter)
    }

    private fun handleValidateCard(cardSecurity: CardSecurity) {
        Log.d(TAG, "handleValidateCard: validating")
        val isPrivileged = PreferenceManager.getDefaultSharedPreferences(requireContext()).getBoolean(RequestManager.IS_PRIVILEGED_KEY, false)
        lotteryCardDatabase.getCardBySerialNumber(cardSecurity.serialNumber)?.let {lotteryCard ->
            var isValid = lotteryCard.status === CardStatus.SOLD
            isValid = isValid && lotteryCard.securityCode == cardSecurity.securityCode
            isValid = isValid && lotteryCard.a1 == cardSecurity.a1
            isValid = isValid && lotteryCard.a2 == cardSecurity.a2
            isValid = isValid && lotteryCard.a3 == cardSecurity.a3
            isValid = isValid && lotteryCard.a4 == cardSecurity.a4
            isValid = isValid && lotteryCard.a5 == cardSecurity.a5
            val maxRedeemable = if (isPrivileged) Int.MAX_VALUE else LIMITED_MAX_REDEEMABLE
            if (isValid && lotteryCard.value > 0 && lotteryCard.value <= maxRedeemable) {
                redeemViewModel.setCardSecurity(cardSecurity)
                redeemViewModel.setCardValue(lotteryCard.value)
                redeemViewModel.setScreen(RedeemScreen.Redeem)
            } else {
                redeemViewModel.showFeedback("Error validating card. \nContact head office for more details.")
            }
        }
    }

    override fun onDestroy() {
        requireContext().unregisterReceiver(qscReceiver)
        super.onDestroy()
    }

    companion object {
        @JvmStatic
        fun newInstance() = ValidateCardFragment()
        const val TAG = "ValidateCardFragment"
        const val SCAN_ACTION = "com.android.NYX_QSC_DATA"
        const val LIMITED_MAX_REDEEMABLE = 10000
    }
}