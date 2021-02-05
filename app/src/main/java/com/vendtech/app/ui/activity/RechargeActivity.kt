package com.vendtech.app.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.TextView
import android.widget.Toast
import com.vendtech.app.R
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.activity_recharge.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class RechargeActivity: AppCompatActivity() {

    private lateinit var meterListResults: MeterListResults;
    private lateinit var tv_pos_id:TextView;
    private lateinit var tv_meter_number:TextView;
    var totalAvlblBalance = 0.0
    // private lateinit var tv_amount:TextView;

    private fun getData(){
        if(intent!!.extras!=null){
            meterListResults= intent.extras.getSerializable("Data") as MeterListResults;
            tv_meter_number.setText(meterListResults.number);
            //tv_meter_number.setText(meterListResults.number)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recharge)


        tv_pos_id=findViewById(R.id.tv_pos_id);
        tv_meter_number=findViewById(R.id.tv_meter_number);
      //  tv_amount=findViewById(R.id.tv_amount);

        et_money.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                et_money.removeTextChangedListener(this)
                try {
                    var originalString = s.toString();

                    var longval: Long
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "");
                    }
                    longval = originalString.toLong()
                    if (longval <= totalAvlblBalance) {
                        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat;
                        formatter.applyPattern("#,###,###,###");
                        var formattedString = formatter.format(longval);
                        et_money.setText(formattedString);
                        et_money.setSelection(et_money.text.length);
                    } else {
                        Toast.makeText(applicationContext, "Amount is greater then Wallet Balance", Toast.LENGTH_SHORT).show();
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                }
                et_money.addTextChangedListener(this);
            }
        })
        getData();

        imgBack.setOnClickListener {
            onBackPressed();
        }

        paynowTV.setOnClickListener {
            var moneyValue = et_money.text.toString().trim().replace(",", "");
            if (TextUtils.isEmpty(tv_meter_number.text.toString().trim())) {
                Utilities.shortToast("Please select a meter number.", applicationContext);
            } else if (TextUtils.isEmpty(et_money.text.toString().trim())) {
                Utilities.shortToast("Please enter recharge amount.", applicationContext);
            } else if (totalAvlblBalance < 1) {
                Utilities.shortToast("You don't have enough balance to recharge", applicationContext);
            } else if (moneyValue.toDouble() > totalAvlblBalance) {
                Utilities.shortToast("Recharge amount is greater than the available balance.", applicationContext);
            } else {
                //ShowAlertForRecharge(moneyValue);
            }
        }



    }

}