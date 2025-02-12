package com.zjw.chehang168

import androidx.annotation.Keep

@Keep
data class B(val l: L)

@Keep
data class L(val modelPriceShow: ModelPriceShow)

@Keep
data class ModelPriceShow(
    val hq: HQ,
    val info: Info,
)

@Keep
data class HQ(
    val title: String,// 车型名
)

@Keep
data class Info(
    val pbid: String,// 品牌id
    val psid: String,// 车系id
    val psname: String,// 车系名称
    val mid: String,// 车型id
    val date_day: String,// 日期
)

// 导入的抓包文件json结构
@Keep
data class RR(val res: Res)

@Keep
data class Res(val base64: String)
