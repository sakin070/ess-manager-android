package com.ess.manager_ui_native

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ess.manager_ui_native.database.LotteryCardDatabase
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText


class MainActivity : AppCompatActivity() {
    private lateinit var serialNumberInput: TextInputEditText
    private lateinit var sellButton: Button
    private lateinit var requestManager: RequestManager
    private lateinit var tabLayout: TabLayout
    lateinit var lotteryCardDatabase: LotteryCardDatabase
    private val redeemViewModel: RedeemViewModel by viewModels()

    private val qscReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "onReceive: $context Intent $intent")
            if (SCAN_ACTION == intent.action) {
                intent.getStringExtra("qsc")?.let {
                    serialNumberInput.setText(it)
                    handleSell()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        registerQscScanReceiver()
        supportActionBar?.title = getString(R.string.record_sale)
        requestManager = RequestManager.getInstance(this)
        serialNumberInput = findViewById(R.id.serial_number_edit_text)

        sellButton = findViewById(R.id.sell_button)
        sellButton.setOnClickListener { handleSell() }

        tabLayout = findViewById(R.id.tab_layout)
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.text == getString(R.string.verify)) {
                    startActivity(
                        Intent(this@MainActivity, RedeemActivity::class.java).setFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                        )
                    )
                    finish()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

        redeemViewModel.isLoggedIn.observe(this) {
            if (!it) {
                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                finish()
            }
        }
        lotteryCardDatabase = LotteryCardDatabase.getInstance(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                requestManager.logout({}, {})
                finish()
                startActivity(
                    Intent(
                        this,
                        LoginActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return true
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        unregisterReceiver(qscReceiver)
        super.onDestroy()
    }

    private fun handleSell() {
        if (lotteryCardDatabase.sellCard(serialNumberInput.text.toString())) {
            Toast.makeText(this, "Card sold", Toast.LENGTH_SHORT).show()
            serialNumberInput.setText("")
        } else {
            Toast.makeText(this, "Card can not be sold", Toast.LENGTH_LONG).show()
        }
    }

    private fun registerQscScanReceiver() {
        Log.d(TAG, "registerQscScanReceiver: ")
        val filter = IntentFilter()
        filter.addAction("com.android.NYX_QSC_DATA")
        registerReceiver(qscReceiver, filter)
    }

    private companion object {
        const val TAG = "MainActivity"
        const val SCAN_ACTION = "com.android.NYX_QSC_DATA"
    }
}

