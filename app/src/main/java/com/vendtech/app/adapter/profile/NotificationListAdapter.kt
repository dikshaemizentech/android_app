package com.vendtech.app.adapter.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.profile.NotificationListModel
import com.vendtech.app.models.profile.NotificationListResult
import com.vendtech.app.ui.activity.transaction.DepositTransactionDetails
import com.vendtech.app.ui.activity.transaction.RechargeTransactionDetails
import com.vendtech.app.utils.Utilities

class NotificationListAdapter (internal var context: Context, internal var serviceList:MutableList<NotificationListResult>): RecyclerView.Adapter<NotificationListAdapter.ViewHolder>(), View.OnClickListener {


    lateinit var itemClickListener: ItemClickListener
    var itemEditables = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications_list, null)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.notificationMessage.text=serviceList.get(position).message
        holder.notificationTitle.text=serviceList.get(position).title
        holder.dateNotificationTV.text=Utilities.DateFormatNotificationList(serviceList.get(position).sentOn)


        holder.listItem.setOnClickListener(View.OnClickListener {

            if(serviceList.get(position).type.equals("1")){

                var intent=Intent(context, RechargeTransactionDetails::class.java)
                intent.putExtra("rechargeId",serviceList.get(position).id.toString().toInt())
                context.startActivity(intent)

            }else if(serviceList.get(position).type.equals("2")){

                var intent=Intent(context, DepositTransactionDetails::class.java)
                intent.putExtra("depositId",serviceList.get(position).id.toInt())
                context.startActivity(intent)

            }
        })
    }

    override fun getItemCount(): Int {
        return serviceList.size
    }

    override fun onClick(v: View) {

    }



    interface ItemClickListener {
        fun clickedServiceId(platformId:String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {


        lateinit var notificationTitle: TextView
        lateinit var notificationMessage: TextView
        lateinit var listItem:RelativeLayout
        lateinit var dateNotificationTV:TextView

        init {


            notificationMessage = itemView.findViewById<View>(R.id.notificationMessageTV) as TextView
            notificationTitle = itemView.findViewById<View>(R.id.notificationTitleTV) as TextView
            listItem=itemView.findViewById<View>(R.id.listItem)as RelativeLayout
            dateNotificationTV=itemView.findViewById<View>(R.id.dateNotificationTV)as TextView


        }

    }

}