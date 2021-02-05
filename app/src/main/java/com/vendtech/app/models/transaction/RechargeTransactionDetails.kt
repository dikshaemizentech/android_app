package com.vendtech.app.models.transaction

class RechargeTransactionDetails(var status:String,var message:String,var result:RechargeTransactionDetailResult)

data class RechargeTransactionDetailResult(var vendorName:String,var vendorId:Int,var meterNumber:String,var amount:String,var createdAt:String,var status:String,var transactionId:String)

