package com.ess.manager_ui_native

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import com.ess.manager_ui_native.models.Bank
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class RedeemActivity : AppCompatActivity() {
    // todo add merchant id and date to receipt
    // todo make transfer and cash very visible

    private lateinit var requestManager: RequestManager
    private val redeemViewModel: RedeemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_redeem)
        requestManager =
            RequestManager.getInstance(this)
        supportActionBar?.title = "Validate"

        supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container_view, ValidateCardFragment.newInstance()).commit()
        redeemViewModel.redeemScreen.observe(this) {
            when (it) {
                RedeemScreen.Validate -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, ValidateCardFragment.newInstance())
                        .commit()
                }
                RedeemScreen.Redeem -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, RedeemFragment.newInstance())
                        .commit()
                }
                else -> {}
            }
        }

        redeemViewModel.feedbackText.observe(this) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, FeedbackFragment.newInstance(it)).commit()
        }
        redeemViewModel.isLoggedIn.observe(this) {
            if (!it) {
                startActivity(Intent(this, LoginActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                finish()
            }
        }
        getBanks()
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

    private fun getBanks() {
        val bankListType = object : TypeToken<List<Bank>>() {}.type
        requestManager.getBankList(
            {
                redeemViewModel.setBanks(Gson().fromJson(it.body?.string(), bankListType))
            },
            {
                if (it.code == NO_AUTH_CODE) {
                    redeemViewModel.logout()
                }

                Log.d(TAG, "getBanks: $it")
            })
    }


    companion object {
        private const val TAG = "RedeemActivity"
        const val NO_AUTH_CODE = 401
    }
}
