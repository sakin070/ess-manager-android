package com.ess.manager_ui_native.models

data class NetworkWriteTask(
    val id: Int,
    val networkAction: Int,
    val serialNumber: String,
    val retries: Int,
    val cardSecurity: CardSecurity? = null,
    val beneficiaryData: BeneficiaryData? = null
)