package com.vendtech.app.models.profile

class NotificationListModel (var status:String,var message:String,var result:MutableList<NotificationListResult>)

data class NotificationListResult(var message:String,var title:String,var type:String,var userName:String,var sentOn:String,var id:String)