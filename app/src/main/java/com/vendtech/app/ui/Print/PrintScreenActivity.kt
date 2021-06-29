package com.vendtech.app.ui.Print


import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.*
import android.graphics.Bitmap
import android.os.BatteryManager
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.telpo.tps550.api.printer.ThermalPrinter
import com.telpo.tps550.api.util.StringUtil
import com.telpo.tps550.api.util.SystemUtil
import com.vendtech.app.R
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.print_screen_layout.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*

class PrintScreenActivity : AppCompatActivity() {


    /*
     print
     */

    var printVersion: String? = null
    val NOPAPER = 3
    val LOWBATTERY = 4
    val PRINTVERSION = 5
    val PRINTBARCODE = 6
    val PRINTQRCODE = 7
    val PRINTPAPERWALK = 8
    val PRINTCONTENT = 9
    val CANCELPROMPT = 10
    val PRINTERR = 11
    val OVERHEAT = 12
    val MAKER = 13
    val PRINTPICTURE = 14
    val EXECUTECOMMAND = 15

    private val size = 660;
    private val size_width = 600;
    private val size_height = 264;

    private var Result: String? = null
    private var nopaper = false
    private var LowBattery = false

    private var barcodeStr: String? = null
    private var qrcodeStr: String? = null
    private var paperWalk = 0
    private var printContent: String? = null
    private val leftDistance = 0
    private val lineDistance = 0
    private val wordFont = 0
    private val printGray = 0
    private var progressDialog: ProgressDialog? = null
    private val MAX_LEFT_DISTANCE = 255

    private var handler: MyHandler? = null

    private var rechargeTransactionDetailResult: RechargeMeterModel?=null;

    private var dialog: ProgressDialog? = null;

    inner class MyHandler : Handler() {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                NOPAPER -> noPaperDlg()
                LOWBATTERY -> {
                    val alertDialog = AlertDialog.Builder(this@PrintScreenActivity)
                    alertDialog.setTitle(R.string.operation_result)
                    alertDialog.setMessage(getString(R.string.LowBattery))
                    alertDialog.setPositiveButton(getString(R.string.dialog_comfirm), DialogInterface.OnClickListener { dialogInterface, i -> })
                    alertDialog.show()
                }
                PRINTVERSION -> {
                    dialog!!.dismiss()
                    if (msg.obj == "1") {
                        //textPrintVersion.setText(Constants.printVersion)
                    } else {
                        Toast.makeText(this@PrintScreenActivity, R.string.operation_fail, Toast.LENGTH_LONG).show()
                    }
                }
                PRINTBARCODE -> barcodePrintThread().start()
                PRINTQRCODE -> qrcodePrintThread().start()
                PRINTPAPERWALK -> paperWalkPrintThread().start()
                PRINTCONTENT -> contentPrintThread().start()
                MAKER -> MakerThread().start()
                //Constants.PRINTPICTURE -> printPicture().start()
                CANCELPROMPT -> if (progressDialog != null && !this@PrintScreenActivity.isFinishing()) {
                    progressDialog!!.dismiss()
                    progressDialog = null
                }
                // EXECUTECOMMAND ->executeCommand().start()
                OVERHEAT -> {
                    val overHeatDialog = AlertDialog.Builder(this@PrintScreenActivity)
                    overHeatDialog.setTitle(R.string.operation_result)
                    overHeatDialog.setMessage(getString(R.string.overTemp))
                    overHeatDialog.setPositiveButton(getString(R.string.dialog_comfirm), DialogInterface.OnClickListener { dialogInterface, i -> })
                    overHeatDialog.show()
                }
                else -> Toast.makeText(this@PrintScreenActivity, "Print Error!", Toast.LENGTH_LONG).show()
            }
        }
    }

   inner  private class paperWalkPrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                ThermalPrinter.start(this@PrintScreenActivity)
                ThermalPrinter.reset()
                ThermalPrinter.walkPaper(paperWalk)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Result = e.toString();
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
                ThermalPrinter.stop(this@PrintScreenActivity)
            }
        }
    }

   @Throws(WriterException::class)
   fun CreateCode(str: String?, type: BarcodeFormat?, bmpWidth: Int, bmpHeight: Int): Bitmap? {
        val mHashtable = Hashtable<EncodeHintType, String?>()
        mHashtable[EncodeHintType.CHARACTER_SET] = "UTF-8"
        // Generate a two-dimensional matrix, specify the size when encoding, do not zoom after the image is generated, to prevent blurring and cause recognition
        val matrix = MultiFormatWriter().encode(str, type, bmpWidth, bmpHeight, mHashtable)
        val width = matrix.width
        val height = matrix.height
        // Convert a two-dimensional matrix to a one-dimensional pixel array (row horizontally all the time)
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (matrix[x, y]) {
                    pixels[y * width + x] = -0x1000000
                } else {
                    pixels[y * width + x] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        // Generate bitmap through pixel array, refer to api for details api
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

   inner  private class barcodePrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                ThermalPrinter.start(this@PrintScreenActivity)
                ThermalPrinter.reset()
                ThermalPrinter.setGray(printGray)
                val bitmap = CreateCode(barcodeStr, BarcodeFormat.CODE_128, 320, 176)
                if (bitmap != null) {
                    ThermalPrinter.printLogo(bitmap)
                }
                ThermalPrinter.addString(barcodeStr)
                ThermalPrinter.printString()
                ThermalPrinter.walkPaper(100)
            } catch (e: Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
                ThermalPrinter.stop(this@PrintScreenActivity)
            }
        }
   }

   inner private class qrcodePrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                ThermalPrinter.start(this@PrintScreenActivity)
                ThermalPrinter.reset()
                ThermalPrinter.setGray(printGray)
                val bitmap = CreateCode(qrcodeStr, BarcodeFormat.QR_CODE, 256, 256)
                if (bitmap != null) {
                    ThermalPrinter.printLogo(bitmap)
                }
                ThermalPrinter.addString(qrcodeStr)
                ThermalPrinter.printString()
                ThermalPrinter.walkPaper(100)
            } catch (e: Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
                ThermalPrinter.stop(this@PrintScreenActivity)
            }
        }
   }

   inner private class contentPrintThread : Thread() {
        override fun run() {
            super.run()
            try {
                ThermalPrinter.start(this@PrintScreenActivity)
                ThermalPrinter.reset()
                ThermalPrinter.setAlgin(ThermalPrinter.ALGIN_MIDDLE)
                ThermalPrinter.setLeftIndent(leftDistance)
                ThermalPrinter.setLineSpace(lineDistance)
                if (wordFont == 4) {
                    ThermalPrinter.setFontSize(2)
                    ThermalPrinter.enlargeFontSize(2, 2)
                } else if (wordFont == 3) {
                    ThermalPrinter.setFontSize(1)
                    ThermalPrinter.enlargeFontSize(2, 2)
                } else if (wordFont == 2) {
                    ThermalPrinter.setFontSize(2)
                } else if (wordFont == 1) {
                    ThermalPrinter.setFontSize(1)
                }
                ThermalPrinter.setGray(printGray)
                //ThermalPrinter.addString(printContent)
                ThermalPrinter.addString(printContent)

                val bitmap = CreateCode(tv_bar_code_no.text.toString(), BarcodeFormat.CODE_39, 320, 176)
                if (bitmap != null) {
                    ThermalPrinter.printLogo(bitmap)
                }

                ThermalPrinter.addString(tv_bar_code_no.text.toString());

                ThermalPrinter.printString()
                ThermalPrinter.walkPaper(100);
            } catch (e: Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
                ThermalPrinter.stop(this@PrintScreenActivity)
            }
        }
   }

   inner private class MakerThread : Thread() {
        override fun run() {
            super.run()
            try {
                ThermalPrinter.start(this@PrintScreenActivity)
                ThermalPrinter.reset()
                ThermalPrinter.searchMark(200, 50)
            } catch (e: Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                handler!!.sendMessage(handler!!.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler!!.sendMessage(handler!!.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
                ThermalPrinter.stop(this@PrintScreenActivity)
            }
        }
    }

    /* inner private class executeCommand : Thread() {
        override fun run() {
            super.run()
            try {
                ThermalPrinter.start(this@PrintScreenActivity)
                ThermalPrinter.reset()
                ThermalPrinter.sendCommand(edittext_input_command.getText().toString())
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
                Result = e.toString()
                if (Result == "com.telpo.tps550.api.printer.NoPaperException") {
                    nopaper = true
                } else if (Result == "com.telpo.tps550.api.printer.OverHeatException") {
                    handler!!.sendMessage(handler!!.obtainMessage(OVERHEAT, 1, 0, null))
                } else {
                    handler!!.sendMessage(handler!!.obtainMessage(PRINTERR, 1, 0, null))
                }
            } finally {
                try {
                    sleep(3000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                handler.sendMessage(handler.obtainMessage(CANCELPROMPT, 1, 0, null))
                if (nopaper) {
                    handler.sendMessage(handler.obtainMessage(NOPAPER, 1, 0, null))
                    nopaper = false
                    return
                }
                ThermalPrinter.stop(this@PrinterActivity)
            }
        }
    }
*/

    private fun noPaperDlg() {
        val dlg = AlertDialog.Builder(this)
        dlg.setTitle(getString(R.string.noPaper))
        dlg.setMessage(getString(R.string.noPaperNotice))
        dlg.setCancelable(false)
        dlg.setPositiveButton(R.string.sure, DialogInterface.OnClickListener { dialogInterface, i -> ThermalPrinter.stop(this) })
        dlg.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.print_screen_layout)

        if (intent.extras!=null){
            rechargeTransactionDetailResult= intent.getSerializableExtra(Constants.DATA) as RechargeMeterModel?;
        }

        handler = MyHandler()

        setData(rechargeTransactionDetailResult!!);

        val pIntentFilter = IntentFilter()
        pIntentFilter.addAction(Intent.ACTION_BATTERY_CHANGED)
        pIntentFilter.addAction("android.intent.action.BATTERY_CAPACITY_EVENT")
        registerReceiver(printReceive, pIntentFilter);

        tv_sms.setOnClickListener{
                showdialog();
        }
        imgBack.setOnClickListener {
            var intent=Intent();
            setResult(Activity.RESULT_OK);
            finish();
        }
        img_close.setOnClickListener {
            var intent=Intent();
            setResult(Activity.RESULT_OK);
            finish();
        }
        tv_print.setOnClickListener {
               // mPdfPrinter!!.print(PRINT_PDF, mDirectory!!, mFilename!!);

               printContent="${tv_vendtech_name.text.toString()}\n" +
                    "${tv_edsa.text.toString()}\n" +
                    "--------------------------------------------------------------\n" +
                    "${tv_date_txt.text.toString() +" "+tv_date.text.toString()}\n" +
                    "${tv_vendor_txt.text.toString() +" "+tv_vendor_name.text.toString()}\n" +
                    "${tv_pos_id_txt.text.toString() +" "+tv_pos_id.text.toString()}\n" +
                    "------${tv_custInfo.text.toString()+"------"}\n" +
                    "${tv_customer_txt.text.toString() +" "+tv_cus_name.text.toString()}\n" +
                    "${tv_account_txt.text.toString() +" "+tv_account.text.toString()}\n" +
                    "${tv_meter_txt.text.toString() +" "+tv_meter_number.text.toString()}\n" +
                    "${tv_amt_tend_txt.text.toString() +" "+tv_amount_tendered.text.toString()}\n" +
                    "------${tv_deduct.text.toString()+"------"}\n" +
                    "${tv_service_charge_txt.text.toString() +" "+tv_service_charge.text.toString()}\n" +
                    "${tv_debit_recovery_txt.text.toString() +" "+tv_debit_recovery.text.toString()}\n" +
                    "------${tv_tottext.text.toString()+"------"}\n" +
                    "${tv_cost_of_unit_txt.text.toString() +" "+tv_cost_of_unit.text.toString()}\n" +
                    "${tv_unit_txt.text.toString() +" "+tv_unit.text.toString()}\n" +
                    "${tv_token.text.toString()}\n" +
                    "${tv_vtech_txt.text.toString() +" "+tv_transaction_id.text.toString()}\n" +
                    "${tv_web_text.text.toString()}\n" +
                    "${tv_phone_no.text.toString()}\n";

                     //Toast.makeText(this,""+printContent,Toast.LENGTH_LONG).show();

                    if (printContent==null || printContent!!.length==0){
                        Utilities.longToast(getString(R.string.empty),this)
                         return@setOnClickListener
                    }
                    if (LowBattery==true){
                        handler!!.sendMessage(handler!!.obtainMessage(LOWBATTERY, 1, 0, null))
                    }else{
                        if (!nopaper) {
                            progressDialog = ProgressDialog.show(this, getString(R.string.bl_dy), getString(R.string.printing_wait))
                            handler!!.sendMessage(handler!!.obtainMessage(PRINTCONTENT, 1, 0, null));
                        } else {
                            //Toast.makeText(this, , Toast.LENGTH_LONG).show()
                            Utilities.longToast(getString(R.string.ptintInit),applicationContext);
                        }

                    }

        }
    }

    private val printReceive: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == Intent.ACTION_BATTERY_CHANGED) {
                val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_NOT_CHARGING)
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0)
                //TPS390 can not print,while in low battery,whether is charging or not charging
                if (SystemUtil.getDeviceType() == StringUtil.DeviceModelEnum.TPS390.ordinal) {
                    if (level * 5 <= scale) {
                        LowBattery = true
                    } else {
                        LowBattery = false
                    }
                } else {
                    if (status != BatteryManager.BATTERY_STATUS_CHARGING) {
                        if (level * 5 <= scale) {
                            LowBattery = true
                        } else {
                            LowBattery = false
                        }
                    } else {
                        LowBattery = false
                    }
                }
            } else if (action == "android.intent.action.BATTERY_CAPACITY_EVENT") {
                val status = intent.getIntExtra("action", 0)
                val level = intent.getIntExtra("level", 0)
                if (status == 0) {
                    if (level < 1) {
                        LowBattery = true
                    } else {
                        LowBattery = false
                    }
                } else {
                    LowBattery = false
                }
            }
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        var intent=Intent();
        setResult(Activity.RESULT_OK);
        finish();
    }

    private fun setBarCode(barCodeNo:String){

        var bitmap: Bitmap? = null
        try {
            bitmap = CreateImage(barCodeNo)
            //myBitmap = bitmap
        } catch (we: WriterException) {
            we.printStackTrace()
        }

        if (bitmap != null) {
            img_bar_code.setImageBitmap(bitmap);
        }

    }

    private fun setData(rechargeMeterModel: RechargeMeterModel){

        if (rechargeMeterModel.result==null){
            return;
        }

        tv_date.setText(rechargeMeterModel.result.transactionDate)
        tv_vendor_name.setText(rechargeMeterModel.result.vendorId)
        tv_pos_id.setText(rechargeMeterModel.result.pos)
        tv_cus_name.setText(rechargeMeterModel.result.customerName)
        tv_account.setText(rechargeMeterModel.result.accountNo)
        tv_address.setText(rechargeMeterModel.result.address)
        tv_meter_number.setText(rechargeMeterModel.result.deviceNumber)
        //tv_terrif.setText(rechargeMeterModel.result.tarrif)

        //var longval:Long=meterListModels[position].amount.toLong();

        //tv_gst.setText("le:"+rechargeMeterModel.result.tax);


        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatter.applyPattern("#,###,###,###");

        var formatterFloat: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
        formatterFloat.applyPattern("#,###,###,###.##");

        //val  amoundDouble:Double=rechargeMeterModel.result.amount.toDouble();
        val  chargesDouble:Double=rechargeMeterModel.result.charges.toDouble();
        val  debitRecoveryDouble:Double=rechargeMeterModel.result.debitRecovery.toDouble();
        val  tarrifDouble:Double=rechargeMeterModel.result.tarrif.toDouble();
        //val  unitCostDouble:Double=rechargeMeterModel.result.unitCost.toDouble();
        val  unitCostDouble:Double=rechargeMeterModel.result.unitCost.replace(",","").toDouble();
        val  taxDouble:Double=rechargeMeterModel.result.tax.toDouble();

        // var formattedAmount = formatter.format(amoundDouble);
        var formattedServiceCharge = formatter.format(chargesDouble);
        var formattedRecovery = formatter.format(debitRecoveryDouble);
        var formattedTarrif = formatter.format(tarrifDouble);

        var formattedUnitCost=formatterFloat.format(unitCostDouble);
        var formattedTax=formatterFloat.format(taxDouble);

        tv_gst.setText("le:"+formattedTax);

        tv_terrif.setText(formattedTarrif);

        tv_amount_tendered.setText(rechargeMeterModel.result.amount);
        tv_service_charge.setText("le:"+formattedServiceCharge);
        tv_debit_recovery.setText("le:"+formattedRecovery);
        tv_debit_recovery.setText("le:"+formattedRecovery);

        tv_cost_of_unit.setText("le:"+formattedUnitCost)

        tv_unit.setText(rechargeMeterModel.result.unit)
        tv_token.setText(rechargeMeterModel.result.pin1)
        tv_serial.setText(rechargeMeterModel.result.serialNo)
        tv_transaction_id.setText(rechargeMeterModel.result.vtechSerial)

        tv_bar_code_no.setText(rechargeMeterModel.result.deviceNumber)
        setBarCode(rechargeMeterModel.result.deviceNumber);

    }

    @Throws(WriterException::class)
    fun CreateImage(message: String?): Bitmap? {
        var bitMatrix: BitMatrix? = null
        bitMatrix =MultiFormatWriter().encode(message, BarcodeFormat.CODE_39, size_width, size_height);

        val width = bitMatrix.width
        val height = bitMatrix.height
        val pixels = IntArray(width * height)
        for (i in 0 until height) {
            for (j in 0 until width) {
                if (bitMatrix[j, i]) {
                    pixels[i * width + j] = -0x1000000
                } else {
                    pixels[i * width + j] = -0x1
                }
            }
        }
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    private fun showdialog() {
        val adDialog = Dialog(this@PrintScreenActivity, R.style.MyDialogThemeBlack);
        adDialog.window!!.requestFeature(Window.FEATURE_NO_TITLE);
        adDialog.setContentView(R.layout.alertdialog);
        adDialog.setCancelable(false);

        val tv_send = adDialog.findViewById<TextView>(R.id.tv_send);
        val img_close = adDialog.findViewById<AppCompatImageButton>(R.id.img_close);

        img_close.setOnClickListener {
            adDialog.dismiss();
        }

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
        adDialog.show();
    }

    override fun onDestroy() {
        if (progressDialog != null && !this.isFinishing()) {
            progressDialog!!.dismiss()
            progressDialog = null
        }
        unregisterReceiver(printReceive)
        ThermalPrinter.stop()
        super.onDestroy()
    }

}