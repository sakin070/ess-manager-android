package com.ess.manager_ui_native

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.ess.manager_ui_native.database.LotteryCardDatabase
import com.ess.manager_ui_native.models.CardStatus
import com.ess.manager_ui_native.models.NetworkWriteTask
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

class NetworkWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    private val lotteryCardDatabase: LotteryCardDatabase
    private val requestManager: RequestManager

    init {
        lotteryCardDatabase = LotteryCardDatabase.getInstance(appContext)
        requestManager = RequestManager.getInstance(appContext)
    }

    override fun doWork(): Result {
        Log.d(TAG, "doing Work: ")
        val tasks = lotteryCardDatabase.networkWriteTasks()
        if (tasks.isEmpty()) {
            reloadLotteryCardData()
        } else {
            tasks.forEach { networkTask ->
                when (networkTask.networkAction) {
                    CardStatus.SOLD.ordinal -> {
                        sendSoldRequest(networkTask)
                    }
                    CardStatus.SETTLED.ordinal -> {
                        sendRedeemedRequest(networkTask)
                    }
                    else -> {
                        Log.e(TAG, "doWork: $networkTask")
                    }
                }
            }
        }

        return Result.success()
    }

    private fun reloadLotteryCardData () {
        val lotteryCardListType = object : TypeToken<List<LotteryCard>>() {}.type
        requestManager.getAllActiveCards(
            { responseBody ->
                lotteryCardDatabase.deleteLotteryCards()
                responseBody.body?.use {body ->
                    val responseStr = body.string()
                    Log.d(TAG, "reloadLotteryCardData: $responseStr")
                    val activeCards: List<LotteryCard> =
                        Gson().fromJson(responseStr, lotteryCardListType)
                    activeCards.forEach { lotteryCardDatabase.insertCard(it) }
                }
           },
            {
                if (it.code == RedeemActivity.NO_AUTH_CODE) {
                    logout()
                }
                Log.e(TAG, "error getting cards: $it")
            }
        )
    }

    private fun sendSoldRequest(networkTask: NetworkWriteTask) {
        Log.d(TAG, "sending SoldRequest: ")
        requestManager.sell(
            networkTask.serialNumber,
            {
                Log.d(TAG, "sold: $it")
                lotteryCardDatabase.deleteNetworkAction(networkTask.id)
            },
            {
                if (it?.networkResponse?.statusCode == RedeemActivity.NO_AUTH_CODE) {
                    logout()
                }
            }
        )
    }

    private fun logout() {
        applicationContext.startActivity(
            Intent(
                applicationContext,
                LoginActivity::class.java
            ).setFlags(FLAG_ACTIVITY_NEW_TASK)
        )
    }

    private fun sendRedeemedRequest(networkTask: NetworkWriteTask) {
        Log.d(TAG, "sending RedeemedRequest: ")
        requestManager.settleCard(
            JSONObject(
                Gson().toJson(
                    SettleCard(
                        networkTask.cardSecurity!!,
                        networkTask.beneficiaryData!!
                    )
                )
            ),
            {
                Log.d(TAG, "settled: $it")
                lotteryCardDatabase.deleteNetworkAction(networkTask.id)
            },
            {
                if (it?.networkResponse?.statusCode == RedeemActivity.NO_AUTH_CODE) {
                    logout()
                }
            }
        )
        // todo deleted cards after redeemption, add clean up task that reedeems all 0 val cards add job that cleans data after two months of exting on drive

    }

    companion object {
        private const val TAG = "NetworkWorker"
    }
}