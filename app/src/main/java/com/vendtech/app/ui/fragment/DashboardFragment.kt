package com.vendtech.app.ui.fragment

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.robinhood.ticker.TickerUtils
import com.robinhood.ticker.TickerView
import com.vendtech.app.R
import com.vendtech.app.adapter.meter.MeterListAutoCompleteAdapter
import com.vendtech.app.adapter.meter.MeterListDialogAdapter
import com.vendtech.app.adapter.profile.UserServicesAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.GetMetersModel
import com.vendtech.app.models.meter.MeterListResults
import com.vendtech.app.models.meter.PosResultModel
import com.vendtech.app.models.meter.RechargeMeterModel
import com.vendtech.app.models.profile.GetWalletModel
import com.vendtech.app.models.profile.UserAssignedServicesModel
import com.vendtech.app.models.profile.UserServicesResult
import com.vendtech.app.network.Uten
import com.vendtech.app.ui.Print.PrintScreenActivity
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.MessageEvent
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.layout_confirm_pay.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList

class DashboardFragment : Fragment(), View.OnClickListener, MeterListDialogAdapter.ItemClickListener, UserServicesAdapter.ItemClickListener {

    private  var data:MeterListResults?=null;


    lateinit var contactVendtechTV: TextView
    lateinit var errorDashTV: TextView
    lateinit var errorAccountService: LinearLayout

    //BALANCE DETAIL LAYOUT
    lateinit var totalBalanceTV: TextView
    var totalAvlblBalance = 0.0
    lateinit var tickerViewBalance: TickerView

    //SERVICE MENU LAYOUT
    //  lateinit var electricityLL: LinearLayout
    //  lateinit var waterLL: LinearLayout
    //   lateinit var gasLL: LinearLayout
    lateinit var serviceLayout: RelativeLayout
    lateinit var servicesRecyclerview: RecyclerView
    lateinit var servicesAdapter: UserServicesAdapter
//confirmpay
    lateinit var confirmPayCancel:TextView
    lateinit var confirmPayPayBtn:TextView


    //SERVICE PAY LAYOUT
    lateinit var payBillTV: TextView
    lateinit var moneyET: EditText
    lateinit var fabBack: FloatingActionButton
    lateinit var confirmFabBack: FloatingActionButton
    lateinit var payLayout: RelativeLayout
    lateinit var autoCompleteTV: AutoCompleteTextView
    internal var meterListModels: MutableList<MeterListResults> = ArrayList()
    lateinit var selectedMeterID: String
    lateinit var showListmeterIV: ImageView
    lateinit var meterDialogAdapter: MeterListDialogAdapter
    lateinit var dialogMain: Dialog
    var IsSelectFromMeterList = false
    lateinit var cbSaveMeter: CheckBox
    lateinit var posSpinner: Spinner

    //ANIMATION
    lateinit var slide_up: Animation
    lateinit var slide_down: Animation
    var posId =""
    var balance = ""

    //INTERFACE COUNTS
    var countInterface: NotificationCount? = null
    var posList = ArrayList<PosResultModel.Result>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        //val view = inflater.inflate(R.layout.layout_service_menu, container, false)

        try {
            countInterface = activity as? NotificationCount
        } catch (e: ClassCastException) {
            throw ClassCastException(activity.toString()
                    + " must implement MyInterface ");
        }
        findviews(view)

        getData();

        return view
    }

    private fun getData(){
        try {
            val bundle = this.arguments;
             data= bundle!!.getSerializable("data") as MeterListResults;
             ShowPayLayout();

            autoCompleteTV.setText(data!!.number);
            selectedMeterID=data!!.meterId;

            showListmeterIV.setOnClickListener(null)

        }catch (exception:Exception){

        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvPosNumber.setText("POS ID : " + SharedHelper.getString(activity!!, Constants.POS_NUMBER))
    }


    fun findviews(view: View) {

        slide_down = AnimationUtils.loadAnimation(activity, R.anim.slide_down)
        slide_up = AnimationUtils.loadAnimation(activity, R.anim.slide_up)
        contactVendtechTV = view.findViewById<View>(R.id.contactVendtechTV) as TextView
        errorDashTV = view.findViewById<View>(R.id.errorDashTV) as TextView
        errorAccountService = view.findViewById<View>(R.id.errorAccountService) as LinearLayout

        totalBalanceTV = view.findViewById<View>(R.id.totalBalanceTV) as TextView
        tickerViewBalance = view.findViewById<View>(R.id.tickerView) as TickerView
        servicesRecyclerview = view.findViewById<View>(R.id.servicesRecyclerview) as RecyclerView
        tickerViewBalance.setCharacterLists(TickerUtils.provideNumberList());
        val font = Typeface.createFromAsset(requireContext().assets, "fonts/roboto_bold.ttf")
        tickerViewBalance.typeface = font

        payBillTV = view.findViewById<View>(R.id.paynowTV) as TextView
        cbSaveMeter = view.findViewById(R.id.cbSaveMeter) as CheckBox
        moneyET = view.findViewById<View>(R.id.moneypayET) as EditText
        fabBack = view.findViewById<View>(R.id.fabBack) as FloatingActionButton
        confirmFabBack = view.findViewById<View>(R.id.confirmFabBack) as FloatingActionButton
        serviceLayout = view.findViewById<View>(R.id.serviceLayout) as RelativeLayout
        payLayout = view.findViewById<View>(R.id.payLayout) as RelativeLayout
        autoCompleteTV = view.findViewById<View>(R.id.autoCompleteTextView) as AutoCompleteTextView
        showListmeterIV = view.findViewById<View>(R.id.showListmeterIV) as ImageView
        posSpinner = view.findViewById<View>(R.id.posIdSpinner) as Spinner
        confirmPayCancel = view.findViewById<View>(R.id.confirmPayCancel) as TextView
        confirmPayPayBtn = view.findViewById<View>(R.id.confirmPayPayBtn) as TextView

        payBillTV.setOnClickListener(this)
        fabBack.setOnClickListener(this)
        confirmFabBack.setOnClickListener(this)
        confirmPayCancel.setOnClickListener(this)
        confirmPayPayBtn.setOnClickListener(this)
        showListmeterIV.setOnClickListener(this)

        payLayout.visibility = View.GONE


       /* autoCompleteTV.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                if (!IsSelectFromMeterList) {
                    selectedMeterID = ""
                    IsSelectFromMeterList = false
                } else {
                    IsSelectFromMeterList = false
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }
        })*/


        moneyET.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                moneyET.removeTextChangedListener(this)
                try {
                    var originalString = s.toString()

                    var longval: Long
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "")
                    }
                    longval = originalString.toLong()
                    var waletBalCrnt = tickerViewBalance.text.toString()
                    if (waletBalCrnt.contains(","))
                        waletBalCrnt = waletBalCrnt.replace(",", "")
                    if (longval <= waletBalCrnt.toLong()) {
                        var formatter: DecimalFormat = NumberFormat.getInstance(Locale.US) as DecimalFormat
                        formatter.applyPattern("#,###,###,###")
                        var formattedString = formatter.format(longval)
                        moneyET.setText(formattedString);
                        moneyET.setSelection(moneyET.text.length)
                    } else {
                        Toast.makeText(requireContext(), "Amount is greater then Wallet Balance", Toast.LENGTH_SHORT).show()
                        var getEnterValue = moneyET.text.toString()
                        var op: String = getEnterValue.dropLast(1)
                        moneyET.setText("" + op)
                        moneyET.setSelection(moneyET.text.length)
                    }
                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                }
                moneyET.addTextChangedListener(this)
            }
        })


        contactVendtechTV.setOnClickListener(View.OnClickListener {

            var intent = Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+232 79 990 990"));
            startActivity(intent);
        })


        //Check whether user account status is Activie or Pending
        if (SharedHelper.getString(requireContext(), Constants.USER_ACCOUNT_STATUS).equals(Constants.STATUS_ACTIVE)) {
            GetAssignedService()
        } else {
            ErrorAccountApproval()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }


    fun GetWalletBalance() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()

        val call: Call<GetWalletModel> = Uten.FetchServerData().get_wallet_balance(SharedHelper.getString(requireActivity(), Constants.TOKEN))
        call.enqueue(object : Callback<GetWalletModel> {
            override fun onResponse(call: Call<GetWalletModel>, response: Response<GetWalletModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {

                    if (data.status.equals("true")) {

                        totalBalanceTV.setText("SLL : " + data.result.balance)
                        tickerViewBalance.setText("SLL : " + data.result.balance)
                        tickerViewBalance.setText(NumberFormat.getNumberInstance(Locale.US).format(data.result.balance.toDouble().toInt()))
                        //tickerViewBalance.setText(Utilities.formatCurrencyValue(data.result.balance))
                        totalAvlblBalance = data.result.balance.toDouble()
                        countInterface?.CountIs(data.result.unReadNotifications)


                    } else {
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())
                    }
                }
            }

            override fun onFailure(call: Call<GetWalletModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

        })
    }


    override fun onClick(v: View) {


        when (v.id) {

            R.id.paynowTV -> {

                var moneyValue = moneyET.text.toString().trim().replace(",", "")

                if (TextUtils.isEmpty(autoCompleteTV.text.toString().trim())) {
                    Utilities.shortToast("Please select a meter number.", requireActivity())
                } else if (autoCompleteTV.text.toString().length < 11) {
                    Utilities.shortToast("Please enter correct meter number.", requireActivity())
                }
                /*else if (TextUtils.isEmpty(selectedMeterID)) {
                    Utilities.shortToast("Please select a correct meter number.", requireActivity())
                } */ else if (TextUtils.isEmpty(moneyET.text.toString().trim())) {
                    Utilities.shortToast("Please enter recharge amount.", requireActivity())
                } else if (totalAvlblBalance < 1) {
                    Utilities.shortToast("You don't have enough balance to recharge", requireActivity())
                } else if (moneyValue.toDouble() > totalAvlblBalance) {
                    Utilities.shortToast("Recharge amount is greater than the available balance.", requireActivity())
                } else {
                    //  ShowAlertForRecharge(moneyValue)  //show alertPopUp
                    showPayCoinfrmLayout()
                }
            }


            R.id.fabBack -> {
                HidePayLayout()
            }

            R.id.confirmFabBack -> {
                HideConfirmLayout()
            }
            R.id.confirmPayCancel->{
                HideConfirmLayout()
            } R.id.confirmPayPayBtn->{
               // startActivity(Intent(activity, PrintScreenActivity::class.java))
                HideConfirmLayout();
                if (Uten.isInternetAvailable(requireActivity())){
                    DoRecharge(confirmPayAmtValue.text.toString().trim().replace(",", ""), selectedMeterID, posId)
                } else {
                    Utilities.shortToast("No internet connection. Please check your network connectivity.", requireActivity())
                }
            }

            /*   R.id.waterLL->{
                   Utilities.shortToast("This service will be available soon.",requireActivity())
               }

               R.id.gasLL->{
                   Utilities.shortToast("This service will be available soon.",requireActivity())
               }*/

            R.id.electricityLL -> {
                GetMeterList()
            }

            R.id.showListmeterIV -> {
                showMeterListDialog(meterListModels)
            }
        }
    }

    private fun showPayCoinfrmLayout() {

        confirmPayPosID.text="POS ID - "+ posSpinner.selectedItem.toString()
        confirmPayMeterValue.text=autoCompleteTV.text.toString()


         confirmPayAmtValue.text=moneyET.text.toString()

        autoCompleteTV.setText("")
        moneyET.setText("")



        if (payLayout.visibility == View.VISIBLE) {
            payLayout.startAnimation(slide_down)
        }
        payLayout.visibility = View.GONE

        if (payConfirm.visibility == View.GONE) {
            payConfirm.startAnimation(slide_up)
        }
        payConfirm.visibility = VISIBLE


    }

    companion object {

        fun newInstance(): DashboardFragment {
            return DashboardFragment()
        }
    }

    override fun clickedServiceId(platformId: String) {

        if (platformId.contentEquals("1")) {
            GetMeterList()
            GetPosIdList()
        }

    }


    override fun onResume() {

        if (Uten.isInternetAvailable(requireActivity())) {
             GetWalletBalance();
            if (payLayout.visibility == View.VISIBLE) {
                GetMeterList()
                GetPosIdList()
            }
        } else {
            NoInternetDialog("No internet connection. Please check your network connectivity.")
        }
        super.onResume()
    }


   /* fun ShowAlertForRecharge(moneyvalue: String) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.app_name)
        builder.setMessage("Are you sure to recharge this meter?")
        builder.setIcon(R.drawable.appicon)

        builder.setPositiveButton("Recharge") { dialogInterface, which ->

            if (Uten.isInternetAvailable(requireActivity())) {
                DoRecharge(moneyvalue, selectedMeterID, posId)
            } else {
                Utilities.shortToast("No internet connection. Please check your network connectivity.", requireActivity())
            }

        }
        builder.setNegativeButton("Check Again") { dialogInterface, which ->
        }
        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()
    }*/

    fun ShowPayLayout() {

        if (serviceLayout.visibility == View.VISIBLE) {
            serviceLayout.startAnimation(slide_down)
        }
        serviceLayout.visibility = View.GONE

        if (payLayout.visibility == View.GONE) {
            payLayout.startAnimation(slide_up)
        }
        payLayout.visibility = View.VISIBLE

    }


    fun HidePayLayout() {

        if (payLayout.visibility == View.VISIBLE) {
            payLayout.startAnimation(slide_down)
        }
        payLayout.visibility = View.GONE
        autoCompleteTV.setText("")
        moneyET.setText("")

        if (serviceLayout.visibility == View.GONE) {
            serviceLayout.startAnimation(slide_up)
        }
        serviceLayout.visibility = View.VISIBLE

    }

    fun HideConfirmLayout() {
        if (payConfirm.visibility == View.VISIBLE) {
            payConfirm.startAnimation(slide_down);
        }
        payConfirm.visibility = View.GONE;
        //showPayLayout
        ShowPayLayout();
//        if (serviceLayout.visibility == View.GONE) {
//            serviceLayout.startAnimation(slide_up)
//        }
//        serviceLayout.visibility = View.VISIBLE

    }

    fun GetPosIdList() {
        val call = Uten.FetchServerData().getPosList(SharedHelper.getString(requireActivity(), Constants.TOKEN))
        call.enqueue(object : Callback<PosResultModel> {
            override fun onFailure(call: Call<PosResultModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                Utilities.shortToast("Something went wrong", requireActivity())
            }

            override fun onResponse(call: Call<PosResultModel>, response: Response<PosResultModel>) {
                if (response != null) {
                    if (response?.body() != null) {
                        if (response?.body()?.status == "true") {
                            if (response.body()?.result?.size!! > 0) {
                                SetOnSpinner(response.body()?.result!!)
                            }
                        }
                    }
                }
            }

        })
    }

    private fun SetOnSpinner(result: List<PosResultModel.Result>) {
        posList.clear();
        posList.addAll(result)
        val list = ArrayList<String>()
        result.forEach {
            list.add(it.serialNumber)
        }

        if (activity!=null) {
            val adapter = ArrayAdapter<String>(context, R.layout.item_pos_large, list)
            adapter.setDropDownViewResource(R.layout.sppiner_layout_item)
            posSpinner.adapter = adapter
            posSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {

                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    balance = posList.get(p2).balance;
                    posId = posList.get(p2).posId.toString();
                    Log.e("balance+pos", "$balance $posId");
                    tvPosNumber.setText("POS ID : " + posList.get(p2).serialNumber);
                    tickerViewBalance.setText(balance);
                }
            }
        }
    }

    fun GetMeterList() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()
        var vv = (SharedHelper.getString(requireActivity(), Constants.TOKEN))
        val call: Call<GetMetersModel> = Uten.FetchServerData().get_meters(SharedHelper.getString(requireActivity(), Constants.TOKEN), "1", "50")
        call.enqueue(object : Callback<GetMetersModel> {

            override fun onResponse(call: Call<GetMetersModel>, response: Response<GetMetersModel>) {
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if (data.result.size > 0) {
                            ShowPayLayout()
                            //   meterListModels.clear()
                            if (meterListModels.size > 0) {
                                meterListModels.clear()
                            }
                            meterListModels.addAll(data.result);
                            //SetAutoCompleteData();
                        } else {
                            Utilities.shortToast("No meter found", requireActivity());
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())
                    }
                }
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onFailure(call: Call<GetMetersModel>, t: Throwable) {

                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong", requireActivity())
            }


        })
    }


    fun DoRecharge(amount: String, meterId: String, posId: String) {


        //Toast.makeText(activity,meterId,Toast.LENGTH_LONG).show();

        var customDialog: CustomDialog;
        customDialog = CustomDialog(requireActivity());
        customDialog.show();
        var meterNumber: String? = null;
        var meterid: String? = null;
        if (meterId.isEmpty()) {
            meterNumber = autoCompleteTV.text.toString().trim()
            meterid = null
        } else {
            meterid = meterId
            meterNumber = null
        }
        //val call: Call<RechargeMeterModel> = Uten.FetchServerData().rechargeMeter(SharedHelper.getString(requireActivity(), Constants.TOKEN), meterid, amount, meterNumber, posId,SharedHelper.getString(activity!!, Constants.PASS_CODE_VALUE))
        val call: Call<RechargeMeterModel> = Uten.FetchServerData().rechargeMeter(SharedHelper.getString(requireActivity(), Constants.TOKEN),amount,meterId,posId)
        call.enqueue(object : Callback<RechargeMeterModel> {
            override fun onResponse(call: Call<RechargeMeterModel>, response: Response<RechargeMeterModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss();
                }

                var data = response.body();
                if (data!=null){
                    if (data.message !=null) {
                        Utilities.shortToast(data.message, activity!!);
                    }else {
                        if (data.status.equals("true")) {
                            if (data!=null){
                                serviceLayout.visibility= VISIBLE;
                                payLayout.visibility= GONE;
                            }
                            var intent = Intent(activity!!, PrintScreenActivity::class.java);
                            intent.putExtra("data", response.body());
                            startActivityForResult(intent,Constants.REQUEST_CODE);

                        } else {
                            Utilities.CheckSessionValid(data.message, activity!!, activity!!);
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RechargeMeterModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong", requireActivity())
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==Constants.REQUEST_CODE){
            if (resultCode== Activity.RESULT_OK){
                ShowPayLayout();
            }
        }

        //Toast.makeText(activity, "OnactivityResult", Toast.LENGTH_SHORT).show();
    }

    fun SetAutoCompleteData() {

        val adapter = MeterListAutoCompleteAdapter(requireActivity(), android.R.layout.simple_list_item_1, meterListModels)
        autoCompleteTV.setAdapter(adapter)
        autoCompleteTV.threshold = 1
        autoCompleteTV.setOnItemClickListener() { parent, _, position, id ->
            val selectedPoi = parent.adapter.getItem(position) as MeterListResults?
            // autoCompleteTV.setText(selectedPoi?.number.toString())
            //selectedMeterID = selectedPoi?.meterId.toString();

            //Toast.makeText(activity!!,"--"+selectedMeterID,Toast.LENGTH_LONG).show();

        }
    }


    fun GetAssignedService() {


        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()

        val call: Call<UserAssignedServicesModel> = Uten.FetchServerData().user_assigned_services(SharedHelper.getString(requireActivity(), Constants.TOKEN))
        call.enqueue(object : Callback<UserAssignedServicesModel> {
            override fun onResponse(call: Call<UserAssignedServicesModel>, response: Response<UserAssignedServicesModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        Log.v("AssignedServices", Gson().toJson(data.result))
                        if (data.result.size > 0) {
                            servicesRecyclerview.visibility = View.VISIBLE
                            errorAccountService.visibility = View.GONE
                            UpdateServiceAdapter(data.result)
                        } else {
                            ErrorServiceAssigned()
                        }
                    }else{
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())
                    }
                }
            }

            override fun onFailure(call: Call<UserAssignedServicesModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong", requireActivity())
            }
        })
    }

    fun UpdateServiceAdapter(data: MutableList<UserServicesResult>) {

        servicesAdapter = UserServicesAdapter(context!!, data, this)
        servicesRecyclerview.adapter = servicesAdapter
        servicesRecyclerview.layoutManager = GridLayoutManager(requireContext(), 2)
        servicesRecyclerview.setHasFixedSize(true)
        servicesAdapter.notifyDataSetChanged()


    }

    private fun NoInternetDialog(msg: String) {

        val dialog = AlertDialog.Builder(requireActivity())
        dialog.setMessage(msg)
                .setPositiveButton("OK") { paramDialogInterface, paramInt ->
                    //  permissionsclass.requestPermission(type,code);
                    startActivity(Intent(Settings.ACTION_SETTINGS))
                }
        dialog.setCancelable(false)
        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this);
    }

    // UI updates must run on MainThread
    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onEvent(event: MessageEvent) {
        if (event.message.equals("update_balance")) {
            GetWalletBalance()
        }
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this);
    }

    fun ErrorServiceAssigned() {

        servicesRecyclerview.visibility = View.GONE
        errorDashTV.text = "Status: ${resources.getString(R.string.service_not_assigned)}"
        errorAccountService.visibility = View.VISIBLE
    }


    fun ErrorAccountApproval() {

        servicesRecyclerview.visibility = View.GONE
        errorDashTV.text = "Status: ${resources.getString(R.string.account_under_approval)}"
        errorAccountService.visibility = View.VISIBLE
    }

    private fun showMeterListDialog(list: MutableList<MeterListResults>) {

        val dialog = Dialog(activity)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.dialog_select_meter)
        val cancel = dialog.findViewById(R.id.cancelDialog) as AppCompatTextView
        val recyclerview = dialog.findViewById(R.id.recyclerviewMeter) as RecyclerView
        val mLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
        meterDialogAdapter = MeterListDialogAdapter(list, requireContext(), this)
        recyclerview.adapter = meterDialogAdapter
        recyclerview.layoutManager = mLayoutManager
        recyclerview.setHasFixedSize(true)
        meterDialogAdapter.notifyDataSetChanged()

        dialogMain = dialog
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()

    }


    override fun meterId(id: String, name: String) {


        IsSelectFromMeterList = true
        selectedMeterID = id
        autoCompleteTV.setText(name)
        dialogMain.dismiss()



    }

    interface NotificationCount {

        fun CountIs(count: String)
    }

}
