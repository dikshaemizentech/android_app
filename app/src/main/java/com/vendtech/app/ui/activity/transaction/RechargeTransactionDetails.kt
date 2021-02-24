package com.vendtech.app.ui.activity.transaction

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import com.downloader.*
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.transaction.RechargeTransactionDetailResult
import com.vendtech.app.models.transaction.RechargeTransactionDetails
import com.vendtech.app.models.transaction.RechargeTransactionInvoiceModel
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_transaction_details.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RechargeTransactionDetails : Activity(){

    var transIDS=""
    var amountTrans=""
    var dateTransaction=""
    var statusTransaction=""
    var meterNo=""
    lateinit var downloadInvoicePDF:LinearLayout
    var TAG="RechargeTransactionDetails"
    var rechargeID=""
    var timeTransaction=""


    //Download DetailsL
    var downloadID = 0
    internal var INVOICE_URL = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)
        var builder =  StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        downloadInvoicePDF=findViewById(R.id.downloadInvoice)
        rechargeID=intent.getIntExtra("rechargeId",0).toString()

        Log.v("DEPOSITID","Activity rechargeId: "+rechargeID)

        GetRechargeDetail()


        imgBack.setOnClickListener(View.OnClickListener {
            finish();
            overridePendingTransition(R.anim.activity_back_in, R.anim.activity_back_out);
        });

    }


    fun SetData(result: RechargeTransactionDetailResult) {

           // vendorIdLL.visibility = View.GONE
            //vendorNameLL.visibility = View.GONE
            chequeslipLL.visibility = View.GONE
            commentBoxLL.visibility = View.GONE
            paymodeLL.visibility = View.GONE
            meternoLL.visibility = View.VISIBLE


            Glide.with(this).load(R.drawable.light).into(rechargeLogoIV)
            rechargeTypeTV.text = "Electricity Recharge"
            transID.text = transIDS
        vendorIdTrans.text="${result.vendorId}"
        vendornameTrans.text=result.vendorName
        amntTrans.text= "SLL: ${NumberFormat.getNumberInstance(Locale.US).format(amountTrans.toDouble().toInt())}"

    //  amntTrans.text = amountTrans
            dateTrans.text = dateTransaction
            statusTrans.text = statusTransaction
            meternoTrans.text = meterNo
            timeTrans.text=timeTransaction

            if(statusTransaction.equals("Pending")){
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colorred))
            }else if (statusTransaction.equals("Rejected")){
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colorred))
            }else if (statusTransaction.equals("Success")){
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colorgreen))
            }else {
                statusTrans.setTextColor(ContextCompat.getColor(this,R.color.colororange))
            }





        downloadInvoicePDF.setOnClickListener(View.OnClickListener {
            if(checkAndRequestPermissions()){
                GetRechargeDetailsPDF()
            }
        })

    }

    fun GetRechargeDetail(){


        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        val call: Call<RechargeTransactionDetails> = Uten.FetchServerData().get_rechargedetail(SharedHelper.getString(this, Constants.TOKEN),rechargeID)
        call.enqueue(object : Callback<RechargeTransactionDetails> {
            override fun onResponse(call: Call<RechargeTransactionDetails>, response: Response<RechargeTransactionDetails>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                val  g = Gson()
                g.toJson(response.body())

                var data=response.body()
                if(data!=null){


                    if(data.status.equals("true")){

                        if(data.result.transactionId==null || data.result.transactionId.equals("")){
                            transIDS="N/A"
                        }else{
                            transIDS=data.result.transactionId
                        }

                        amountTrans=data.result.amount
                        dateTransaction=Utilities.changeDateFormat(/*this@RechargeTransactionDetails,*/data.result.createdAt)
                        timeTransaction=Utilities.changeTimeFormat(this@RechargeTransactionDetails,data.result.createdAt)
                        statusTransaction=data.result.status
                        meterNo=data.result.meterNumber

                        SetData(data.result)
                    }else{
                        Utilities.CheckSessionValid(data.message,this@RechargeTransactionDetails,this@RechargeTransactionDetails)
                    }
                }
            }

            override fun onFailure(call: Call<RechargeTransactionDetails>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }

    fun GetRechargeDetailsPDF(){

        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        val call: Call<RechargeTransactionInvoiceModel> = Uten.FetchServerData().get_rechargedetail_pdf(SharedHelper.getString(this,Constants.TOKEN),rechargeID)
        call.enqueue(object : Callback<RechargeTransactionInvoiceModel> {
            override fun onResponse(call: Call<RechargeTransactionInvoiceModel>, response: Response<RechargeTransactionInvoiceModel>) {

                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
                val  g = Gson()
                g.toJson(response.body())

                var data=response.body()
                if(data!=null){


                    if(data.status.equals("true")){

                        if(!TextUtils.isEmpty(data.result.path)){

                            if(data.result.path.contains(".pdf")){

                                INVOICE_URL=data.result.path
                                PerformDownload()
                            }else {
                                Utilities.shortToast("Error while downloading the file",this@RechargeTransactionDetails)
                            }
                        }else{
                            Utilities.shortToast("File not found",this@RechargeTransactionDetails)
                        }
                    }else{
                        Utilities.CheckSessionValid(data.message,this@RechargeTransactionDetails,this@RechargeTransactionDetails)
                    }
                }
            }

            override fun onFailure(call: Call<RechargeTransactionInvoiceModel>, t: Throwable) {
                val  gs = Gson()
                gs.toJson(t.localizedMessage)
                if(customDialog.isShowing){
                    customDialog.dismiss()
                }
            }
        })
    }


    fun PerformDownload(){

        var customDialog:CustomDialog
        customDialog=CustomDialog(this)
        customDialog.show()

        var filedirectorys = File(Environment.getExternalStorageDirectory(),"/VendTech/Invoice");

        if (!filedirectorys.exists()) {
            filedirectorys.mkdirs()
        }

        var filename = "VTR"+System.currentTimeMillis().toString()+".pdf"
        downloadID = PRDownloader.download(INVOICE_URL,filedirectorys.path, filename)
                .build()
                .setOnStartOrResumeListener {
                }
                .setOnPauseListener {
                }
                .setOnCancelListener {
                }
                .setOnProgressListener { progress ->
                }
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        customDialog.dismiss()
                        Utilities.shortToast("Download complete",this@RechargeTransactionDetails)
                        var openFile=  File(Environment.getExternalStorageDirectory(),"/VendTech/Invoice/"+filename);
                        OpenPdfFile(openFile)
                    }

                    override fun onError(error: Error) {
                        customDialog.dismiss()
                        var g:Gson
                        g= Gson()
                        Log.v("DownloadError",g.toJson(error))
                        Utilities.shortToast("Downloading failed",this@RechargeTransactionDetails)
                    }
                })
    }



    private fun checkAndRequestPermissions(): Boolean {
        val writepermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)

        val listPermissionsNeeded = ArrayList<String>()

        if (writepermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                // Fill with actual results from user
                if (grantResults.size > 0) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED

                    ) {
                        // process the normal flow
                        //  GoInsideApp()
                        //PerformDownload()
                        GetRechargeDetailsPDF()
                        //else any one or both the permissions are not granted
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        //                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                               ) {
                            showDialogOK("Service Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                dialog.dismiss()
                                        }
                                    })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }

    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
    }

    private fun explain(msg: String) {
        val dialog = android.support.v7.app.AlertDialog.Builder(this)
        dialog.setMessage(msg)
                .setPositiveButton("Yes") { paramDialogInterface, paramInt ->
                    //  permissionsclass.requestPermission(type,code);
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.vendtech.app")))
                }
                .setNegativeButton("Cancel") { paramDialogInterface, paramInt -> finish() }
        dialog.show()
    }

    companion object {
        val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
    }


    fun OpenPdfFile(file:File){
         var target =  Intent(Intent.ACTION_VIEW);
         target.setDataAndType(Uri.fromFile(file),"application/pdf");
         target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
         var intent = Intent.createChooser(target, "Open File");
    try {
        startActivity(intent);
        } catch ( e : ActivityNotFoundException) {
    // Instruct the user to install a PDF reader here, or something
        Utilities.shortToast("Unable to found any PDF reader application. Please install any PDF reader",this)
      }

    }
}