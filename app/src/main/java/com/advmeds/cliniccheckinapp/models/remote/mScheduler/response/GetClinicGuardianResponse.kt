package com.advmeds.cliniccheckinapp.models.remote.mScheduler.response

import kotlinx.serialization.Serializable

@Serializable
data class GetClinicGuardianResponse(
    /** API 執行是否成功 */
    val success: Boolean = false,

    /** 錯誤代碼 */
    val code: Int = 0,

    /** 錯誤訊息 */
    val message: String = "",

    /** 診所名稱 */
    val name: String = "",

    /** 整所LOGO連結 */
    val logo: String = "",

    /** 跑馬燈訊息 */
    val marquee: String = "",

    /** 語系 */
    val language: String = ""
)