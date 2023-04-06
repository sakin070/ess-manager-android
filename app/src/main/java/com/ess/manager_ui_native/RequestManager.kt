package com.ess.manager_ui_native

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import androidx.preference.PreferenceManager
import com.android.volley.*
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.nio.charset.StandardCharsets


class RequestManager(context: Context) {

    private val url = BuildConfig.API_URL
    private val sharedPref: SharedPreferences
    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(context.applicationContext)
    }
    private val client = OkHttpClient()
    val JSON: MediaType = "application/json; charset=utf-8".toMediaType()


    init {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun login(
        username: String, password: String, listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) {
        val base64 = "$username:$password".toBase64()
        val jsonObjReq = object : StringRequest(
            Method.POST,
            "$url/login",
            listener,
            errorListener
        ) {

            @Throws(AuthFailureError::class)
            override fun getHeaders() = mapOf("Authorization" to "Basic $base64")

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                with(sharedPref.edit()) {
                    putString(AUTH_KEY, response?.headers?.get(AUTH_KEY))
                    apply()
                }
                return super.parseNetworkResponse(response)
            }
        }
        requestQueue.add(jsonObjReq)
    }

    fun onCurrentUserIsAuthorized(
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) {
        val verifyAuthRequest = object : StringRequest(
            Method.GET,
            "$url/auth-ping",
            listener,
            errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders() = getAuthHeader()
        }
        requestQueue.add(verifyAuthRequest)
    }

    fun logout(
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) {
        val verifyAuthRequest = object : StringRequest(
            Method.POST,
            "$url/logout",
            listener,
            errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders() = getAuthHeader()
        }
        requestQueue.add(verifyAuthRequest)
    }

    fun getAuthHeader() = mapOf(
        AUTH_KEY to sharedPref.getString(
            AUTH_KEY, ""
        )
    )

    fun sell(
        serialNumber: String, listener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) {
        val verifyAuthRequest = object : StringRequest(
            Method.POST,
            "$url/card/sell?serialNumber=$serialNumber",
            listener,
            errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders() = getAuthHeader()
        }
        requestQueue.add(verifyAuthRequest)
    }

    fun getBankList(
        listener: Response.Listener<String>,
        errorListener: Response.ErrorListener? = null
    ) {
        val jsonObjReq = object : StringRequest(
            "$url/bank/bankList",
            listener,
            errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders() = getAuthHeader()
        }
        requestQueue.add(jsonObjReq)
    }

    fun getCustomerAccountInfo(
        accountInfo: JSONObject,
        onSuccess: (responseBody: okhttp3.Response) -> Unit,
        onError: (response: okhttp3.Response) -> Unit
    ) {
        val body: RequestBody = accountInfo.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url("url/bank/getCustomerAccountInfo")
            .post(body)
            .header(AUTH_KEY , getAuthKey())
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
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

    fun getAllActiveCards(
        onSuccess: (responseBody: okhttp3.Response) -> Unit,
        onError: (response: okhttp3.Response) -> Unit
    ) {
        val request = Request.Builder()
            .url("$url/card/active-cards")
            .header(AUTH_KEY , getAuthKey())
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.d(TAG, "onFailure: ${e.printStackTrace()}")
            }

            override fun onResponse(call: Call, response: okhttp3.Response) {
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
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ) {
        val jsonObjReq = object : JsonObjectRequest(
            Method.POST,
            "$url/card/settle",
            settlementInfo,
            listener,
            errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders() = getAuthHeader()
        }
        requestQueue.add(jsonObjReq)
    }

    fun getCardValue(
        cardSecurity: JSONObject,
        listener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ) {
        val jsonObjReq = object : JsonObjectRequest(
            Method.POST,
            "$url/card/getValue",
            cardSecurity,
            listener,
            errorListener
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders() = getAuthHeader()
        }
        requestQueue.add(jsonObjReq)
    }

    fun String.toBase64(): String {
        val data: ByteArray = this.toByteArray(StandardCharsets.UTF_8)
        return Base64.encodeToString(data, Base64.DEFAULT)
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
