package com.vendtech.app.ui.activity.profile

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.adapter.profile.NotificationListAdapter
import com.vendtech.app.adapter.profile.UserServicesAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.profile.NotificationListModel
import com.vendtech.app.models.profile.NotificationListResult
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_notifications_list.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.downloader.PRDownloader.cancelAll
import android.content.Context.NOTIFICATION_SERVICE
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import com.vendtech.app.models.meter.MeterListResults
import java.util.ArrayList


class NotificationsListActivity :Activity(){

    lateinit var recyclerView:RecyclerView
    var pageNumber=1
    lateinit var notificationListAdapter:NotificationListAdapter
    internal var notificationList: MutableList<NotificationListResult> = ArrayList()


    //Pagination
    var loadings = true
    var pastVisiblesItems = 0
    var visibleItemCount = 0
    var totalItemCount = 0
    var totalItemsNo=10



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications_list)
        initView()
    }


    fun initView(){

        recyclerView=findViewById<View>(R.id.recyclerviewNotificationsList)as RecyclerView

        imgBack.setOnClickListener(View.OnClickListener {
            finish()
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out)

        })

        GetNotificationsList()

    }

    fun GetNotificationsList(){

        var customDialog: CustomDialog
        customDialog= CustomDialog(this)
        customDialog.show()

        val call: Call<NotificationListModel> = Uten.FetchServerData().get_notifications(SharedHelper.getString(this, Constants.TOKEN),pageNumber.toString(),totalItemsNo.toString())
        call.enqueue(object : Callback<NotificationListModel> {
            override fun onResponse(call: Call<NotificationListModel>, response: Response<NotificationListModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }

                var data=response.body()
                if(data!=null){
                    if(data.status.equals("true")){

                        if(data.result.size>0){


                            if(data.result.size<totalItemsNo){
                                //last item reached. so no more pagination required
                                loadings=false
                            }else{
                                loadings=true
                            }

                            if(pageNumber==1){
                                notificationList.clear()
                                notificationList.addAll(data.result)
                                SetUpAdapter(notificationList)

                            }else {

                                notificationList.addAll(data.result)
                                notificationListAdapter.notifyDataSetChanged()

                            }

                            nodataTV.visibility=View.GONE
                            recyclerView.visibility=View.VISIBLE
                           // SetUpAdapter(data.result)

                        }else{

                            if(notificationList.size<1) {
                                nodataTV.visibility=View.VISIBLE
                                recyclerView.visibility=View.GONE
                            }
                        }

                    }else{
                        Utilities.CheckSessionValid(data.message,this@NotificationsListActivity,this@NotificationsListActivity)

                    }
                }
            }

            override fun onFailure(call: Call<NotificationListModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }


    override fun onResume() {
        super.onResume()
        // Clear all notification
        val nMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nMgr.cancelAll()
    }

    fun SetUpAdapter(data:MutableList<NotificationListResult>){

        var mLayoutManager=LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        notificationListAdapter = NotificationListAdapter(this, data)
        recyclerView.adapter = notificationListAdapter
        recyclerView.layoutManager = mLayoutManager
        //recyclerView.setHasFixedSize(true)
        notificationListAdapter.notifyDataSetChanged()


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount = mLayoutManager.childCount
                    totalItemCount = mLayoutManager.itemCount
                    pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition()

                    if (loadings) {

                        if (visibleItemCount + pastVisiblesItems >= totalItemCount) {
                            loadings = false
                            Log.v("NotificationList", "-------------------------------------------Last Item Wow !--------------------")
                            pageNumber++
                            GetNotificationsList()
                        }
                    }
                }
            }
        })


    }
}