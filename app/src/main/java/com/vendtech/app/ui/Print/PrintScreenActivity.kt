package com.vendtech.app.ui.Print

import android.app.Dialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Window
import android.widget.TextView
import com.vendtech.app.R
import com.vendtech.app.models.transaction.RechargeTransactionDetailResult
import com.vendtech.app.utils.Constants
import kotlinx.android.synthetic.main.print_screen_layout.*

class PrintScreenActivity : AppCompatActivity() {

    private var  rechargeTransactionDetailResult: RechargeTransactionDetailResult?=null;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.print_screen_layout)

        if (intent.extras!=null){
            rechargeTransactionDetailResult= intent.getSerializableExtra(Constants.DATA) as RechargeTransactionDetailResult?
        }

        tv_sms.setOnClickListener{
            showdialog()
        }

    }
    private fun setData(rechargeTransactionDetailResult: RechargeTransactionDetailResult){

    }




    private fun showdialog() {
        val adDialog = Dialog(this@PrintScreenActivity, R.style.MyDialogThemeBlack)
        adDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        adDialog.setContentView(R.layout.alertdialog)
        adDialog.setCancelable(false)


        val tv_send = adDialog.findViewById<TextView>(R.id.tv_send)




        /*tv_done.setOnClickListener {
            //   adDialog.cancel()
            if (MyApplication.isConnectingToInternet(THIS)) {
                // var str = edittext.text.toString()
                if (edittext.text.toString().length == 0 || edittext.text.toString().trim().matches("".toRegex())) {
                    MyApplication.popErrorMsg("", resources.getString(R.string.plz_enter_eamil), THIS)
                } else {
                    adDialog.dismiss();
                    ForgotPassword(edittext.getText().toString());
                }

            } else
                noNetConnection()
        }*/
        tv_send.setOnClickListener { adDialog.cancel() }
        adDialog.show()

    }

}