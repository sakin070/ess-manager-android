package com.ess.manager_ui_native.models

import com.ess.manager_ui_native.models.BeneficiaryData

data class KudaBankResponse(
    val beneficiaryAccountNumber: String,
    val beneficiaryName: String,
    val beneficiaryBankCode: String,
    val sessionID: String,
    val nameEnquiryID: String,
) {
    fun toBeneficiaryData() =
        BeneficiaryData(beneficiaryAccountNumber, beneficiaryBankCode, beneficiaryName, sessionID)
}
