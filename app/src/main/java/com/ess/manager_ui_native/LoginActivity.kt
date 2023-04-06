package com.ess.manager_ui_native

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.preference.PreferenceManager
import com.ess.manager_ui_native.RequestManager.Companion.IS_PRIVILEGED_KEY
import com.ess.manager_ui_native.database.LotteryCardDatabase
import com.ess.manager_ui_native.models.Merchant
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class LoginActivity : AppCompatActivity() {
    private lateinit var usernameInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var loadingTextView: TextView
    private lateinit var loginConstraintLayout: ConstraintLayout
    private lateinit var loginButton: Button
    private lateinit var requestManager: RequestManager
    lateinit var lotteryCardDatabase: LotteryCardDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_login)
        usernameInput = findViewById(R.id.username_edit_text)
        passwordInput = findViewById(R.id.password_edit_input)
        loginButton = findViewById(R.id.login_button)
        loginButton.setOnClickListener {
            handleLogin()
        }
        loadingTextView = findViewById(R.id.loading_tv)
        loginConstraintLayout = findViewById(R.id.loginContainer)
        requestManager =
            RequestManager.getInstance(this)
        requestManager.onCurrentUserIsAuthorized(
            {
                showSell()
            },
            {
                Log.e(TAG, "isCurrentUserAuth: failure error $it ")
            }
        )
        lotteryCardDatabase = LotteryCardDatabase.getInstance(this)
    }


    private fun handleLogin() {
        requestManager.login(
            usernameInput.text.toString(),
            passwordInput.text.toString(),
            {
                val authMerchant = Gson().fromJson(it, Merchant::class.java)
                Log.d(TAG, "handleLogin: $authMerchant")
                with(PreferenceManager.getDefaultSharedPreferences(this).edit()) {
                    putBoolean(
                        IS_PRIVILEGED_KEY, authMerchant.roles.isNotEmpty()
                    )
                    apply()
                }
                fetchData()
            },
            {
                Log.e(TAG, "handleLogin: failure error $it ")
                Log.d(TAG, "handleLogin: ${it?.networkResponse?.statusCode}")
                if (it?.networkResponse?.statusCode == RedeemActivity.NO_AUTH_CODE) {
                    Toast.makeText(this, "Username or password incorrect", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Internal error try again", Toast.LENGTH_SHORT).show()
                }
            }
        )

    }

    private fun showSell() {
        startActivity(Intent(this, MainActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()

    }

    private fun fetchData () {
        val lotteryCardListType = object : TypeToken<List<LotteryCard>>() {}.type
        requestManager.getAllActiveCards(
            { responseBody ->
//                showLoading()
                responseBody.body?.use {body ->
                    val activeCards: List<LotteryCard> =
                        Gson().fromJson(body.string(), lotteryCardListType)
                    activeCards.forEach { lotteryCardDatabase.insertCard(it) }
                }
                showSell()
            },
            {
                Log.e(TAG, "error getting cards: $it")
            }
        )
    }

    private fun showLoading() {
        loginConstraintLayout.visibility = View.GONE
        loadingTextView.visibility = View.VISIBLE
    }

    private companion object {
        const val TAG = "LoginActivity"
    }
}