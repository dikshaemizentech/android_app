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


  private  lateinit var itemClickListener: ItemClickListener
  private  var itemEditables = false

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications_list, parent, false);
      return ViewHolder(view)
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {

     /* holder.notificationMessage.text=serviceList.get(position).message
        holder.notificationTitle.text=serviceList.get(position).title
        holder.dateNotificationTV.text=Utilities.DateFormatNotificationList(serviceList.get(position).sentOn)


        holder.itemView.setOnClickListener(View.OnClickListener {

            if(serviceList.get(position).type.equals("1")){

                var intent=Intent(context, RechargeTransactionDetails::class.java)
                intent.putExtra("rechargeId",serviceList.get(position).id.toString().toInt())
                context.startActivity(intent)

            }else if(serviceList.get(position).type.equals("2")){

                var intent=Intent(context, DepositTransactionDetails::class.java)
                intent.putExtra("depositId",serviceList.get(position).id.toInt())
                context.startActivity(intent)

            }
        })*/
  }

  override fun getItemCount(): Int {
       // return serviceList.size
      return 10
  }

    override fun onClick(v: View) {

    }



    interface ItemClickListener {
        fun clickedServiceId(platformId:String)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var tv_date: TextView
        lateinit var tv_meter_id: TextView
        lateinit var tv_amount: TextView

        init {

            tv_date = itemView.findViewById<View>(R.id.tv_date) as TextView
            tv_meter_id = itemView.findViewById<View>(R.id.tv_meter_id) as TextView
            tv_amount = itemView.findViewById<View>(R.id.tv_amount) as TextView



        }

    }

}