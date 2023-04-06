package com.ess.manager_ui_native.models

data class BeneficiaryData(
    val account: String,
    val bankCode: String,
    val name: String = "",
    val nameEnquiryId: String = ""
)