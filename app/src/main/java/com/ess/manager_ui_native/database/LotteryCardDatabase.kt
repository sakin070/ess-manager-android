package com.ess.manager_ui_native.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import android.widget.Toast
import com.ess.manager_ui_native.LotteryCard
import com.ess.manager_ui_native.RequestManager
import com.ess.manager_ui_native.models.BeneficiaryData
import com.ess.manager_ui_native.models.CardSecurity
import com.ess.manager_ui_native.models.CardStatus
import com.ess.manager_ui_native.models.NetworkWriteTask
import java.util.LinkedList

private const val DATABASENAME = "ess_db"
const val lottery_card_table = "lottery_card"
const val a1 = "a1"
const val a2 = "a2"
const val a3 = "a3"
const val a4 = "a4"
const val a5 = "a5"
const val security_code = "security_code"
const val serial_number = "serial_number"
const val status = "status"
const val value = "value"
const val network_task = "network_task"
const val action_id = "action_id"
const val account_number = "account_number"
const val bank_code = "bank_code"
const val name = "name"
const val nameEnquiryId = "nameEnquiryId"
const val retries = "retries"


const val createCardsTable =
    "CREATE TABLE IF NOT EXISTS $lottery_card_table ( $a1 varchar(255) not null, $a2 varchar(255) not null, $a3 varchar(255) not null, $a4 varchar(255) not null, $a5 varchar(255) not null, $security_code varchar(255) not null, $serial_number varchar(255) PRIMARY KEY, $status int not null, $value int not null )"

const val createNetworkTaskTable =
    "CREATE TABLE IF NOT EXISTS $network_task ( id INTEGER PRIMARY KEY AUTOINCREMENT, $retries INTEGER, $action_id INTEGER, $account_number VARCHAR(256), $bank_code VARCHAR(256),$name VARCHAR(256), $nameEnquiryId VARCHAR(256), $a1 varchar(255) not null, $a2 varchar(255) not null, $a3 varchar(255) not null, $a4 varchar(255) not null, $a5 varchar(255) not null, $security_code varchar(255) not null, $serial_number varchar(255))"

class LotteryCardDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context, DATABASENAME, null, 1
) {
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createCardsTable)
        db?.execSQL(createNetworkTaskTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun insertCard(card: LotteryCard) {
        val database = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(serial_number, card.serialNumber)
        contentValues.put(security_code, card.securityCode)
        contentValues.put(status, card.status.ordinal)
        contentValues.put(value, card.value)
        contentValues.put(a1, card.a1)
        contentValues.put(a2, card.a2)
        contentValues.put(a3, card.a3)
        contentValues.put(a4, card.a4)
        contentValues.put(a5, card.a5)
        database.insert(lottery_card_table, null, contentValues)
    }

    fun sellCard(serialNumber: String): Boolean {
        val db = this.writableDatabase
        db.query(
            true, lottery_card_table, arrayOf(
                a1, a2, a3, a4, a5, serial_number, security_code, status, value
            ), "$serial_number = ?", arrayOf(serialNumber), null, null, null, null
        ).use { result ->
            if (result.moveToFirst() && result.count == 1) {
                val cardStatus = result.getInt(7)
                if (CardStatus.fromInt(cardStatus) == CardStatus.ASSIGNED) {
                    val contentValues = ContentValues()
                    contentValues.put(status, sold_id)
                    val rowsUpdated = db.update(
                        lottery_card_table,
                        contentValues,
                        "$serial_number = ?",
                        arrayOf(serialNumber)
                    )
                    if (rowsUpdated == 1) {
                        val networkContentValues = ContentValues()
                        networkContentValues.put(serial_number, serialNumber)
                        networkContentValues.put(action_id, CardStatus.SOLD.ordinal)
                        networkContentValues.put(retries, 0)
                        networkContentValues.put(a1, "")
                        networkContentValues.put(a2, "")
                        networkContentValues.put(a3, "")
                        networkContentValues.put(a4, "")
                        networkContentValues.put(a5, "")
                        networkContentValues.put(security_code, "")
                        networkContentValues.put(account_number, "")
                        networkContentValues.put(bank_code, "")
                        networkContentValues.put(name, "")
                        networkContentValues.put(nameEnquiryId, "")
                        return db.insert(network_task, null, networkContentValues) >= 0
                    }
                }
            }
            return false
        }
    }

    fun updateCardToRedeemed(networkWriteTask: NetworkWriteTask): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(status, CardStatus.SETTLED.ordinal)
        val rowsUpdated = db.update(
            lottery_card_table,
            contentValues,
            "$serial_number = ?",
            arrayOf(networkWriteTask.serialNumber)
        )
        if (rowsUpdated == 1) {
            val networkContentValues = ContentValues()

            networkContentValues.put(action_id, CardStatus.SETTLED.ordinal)
            networkContentValues.put(serial_number, networkWriteTask.serialNumber)
            networkContentValues.put(retries, 0)
            networkWriteTask.cardSecurity?.let {
                networkContentValues.put(a1, it.a1)
                networkContentValues.put(a2, it.a2)
                networkContentValues.put(a3, it.a3)
                networkContentValues.put(a4, it.a4)
                networkContentValues.put(a5, it.a5)
                networkContentValues.put(security_code, it.securityCode)
            }
            networkWriteTask.beneficiaryData?.let {
                networkContentValues.put(account_number, it.account)
                networkContentValues.put(bank_code, it.bankCode)
                networkContentValues.put(name, it.name)
                networkContentValues.put(nameEnquiryId, it.nameEnquiryId)
            }

            return db.insert(network_task, null, networkContentValues) >= 0
        }
        return false
    }

    fun networkWriteTasks(): List<NetworkWriteTask> {
        val taskList = LinkedList<NetworkWriteTask>()
        Log.d(TAG, "networkWriteTasks: ")
        val db = this.readableDatabase
        db.query(
            network_task, arrayOf(
                "id",
                action_id,
                serial_number,
                retries,
                a1,
                a2,
                a3,
                a4,
                a5,
                security_code,
                account_number,
                bank_code,
                name,
                nameEnquiryId
            ), null, null, null, null, "id"
        ).use { cursor ->

            if (cursor.moveToFirst()) {
                do {
                    if (cursor.getInt(1) == CardStatus.SOLD.ordinal) { // status sold
                        taskList.add(
                            NetworkWriteTask(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getString(2),
                                cursor.getInt(3)
                            )
                        )
                    } else if (cursor.getInt(1) == CardStatus.SETTLED.ordinal) { //status settled
                        taskList.add(
                            NetworkWriteTask(
                                cursor.getInt(0),
                                cursor.getInt(1),
                                cursor.getString(2),
                                cursor.getInt(3),
                                CardSecurity(
                                    cursor.getString(4),
                                    cursor.getString(5),
                                    cursor.getString(6),
                                    cursor.getString(7),
                                    cursor.getString(8),
                                    cursor.getString(2),
                                    cursor.getString(9)
                                ),
                                BeneficiaryData(
                                    cursor.getString(10),
                                    cursor.getString(11),
                                    cursor.getString(12),
                                    cursor.getString(13)
                                )
                            )
                        )
                    }
                } while (cursor.moveToNext())

            }
        }
        return taskList
    }

    fun deleteNetworkAction(id: Int): Boolean {
        val db = this.writableDatabase
        return db.delete(network_task, "id = ?", arrayOf(id.toString())) == 1
    }

    fun deleteLotteryCards() {
        val db = this.writableDatabase
        db.delete(lottery_card_table, null, null)
    }

    fun getCardBySerialNumber(serialNumber: String): LotteryCard? {
        val db = this.writableDatabase
        db.query(
            true, lottery_card_table, arrayOf(
                a1, a2, a3, a4, a5, serial_number, security_code, value, status
            ), "$serial_number = ?", arrayOf(serialNumber), null, null, null, null
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                return LotteryCard(
                    cursor.getString(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getString(5),
                    cursor.getString(6),
                    cursor.getInt(7),
                    CardStatus.fromInt(cursor.getInt(8))
                )
            }
        }
        return null
    }

    companion object {
        const val sold_id = 2
        const val TAG = "LotteryCardDatabase"
        private var INSTANCE: LotteryCardDatabase? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: LotteryCardDatabase(context).also {
                    INSTANCE = it
                }
            }
    }
}