package com.vendtech.app.adapter.profile

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.profile.NotificationListResult
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class NotificationListAdapter (internal var context: Context, internal var serviceList:MutableList<NotificationListResult>): RecyclerView.Adapter<NotificationListAdapter.ViewHolder>(), View.OnClickListener {


    lateinit var itemClickListener: ItemClickListener
    var itemEditables = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_notifications, null)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        var data=serviceList.get(position);


        if (data.rechargePin!=null){
            //meter recharge
            holder.tv_notification_type.setText(context.resources.getString(R.string.meter_recharge_notifications));
            holder.tv_pos_id.setText(context.resources.getString(R.string.pos_id)+": "+data.posId);
            holder.tv_date.setText(data.createdAt);
            holder.tv_deposite.setText(context.resources.getString(R.string.meter2)+""+data.meterNumber);
            //holder.tv_amount.setText(context.resources.getString(R.string.amount2)+""+data.amount);

            holder.tv_amount.setText(context.resources.getString(R.string.amount2)+""+ NumberFormat.getNumberInstance(Locale.US).format(data.amount.toDouble().toInt()));
            holder.tv_transaction_id.setText(context.resources.getString(R.string.transaction_id)+" "+data.transactionId);
            holder.tv_token.setText(context.resources.getString(R.string.token)+" "+data.rechargePin);
            holder.tv_token.visibility=View.VISIBLE;

        }else{

            //deposite notification
            holder.tv_notification_type.setText(context.resources.getString(R.string.deposite__notifications));
            holder.tv_pos_id.setText(context.resources.getString(R.string.pos_id)+": "+data.posNumber);
            holder.tv_date.setText(data.createdAt);
            //holder.tv_deposite.setText(context.resources.getString(R.string.deposite)+" "+data.amount);
            holder.tv_deposite.setText(context.resources.getString(R.string.deposite)+" "+NumberFormat.getNumberInstance(Locale.US).format(data.amount.toDouble().toInt()));

            holder.tv_amount.setText(context.resources.getString(R.string.deposite_ref)+" "+data.chkNoOrSlipId);
            //holder.tv_transaction_id.setText(context.resources.getString(R.string.new_balance)+" "+data.newBalance);
            holder.tv_transaction_id.setText(context.resources.getString(R.string.new_balance)+" "+NumberFormat.getNumberInstance(Locale.US).format(data.newBalance.toDouble().toInt()));

            holder.tv_token.visibility=View.GONE;

        }


        /*  holder.notificationMessage.text=serviceList.get(position).message
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
          })*/

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


        lateinit var tv_notification_type: TextView
        lateinit var tv_pos_id: TextView
        lateinit var tv_date: TextView
        lateinit var tv_deposite: TextView
        lateinit var tv_amount: TextView
        lateinit var tv_transaction_id: TextView
        lateinit var tv_token: TextView


        init {


            tv_notification_type = itemView.findViewById<View>(R.id.tv_notification_type) as TextView
            tv_pos_id = itemView.findViewById<View>(R.id.tv_pos_id) as TextView
            tv_date = itemView.findViewById<View>(R.id.tv_date) as TextView
            tv_deposite = itemView.findViewById<View>(R.id.tv_deposite) as TextView
            tv_amount = itemView.findViewById<View>(R.id.tv_amount) as TextView
            tv_transaction_id = itemView.findViewById<View>(R.id.tv_transaction_id) as TextView
            tv_token = itemView.findViewById<View>(R.id.tv_token) as TextView



        }

    }

}