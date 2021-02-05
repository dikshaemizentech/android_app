package com.vendtech.app.models.authentications

class GetUserTypesModel(var status:String,var message:String,var result:List<ResultUserTypes>)

data class ResultUserTypes(var disabled:String,var group:String,var selected:String,var text:String,var value:String)

