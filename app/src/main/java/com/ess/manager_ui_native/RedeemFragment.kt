package com.ess.manager_ui_native

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.ess.manager_ui_native.database.LotteryCardDatabase
import com.ess.manager_ui_native.models.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import org.json.JSONObject
import net.nyx.printerservice.print.IPrinterService
import net.nyx.printerservice.print.PrintTextFormat


class RedeemFragment : Fragment() {
    private val redeemViewModel: RedeemViewModel by activityViewModels()
    private var banks: List<Bank> = listOf()
    private lateinit var banksTextView: AutoCompleteTextView
    private lateinit var accountNumberInput: TextInputEditText
    private lateinit var accountNumberTextInputLayout: TextInputLayout
    private lateinit var cancelButton: Button
    private lateinit var verifyButton: Button
    private lateinit var redeemButton: Button
    private var selectedBankCode = ""
    private lateinit var requestManager: RequestManager
    private var beneficiaryData = BeneficiaryData("", CASH_CODE, "", "")
    private val gson = Gson()

    private val handler = Looper.myLooper()?.let { Handler(it) }
    private lateinit var printerService: IPrinterService
    lateinit var lotteryCardDatabase: LotteryCardDatabase
    private val connService: ServiceConnection = object : ServiceConnection {
        override fun onServiceDisconnected(name: ComponentName) {
            Log.d(TAG, "onServiceDisconnected: printer service disconnected, try reconnect")
            handler?.postDelayed({ bindService() }, 5000)
        }

        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            Log.d(TAG, "onServiceConnected: %s $name")
            printerService = IPrinterService.Stub.asInterface(service)
        }
    }

    private fun bindService() {
        Log.i(TAG, "bindService: ")
        val intent = Intent()
        intent.setPackage("net.nyx.printerservice")
        intent.action = "net.nyx.printerservice.IPrinterService"
        requireActivity().bindService(intent, connService, Context.BIND_AUTO_CREATE)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requestManager =
            RequestManager.getInstance(
                requireContext()
            )
        banks = redeemViewModel.banks.value!!
        bindService()
        return inflater.inflate(R.layout.fragment_redeem, container, false)
    }

    override fun onDestroy() {
        requireActivity().unbindService(connService)
        super.onDestroy()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireView().apply {
            banksTextView = findViewById(R.id.select_bank_edit_input)
            accountNumberInput = findViewById(R.id.account_number_edit_input)
            accountNumberTextInputLayout = findViewById(R.id.account_number_til)
            cancelButton = findViewById(R.id.cancel_button)
            redeemButton = findViewById(R.id.redeem_button)
            verifyButton = findViewById(R.id.verify_button)
        }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, banks.map { it.bankName })
        banksTextView.setAdapter(adapter)
        banksTextView.setOnItemClickListener { _, _, position, id ->
            Log.d(TAG, "onViewCreated: position $position $id id")
            selectedBankCode = banks[position].bankCode //TODO
            if (selectedBankCode == CASH_CODE) {
                accountNumberTextInputLayout.visibility = View.INVISIBLE
                verifyButton.visibility = View.INVISIBLE
                redeemButton.visibility = View.VISIBLE
            } else {
                accountNumberTextInputLayout.visibility = View.VISIBLE
                verifyButton.visibility = View.VISIBLE
                redeemButton.visibility = View.INVISIBLE
            }
            banksTextView.dismissDropDown()
        }

        cancelButton.setOnClickListener { redeemViewModel.setScreen(RedeemScreen.Validate) }

        verifyButton.setOnClickListener {
            handleRequestAccountInfo(
                BeneficiaryData(
                    accountNumberInput.text.toString(),
                    selectedBankCode
                )
            )
        }

        redeemButton.setOnClickListener {
            onRedeemClicked()
        }

        lotteryCardDatabase = LotteryCardDatabase.getInstance(requireContext())
    }

    private fun onRedeemClicked() {
        redeemViewModel.cardSecurityLiveData.value?.let { cardSecurity ->

            val successfulRedeem = lotteryCardDatabase.updateCardToRedeemed(
                NetworkWriteTask(
                    0,
                    CardStatus.SETTLED.ordinal,
                    cardSecurity.serialNumber,
                    0,
                    cardSecurity,
                    beneficiaryData
                )
            )
            if (successfulRedeem) {
                printReceipt(cardSecurity)
                redeemViewModel.showFeedback("Your prize has been successfully redeemed")
                if (selectedBankCode != CASH_CODE) {
                    Handler(Looper.getMainLooper()).postDelayed(
                        {
                            printReceipt(cardSecurity)
                            redeemViewModel.showFeedback("Your prize has been successfully redeemed")
                        }, 4000
                    )
                }
            } else {
                requireContext().showToast("Error card can not be redeemed")
            }
        }
    }

    fun Context.showToast(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
    }

    private fun printReceipt(cardSecurity: CardSecurity) {
        Log.d(TAG, "printReceipt: $cardSecurity")
        printerService.printBitmap(
            BitmapFactory.decodeResource(
                resources,
                R.drawable.ess_logo
            ), 1, 1
        )
        printerService.printText("", PrintTextFormat().also { it.ali = 1 })
        printerService.printText(
            "Congratulations!!!",
            PrintTextFormat().also { it.ali = 1 })
        printerService.printText(
            "You are a Lucky Star",
            PrintTextFormat().also { it.ali = 1 })
        if (selectedBankCode == CASH_CODE) {
            printerService.printText(
                "Processed cash payment of N ${redeemViewModel.cardValue.value}",
                PrintTextFormat().also { it.ali = 1 })
        } else {
            printerService.printText(
                "You will receive N ${redeemViewModel.cardValue.value}",
                PrintTextFormat().also { it.ali = 1 })
            printerService.printText("By bank transfer", PrintTextFormat().also { it.ali = 1 })
        }

        printerService.printText("", PrintTextFormat().also { it.ali = 1 })
        printerService.printText("Ess Lottery", PrintTextFormat().also { it.ali = 1 })
        printerService.printText(
            "Address: 1/3 command bus stop ",
            PrintTextFormat().also { it.ali = 1 })
        printerService.printText(
            "Musaroq Super market",
            PrintTextFormat().also { it.ali = 1 })
        printerService.printText(
            "Phone number: 08137527579",
            PrintTextFormat().also { it.ali = 1 })
        printerService.printText("", PrintTextFormat().also { it.ali = 1 })
        printerService.printBarcode(cardSecurity.serialNumber, 300, 80, 2, 1)
        printerService.paperOut(80)
    }

    private fun handleRequestAccountInfo(beneficiaryData: BeneficiaryData) {
        requestManager.getCustomerAccountInfo(
            JSONObject(gson.toJson(beneficiaryData)),
            {
                val jsonResponse = JSONObject(gson.toJson(it.body?.string()))
                if (jsonResponse.getBoolean("status")) {
                    showConfirmationDialog(
                        true,
                        gson.fromJson(jsonResponse.getString("data"), KudaBankResponse::class.java)
                    )
                } else {
                    showConfirmationDialog(false)
                }

            },
            {
                if (it.code == RedeemActivity.NO_AUTH_CODE) {
                    redeemViewModel.logout()
                }
                showConfirmationDialog(false)
                Log.e(TAG, "error getting cards: $it")
            }
        )
    }

    private fun showConfirmationDialog(
        success: Boolean,
        vBeneficiaryData: KudaBankResponse? = null
    ) {
        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())

        if (success) {
            dialogBuilder.apply {
                setTitle(vBeneficiaryData?.beneficiaryName)
                setMessage("Confirm with the customer that the name above is correct")
                dialogBuilder.setPositiveButton("I've confirmed") { _, _ ->
                    accountNumberInput.focusable = View.NOT_FOCUSABLE
                    banksTextView.focusable - View.NOT_FOCUSABLE
                    verifyButton.visibility = View.INVISIBLE
                    redeemButton.visibility = View.VISIBLE
                    beneficiaryData = vBeneficiaryData!!.toBeneficiaryData()
                    val adapter = ArrayAdapter(requireContext(), R.layout.list_item, listOf(""))
                    banksTextView.setAdapter(adapter)
                }
            }
        } else {
            dialogBuilder.apply {
                setTitle("Cold not verify")
                setMessage("Cold not verify account info try again \n check the details and try again")
            }
        }
        dialogBuilder.setNegativeButton("Cancel") { _, _ -> }
        dialogBuilder.show()
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            RedeemFragment().apply {
                arguments = Bundle().apply {
                }
            }

        const val TAG = "RedeemFragment"
        private const val CASH_CODE = "0000000"
    }
}