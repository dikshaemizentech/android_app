package com.vendtech.app.adapter.profile

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.profile.UserServicesResult
import com.vendtech.app.utils.Utilities

class UserServicesAdapter(internal var context: Context,internal var serviceList:MutableList<UserServicesResult>,var itemClickListeners: ItemClickListener): RecyclerView.Adapter<UserServicesAdapter.ViewHolder>(), View.OnClickListener {


    lateinit var itemClickListener: ItemClickListener
    var itemEditables = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        @SuppressLint("InflateParams") val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user_services, null)
        this.itemClickListener = itemClickListeners

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        holder.serviceName.text=serviceList.get(position).title

        if(serviceList.get(position).platformId.contentEquals("1")){

            holder.serviceIcon.setImageResource(R.drawable.light)

        }else if(serviceList.get(position).platformId.contentEquals("2")){

            holder.serviceIcon.setImageResource(R.drawable.water)


        }else if(serviceList.get(position).platformId.contentEquals("3")){

            holder.serviceIcon.setImageResource(R.drawable.gas)

        }else {
            holder.serviceIcon.setImageResource(R.drawable.noicon)

        }

        holder.itemViews.setOnClickListener(View.OnClickListener {

            if(serviceList.get(position).platformId.contentEquals("1")){
                if(itemClickListener!=null){
                    itemClickListener.clickedServiceId(serviceList.get(position).platformId)
                }

            }else{

                Utilities.shortToast("This service will be available soon.",context)
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


        internal var serviceName: TextView
        internal var serviceIcon: ImageView
        lateinit var itemViews:RelativeLayout

        init {
            serviceName = itemView.findViewById<View>(R.id.serviceNameTV) as TextView
            serviceIcon = itemView.findViewById<View>(R.id.serviceIconIV) as ImageView
            itemViews=itemView.findViewById<View>(R.id.itemView)as RelativeLayout
        }

    }

}