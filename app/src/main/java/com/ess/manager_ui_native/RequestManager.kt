package com.ess.manager_ui_native

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class RequestManager(context: Context) {

    private val url = BuildConfig.API_URL
    private val sharedPref: SharedPreferences
    private val client = OkHttpClient()
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

    init {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun login(
        username: String, password: String,
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val body: RequestBody = "".toRequestBody(JSON)

        val request = Request.Builder()
            .url("$url/login")
            .header("Authorization", Credentials.basic(username, password))
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(response)
                    } else {
                        with(sharedPref.edit()) {
                            putString(AUTH_KEY, response.headers[AUTH_KEY])
                            apply()
                        }
                        onSuccess(response)
                    }
                }
            }
        })
    }

    fun onCurrentUserIsAuthorized(
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val request = Request.Builder()
            .url("$url/auth-ping")
            .header(AUTH_KEY , getAuthKey())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(response)
                    } else {
                        onSuccess(response)
                    }
                }
            }
        })
    }

    fun logout(
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val body: RequestBody = "".toRequestBody(JSON)

        val request = Request.Builder()
            .url("$url/logout")
            .header(AUTH_KEY , getAuthKey())
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(response)
                    } else {
                        onSuccess(response)
                    }
                }
            }
        })
    }

    fun sell(
        serialNumber: String,
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {

        val body: RequestBody = "".toRequestBody(JSON)

        val request = Request.Builder()
            .url("$url/card/sell?serialNumber=$serialNumber")
            .header(AUTH_KEY , getAuthKey())
            .post(body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(response)
                    } else {
                        onSuccess(response)
                    }
                }
            }
        })
    }

    fun getBankList(
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val request = Request.Builder()
            .url("$url/bank/bankList")
            .header(AUTH_KEY , getAuthKey())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(response)
                    } else {
                        onSuccess(response)
                    }
                }
            }
        })
    }

    fun getCustomerAccountInfo(
        accountInfo: JSONObject,
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val body: RequestBody = accountInfo.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("$url/bank/getCustomerAccountInfo")
            .post(body)
            .header(AUTH_KEY , getAuthKey())
            .build()

        val response = client.newCall(request).execute()
        response.use {
            if (!response.isSuccessful) {
                onError(response)
            } else {
                onSuccess(response)
            }
        }
    }

    fun getAllActiveCards(
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val request = Request.Builder()
            .url("$url/card/active-cards")
            .header(AUTH_KEY , getAuthKey())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError(response)
                    } else {
                        onSuccess(response)
                    }
                }
            }
        })
    }

    private fun getAuthKey(): String {
        var authKey = ""
        sharedPref.getString(AUTH_KEY, "")?.let {
            authKey = it
        }
        return authKey
    }

    fun settleCard(
        settlementInfo: JSONObject,
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val body = settlementInfo.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("$url/card/settle")
            .post(body)
            .header(AUTH_KEY , getAuthKey())
            .build()

        val response = client.newCall(request).execute()
        response.use {
            if (!response.isSuccessful) {
                onError(response)
            } else {
                onSuccess(response)
            }
        }
    }

    fun getCardValue(
        cardSecurity: JSONObject,
        onSuccess: (responseBody: Response) -> Unit,
        onError: (response: Response) -> Unit
    ) {
        val body = cardSecurity.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("$url/card/getValue")
            .post(body)
            .header(AUTH_KEY , getAuthKey())
            .build()

        val response = client.newCall(request).execute()
        response.use {
            if (!response.isSuccessful) {
                onError(response)
            } else {
                onSuccess(response)
            }
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: RequestManager? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: RequestManager(context).also {
                    INSTANCE = it
                }
            }

        const val AUTH_KEY = "X-Auth-Token"
        const val IS_PRIVILEGED_KEY = "isPrivileged"
        const val TAG = "RequestManager"
    }
}
