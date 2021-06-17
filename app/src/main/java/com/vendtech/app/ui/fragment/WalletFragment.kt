package com.vendtech.app.ui.fragment

import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.RecyclerView
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.vendtech.app.R
import com.vendtech.app.adapter.transactions.DepositTransactionAdapter
import com.vendtech.app.adapter.transactions.RechargeTransactionAdapter
import com.vendtech.app.adapter.wallet.AccountAdapter
import com.vendtech.app.helper.SharedHelper
import com.vendtech.app.models.meter.BankResponseModel
import com.vendtech.app.models.meter.PosResultModel
import com.vendtech.app.models.transaction.*
import com.vendtech.app.network.Uten
import com.vendtech.app.utils.Constants
import com.vendtech.app.utils.CustomDialog
import com.vendtech.app.utils.Utilities
import kotlinx.android.synthetic.main.fragment_wallet.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.DecimalFormat
import java.text.NumberFormat
import java.util.*
import kotlin.collections.ArrayList


class WalletFragment : Fragment(), View.OnClickListener {


    lateinit var addBalanceTV: TextView
    lateinit var tranhistoryTV: TextView
    internal var isFirstLaunch = true


    //ADD BALANCE LAYOUT
    lateinit var addBalanceLayout: ScrollView


    //TRANSACTION HISTORY LAYOUT
    lateinit var transactionHistoryLayout: LinearLayout
    lateinit var fragment_frame: FrameLayout
    lateinit var depositText: TextView
    lateinit var linedeposit: View
    lateinit var depositTRL: RelativeLayout
    lateinit var rechargeText: TextView
    lateinit var linerecharge: View
    lateinit var rechargeTRL: RelativeLayout


    //ANIMATION
    lateinit var slide_in: Animation
    lateinit var slide_out: Animation


    //ADD DEPOSIT LAYOUT
    lateinit var spAccounts: Spinner
    lateinit var typeSpinner: Spinner
    lateinit var bankNameSpinner: Spinner
    lateinit var spPosId: Spinner
    lateinit var selectPaytype: RelativeLayout
    var transactionMode = 1

    lateinit var sendNowTV: TextView

    // lateinit var vendornameET: EditText
    lateinit var chxslipET: EditText
    lateinit var depositamountET: EditText
    lateinit var commentET: EditText
    lateinit var plusPercentET: TextView
    lateinit var banknameTV: TextView
    lateinit var accnameTV: TextView
    lateinit var accnumberTV: TextView
    lateinit var accbbanTV: TextView

    // lateinit var commissionLL: LinearLayout
    // lateinit var tvCommisionPercentage: TextView
    lateinit var tvPosNumber: TextView
    lateinit var tvWalletBalance: TextView
    lateinit var commissionPercent: TextView
    lateinit var chequeLayout: LinearLayout

    //  lateinit var bankName: EditText
    lateinit var chequeName: EditText

    //RECHARGE TRANSACTION AND DEPOSIT TRANSACTION LAYOUTS
    var bankName = ""
    lateinit var recyclerviewRecharge: RecyclerView
    lateinit var nodataRecharge: TextView
    lateinit var recyclerviewDeposit: RecyclerView
    lateinit var nodataDeposit: TextView
    internal var rechargeListModel: MutableList<RechargeTransactionNewListModel.Result> = java.util.ArrayList()
    internal var depositListModel: MutableList<DepositTransactionNewListModel.Result> = java.util.ArrayList()
    lateinit var rechargetransAdapter: RechargeTransactionAdapter
    lateinit var deposittransAdapter: DepositTransactionAdapter
    var pageRecharge = 1
    var pageDeposit = 1
    var totalItemsNo = 15
    var percentage = 0.0
    var posId = 0
    var bankAccountId = "0"

    //ANIMATION
    lateinit var slide_up: Animation
    lateinit var slide_down: Animation


    //Pagination recharges
    var loadings_r = true
    var pastVisiblesItems_r = 0
    var visibleItemCount_r = 0
    var totalItemCount_r = 0


    //Pagination deposit
    var loadings_d = true
    var pastVisiblesItems_d = 0
    var visibleItemCount_d = 0
    var totalItemCount_d = 0
    var posList = ArrayList<PosResultModel.Result>()
    var depositType = ""
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_wallet, container, false)

        findviews(view)
        SetDepositLayout()
        GetBankDetails()
        GetPosIdList()
        GetBankNames()
        //  GetBankDetails()

        return view
    }

    private fun GetBankNames() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()
        val call = Uten.FetchServerData().getBankNames(SharedHelper.getString(requireActivity(), Constants.TOKEN))
        call.enqueue(object : Callback<BankNameModel> {
            override fun onFailure(call: Call<BankNameModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onResponse(call: Call<BankNameModel>, response: Response<BankNameModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if (data.result.size > 0) {
                            setBankSpinner(data.result)
                        }

                    }
                }

            }

            private fun setBankSpinner(result: List<BankNameModel.Result>) {
                val bank = ArrayList<String>()
                result.forEach {
                    bank.add(it.text)
                }
                val adapter = ArrayAdapter<String>(requireActivity(), R.layout.spinner_text_second, bank)
                adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
                bankNameSpinner.adapter = adapter
                bankNameSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onNothingSelected(p0: AdapterView<*>?) {

                    }

                    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                        bankName = result.get(p2).value
                    }

                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        tvPosNumber.text = SharedHelper.getString(activity!!, Constants.POS_NUMBER)
        val commissionPercent: String? = SharedHelper.getString(activity!!, Constants.COMMISSION_PERCENTAGE)
        /*  if ((commissionPercent?.toDouble() ?: 0.0) > 0) {
              commissionLL.visibility = View.VISIBLE
              tvCommisionPercentage.text = commissionPercent
          } else {
              commissionLL.visibility = View.GONE
          }*/

    }


    fun findviews(view: View) {

        spAccounts = view.findViewById(R.id.spAccounts) as Spinner
        spPosId = view.findViewById(R.id.spPosId) as Spinner
        bankNameSpinner = view.findViewById(R.id.bankNameSpinner) as Spinner
        // addBalanceTV = view.findViewById<View>(R.id.addBalanceTV) as TextView
//        tranhistoryTV = view.findViewById<View>(R.id.tranhistoryTV) as TextView
        chequeLayout = view.findViewById(R.id.chequeLayout)
        chequeName = view.findViewById(R.id.chequeNameET)
        // bankName=view.findViewById(R.id.bankNameET)
        slide_down = AnimationUtils.loadAnimation(activity, R.anim.slide_down)
        slide_up = AnimationUtils.loadAnimation(activity, R.anim.slide_up)
        tvWalletBalance = view.findViewById(R.id.walletBalance);
        commissionPercent = view.findViewById(R.id.commissionPercentTV)

        //ADD BALANCE LAYOUT
        addBalanceLayout = view.findViewById<View>(R.id.layoutAddBalance) as ScrollView

        //TRANSACTION HISTORY LAYOUT
        transactionHistoryLayout = view.findViewById<View>(R.id.layoutTransactionHistory) as LinearLayout
        depositText = view.findViewById<View>(R.id.depositText) as TextView
        rechargeText = view.findViewById<View>(R.id.rechargeText) as TextView
        linedeposit = view.findViewById<View>(R.id.linedeposit) as View
        linerecharge = view.findViewById<View>(R.id.linerecharge) as View
        depositTRL = view.findViewById<View>(R.id.deposItTRL) as RelativeLayout
        rechargeTRL = view.findViewById<View>(R.id.rechargeTRL) as RelativeLayout
        rechargeTRL.setOnClickListener(this)
        depositTRL.setOnClickListener(this)

        //RECHARGE TRANSACTION AND DEPOSIT TRANSACTION LAYOUT
        recyclerviewDeposit = view.findViewById(R.id.recyclerviewDepositss)
        recyclerviewRecharge = view.findViewById(R.id.recyclerviewRechargess)
        nodataDeposit = view.findViewById(R.id.nodataDeposit)
        nodataRecharge = view.findViewById(R.id.nodataRecharge)

        slide_in = AnimationUtils.loadAnimation(activity, R.anim.slide_in)
        slide_out = AnimationUtils.loadAnimation(activity, R.anim.activity_back_out)

        //Deposit layout
        typeSpinner = view.findViewById<View>(R.id.typeSpinner) as Spinner
        selectPaytype = view.findViewById<View>(R.id.selectPaytype) as RelativeLayout
//        tvPosNumber = view.findViewById(R.id.tvPosNumber) as TextView
        sendNowTV = view.findViewById<View>(R.id.sendnowTV) as TextView
        // vendornameET = view.findViewById<View>(R.id.vendornameET) as EditText
        chxslipET = view.findViewById<View>(R.id.chxslipET) as EditText
        depositamountET = view.findViewById<View>(R.id.depositamountET) as EditText
        plusPercentET = view.findViewById<View>(R.id.plusPercentET) as TextView
        commentET = view.findViewById<View>(R.id.commentET) as EditText
        //  commissionLL = view.findViewById(R.id.commissionLL) as LinearLayout
        //tvCommisionPercentage = view.findViewById(R.id.tvCommisionPercentage) as TextView
        /*  lateinit var banknameTV:TextView
    lateinit var accnameTV:TextView
    lateinit var accnumberTV:TextView
    lateinit var accbbanTV:TextView


*/
        //  banknameTV = view.findViewById<View>(R.id.banknameTV) as TextView
        //  accnameTV = view.findViewById<View>(R.id.accnameTV) as TextView
        //   accnumberTV = view.findViewById<View>(R.id.accnumberTV) as TextView
        //   accbbanTV = view.findViewById<View>(R.id.accbbanTV) as TextView

        //addBalanceTV.setOnClickListener(this)
        //tranhistoryTV.setOnClickListener(this)
        selectPaytype.setOnClickListener(this)


    }


    fun SetDepositLayout() {

        SetSpinnerData()
        SetDepositTextChangeListener()
        sendNowTV.setOnClickListener(View.OnClickListener {
            /*  if (TextUtils.isEmpty(vendornameET.text.toString().trim())) {
                  Utilities.shortToast("Enter vendor name", requireActivity())
              } else */
            if (transactionMode == 2) {
                /* if(TextUtils.isEmpty(bankName.text.toString())){
                     Utilities.shortToast("Enter your bank name", requireActivity())
                 }
                 else*/ if (TextUtils.isEmpty(chequeName.text.toString())) {
                    Utilities.shortToast("Enter the name on Cheque", requireActivity())

                }

            }
            if (TextUtils.isEmpty(chxslipET.text.toString().trim())) {
                if (transactionMode == 1) {
                    Utilities.shortToast("Enter slip id", requireActivity())
                } else {
                    Utilities.shortToast("Enter cheque id", requireActivity())
                }
            } else if (TextUtils.isEmpty(depositamountET.text.toString().trim())) {
                Utilities.shortToast("Enter deposit amount", requireActivity())
            } /*else if (TextUtils.isEmpty(commentET.text.toString().trim())) {
                Utilities.shortToast("Please type some comment", requireActivity())
            }*/ else {
                DoDeposit()
            }
        })

    }

    private fun SetDepositTextChangeListener() {
        depositamountET.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                depositamountET.removeTextChangedListener(this);

                try {
                    var originalString = s.toString();
                    var plusPercent = s.toString()
                    var longval: Long;
                    if (originalString.contains(",")) {
                        originalString = originalString.replace(",", "");
                    }
                    longval = originalString.toLong()
                    var formatter = NumberFormat.getNumberInstance(Locale.US);
                    // formatter.applyPattern("#,###,###,###");
                    var formattedString = formatter.format(longval);
                    depositamountET.setText(formattedString);
                    depositamountET.setSelection(depositamountET.text.length);
                    if (s.toString().length > 0) {
                        if (plusPercentET.text.toString().contains(",")) {
                            plusPercent = plusPercent.replace(",", "")
                        }
                        plusPercentET.setText(getPercentage(plusPercent?.toLong()!!))
                    } else plusPercentET.setText("")

                } catch (nfe: NumberFormatException) {
                    nfe.printStackTrace();
                    plusPercentET.setText("")

                }
                depositamountET.addTextChangedListener(this);
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
/*
if(p0?.length!!>0){
    NumberFormat.getNumberInstance(Locale.US).format(p0?.toString().toInt())
    plusPercentET.setText(getPercentage(p0?.toString()?.toInt()))
}
                else plusPercentET.setText("")
            }
*/
            }

        })
    }

    private fun getPercentage(p0: Long): String {
        val percent = (p0 * percentage) / 100

        Log.e("percent", percent.toString())
        val number = p0 + percent.toLong()

        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }


    fun DoDeposit() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()
        var bankname: String? = null
        var nameOnCheque: String? = null
        if (transactionMode == 1) {
            depositType = "Cash"
            bankname = null
            nameOnCheque = null
        } else {
            depositType = "Cheque"
            //  bankname=bankName.text.toString()
            nameOnCheque = chequeName.text.toString()
        }
        var depositAmount = ""
        var plusPercentAmount = ""
        if (depositamountET.text.toString().contains(",")) {
            depositAmount = depositamountET.text.toString().replace(",", "");
        } else {
            depositAmount = depositamountET.text.toString()
        }
        if (plusPercentET.text.toString().contains(",")) {
            plusPercentAmount = plusPercentET.text.toString().replace(",", "");
        } else {
            plusPercentAmount = plusPercentET.text.toString().trim()
        }
        val call: Call<DepositRequestModel> = Uten.FetchServerData().deposit_request(SharedHelper.getString(requireActivity(),
                Constants.TOKEN),
                posId,
                bankAccountId,
                depositType,
                chxslipET.text.toString().trim(),
                bankname,
                nameOnCheque,
                depositAmount,
                plusPercentAmount)
        call.enqueue(object : Callback<DepositRequestModel> {
            override fun onResponse(call: Call<DepositRequestModel>, response: Response<DepositRequestModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    Utilities.shortToast(data.message, requireActivity())
                    if (data.status.equals("true")) {
                        ResetLayoutAddDeposit()
                    } else {
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())

                    }
                }
            }

            override fun onFailure(call: Call<DepositRequestModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

        })
    }

    fun bindAccountSp(list: List<BankResponseModel.Result>) {
        val accountAdapter = AccountAdapter(activity!!, list as MutableList<BankResponseModel.Result>)
        spAccounts.adapter = accountAdapter
        spAccounts.setOnItemSelectedListener(object : OnItemSelectedListener {
            override fun onItemSelected(adapterView: AdapterView<*>, view: View,
                                        position: Int, id: Long) {
                val item = adapterView.getItemAtPosition(position) as BankDetailResult
                if (item != null) {
                    bindAccountDetails(item);
                }
            }

            override fun onNothingSelected(adapterView: AdapterView<*>?) {

            }
        })
    }

    fun bindAccountDetails(bankDetails: BankDetailResult?) {
        banknameTV.text = bankDetails?.bankName
        accnameTV.text = bankDetails?.accountName
        accnumberTV.text = bankDetails?.accountNumber
        accbbanTV.text = bankDetails?.bban
    }

    fun ResetLayoutAddDeposit() {

        vendornameET.setText("")
        chxslipET.setText("")
        depositamountET.setText("")
        //  bankName.setText("")
        chequeName.setText("")
        // commentET.setText("")
        SetSpinnerData()

    }


    fun SetSpinnerData() {

        val list: MutableList<String> = ArrayList()
        list.add("Cash")
        list.add("Cheque")

        val adapte = ArrayAdapter<String>(requireActivity(), R.layout.spinner_text_second, list)
        adapte.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        typeSpinner.setAdapter(adapte)

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                //Toast.makeText(this@SignUpActivity, "Country ID: " + data[position].countryId, Toast.LENGTH_SHORT).show()
                if (position == 0) {
                    transactionMode = 1
                    chequeLayout.visibility = View.GONE
                } else {
                    transactionMode = 2
                    chequeLayout.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    fun SelectAddBalance() {

        addBalanceTV.setTextColor(resources.getColor(R.color.colorblack))
        addBalanceTV.background = resources.getDrawable(R.drawable.yellow_chooser_left)
        tranhistoryTV.setTextColor(resources.getColor(R.color.colorlightgrey))
        tranhistoryTV.background = resources.getDrawable(R.drawable.grey_chooser_right)


        if (transactionHistoryLayout.visibility == View.VISIBLE) {
            transactionHistoryLayout.startAnimation(slide_out)
        }
        transactionHistoryLayout.visibility = View.GONE


        if (addBalanceLayout.visibility == View.GONE) {
            addBalanceLayout.startAnimation(slide_in)
        }
        addBalanceLayout.visibility = View.VISIBLE


        //   addBalanceLayout.setVisibility(View.VISIBLE);
        //  transactionHistoryLayout.setVisibility(View.GONE);


    }

    fun SelectTransactionHistory() {
        recyclerviewRecharge.visibility = View.GONE
        recyclerviewDeposit.visibility = View.GONE
        nodataDeposit.visibility = View.GONE
        nodataRecharge.visibility = View.GONE

        addBalanceTV.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorlightgrey))
        addBalanceTV.background = ContextCompat.getDrawable(requireActivity(), R.drawable.grey_chooser_left)
        tranhistoryTV.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorblack))
        tranhistoryTV.background = ContextCompat.getDrawable(requireActivity(), R.drawable.yellow_chooser_right)

        if (addBalanceLayout.visibility == View.VISIBLE) {
            addBalanceLayout.startAnimation(slide_out)
        }
        addBalanceLayout.visibility = View.GONE



        if (transactionHistoryLayout.visibility == View.GONE) {
            transactionHistoryLayout.startAnimation(slide_in)
        }
        transactionHistoryLayout.visibility = View.VISIBLE


        Handler().postDelayed({
            SelectRechargeTrans()
        }, 500)


        // addBalanceLayout.setVisibility(View.GONE);
        // transactionHistoryLayout.setVisibility(View.VISIBLE);

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
        posList.addAll(result)
        val list = ArrayList<String>()
        result.forEach {
            list.add(it.serialNumber)
        }
        val adapter = ArrayAdapter<String>(requireActivity(), R.layout.item_pos, list)
        adapter.setDropDownViewResource(R.layout.sppiner_layout_item)
        spPosId.adapter = adapter
        spPosId.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                walletBalance.setText(posList.get(p2).balance)
                posId = posList.get(p2).posId
                percentage = posList.get(p2).percentage
                commissionPercent.setText("PLUS ${posList.get(p2).percentage}%")

            }

        }
    }

    override fun onClick(v: View) {


        when (v.id) {

            R.id.addBalanceTV ->
                SelectAddBalance()

            R.id.tranhistoryTV ->
                SelectTransactionHistory()

            R.id.selectPaytype ->
                typeSpinner.performClick()

            R.id.rechargeTRL ->
                SelectRechargeTrans()

            R.id.deposItTRL ->
                SelectDepositTrans()
        }
    }

    companion object {


        fun newInstance(): WalletFragment {
            return WalletFragment()
        }
    }


    fun SelectRechargeTrans() {

        pageDeposit = 1
        linerecharge.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorYellow))
        rechargeText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorYellow))
        linedeposit.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorWhite))
        depositText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorWhite))

        LoadRechargeTransactionFragment()


    }


    fun SelectDepositTrans() {

        pageRecharge = 1
        linerecharge.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorWhite))
        rechargeText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorWhite))
        linedeposit.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.colorYellow))
        depositText.setTextColor(ContextCompat.getColor(requireActivity(), R.color.colorYellow))


        LoadDepositTransactionFragment()
    }


    fun LoadRechargeTransactionFragment() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()

        val call: Call<RechargeTransactionNewListModel> = Uten.FetchServerData().get_meter_recharges(SharedHelper.getString(requireActivity(), Constants.TOKEN), pageRecharge.toString(), totalItemsNo.toString())
        call.enqueue(object : Callback<RechargeTransactionNewListModel> {
            override fun onResponse(call: Call<RechargeTransactionNewListModel>, response: Response<RechargeTransactionNewListModel>) {


                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {

                        if (data.result.size > 0) {

                            if (data.result.size < totalItemsNo) {
                                loadings_r = false
                            } else {
                                loadings_r = true
                            }

                            if (pageRecharge == 1) {
                                rechargeListModel.clear()
                                rechargeListModel.addAll(data.result)
                            } else {
                                rechargeListModel.addAll(data.result)
                            }

                            if (pageRecharge == 1) {
                                ShowRechargeTransactionFlow()
                            } else {
                                rechargetransAdapter.notifyDataSetChanged()
                            }

                        } else {


                            if (rechargeListModel.size < 1) {
                                nodataDeposit.visibility = View.GONE
                                nodataRecharge.visibility = View.VISIBLE
                                recyclerviewDeposit.visibility = View.GONE
                                recyclerviewRecharge.visibility = View.GONE

                            }
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())

                    }
                }

            }

            override fun onFailure(call: Call<RechargeTransactionNewListModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong.", requireActivity())
            }

        })
    }


    fun LoadDepositTransactionFragment() {

        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()

        val call: Call<DepositTransactionNewListModel> = Uten.FetchServerData().get_deposits(SharedHelper.getString(requireActivity(), Constants.TOKEN), pageDeposit.toString(), totalItemsNo.toString())
        call.enqueue(object : Callback<DepositTransactionNewListModel> {
            override fun onResponse(call: Call<DepositTransactionNewListModel>, response: Response<DepositTransactionNewListModel>) {

                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                val g = Gson()
                g.toJson(response.body())

                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {


                        if (data.result.size > 0) {

                            if (data.result.size < totalItemsNo) {
                                loadings_d = false
                            } else {
                                loadings_d = true
                            }

                            if (pageDeposit == 1) {
                                depositListModel.clear()
                                depositListModel.addAll(data.result)
                            } else {
                                depositListModel.addAll(data.result)
                            }
                            if (pageDeposit == 1) {
                                ShowDepositTransactionFlow()
                            } else {
                                deposittransAdapter.notifyDataSetChanged()
                            }

                        } else {

                            if (depositListModel.size < 1) {

                                nodataDeposit.visibility = View.VISIBLE
                                nodataRecharge.visibility = View.GONE
                                recyclerviewDeposit.visibility = View.GONE
                                recyclerviewRecharge.visibility = View.GONE

                            }
                        }
                    } else {
                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())

                    }
                }
            }

            override fun onFailure(call: Call<DepositTransactionNewListModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                Utilities.shortToast("Something went wrong.", requireActivity())
            }

        })
    }


    fun ShowRechargeTransactionFlow() {


        val mLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)

        rechargetransAdapter = RechargeTransactionAdapter(rechargeListModel, requireActivity(), requireActivity())
        recyclerviewRecharge.adapter = rechargetransAdapter
        recyclerviewRecharge.layoutManager = mLayoutManager
        recyclerviewRecharge.setHasFixedSize(true)
        rechargetransAdapter.notifyDataSetChanged()

        nodataRecharge.visibility = View.GONE
        nodataDeposit.visibility = View.GONE
        recyclerviewDeposit.visibility = View.GONE
        recyclerviewRecharge.visibility = View.GONE


        if (recyclerviewRecharge.visibility == View.GONE) {
            recyclerviewRecharge.startAnimation(slide_up)
        }
        recyclerviewRecharge.visibility = View.VISIBLE



        recyclerviewRecharge.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount_r = mLayoutManager.childCount
                    totalItemCount_r = mLayoutManager.itemCount
                    pastVisiblesItems_r = mLayoutManager.findFirstVisibleItemPosition()

                    if (loadings_r) {
                        if (visibleItemCount_r + pastVisiblesItems_r >= totalItemCount_r) {
                            loadings_r = false
                            pageRecharge++
                            Log.v("WalletFragment", "-------------------------------------------Last Item Wow !--------------------")
                            //Do pagination.. i.e. fetch new data
                            LoadRechargeTransactionFragment()

                        }
                    }
                }
            }
        })


    }


    fun GetBankDetails() {
        var customDialog: CustomDialog
        customDialog = CustomDialog(requireActivity())
        customDialog.show()
        val call = Uten.FetchServerData().getBankDetail(SharedHelper.getString(requireActivity(), Constants.TOKEN))
        call.enqueue(object : Callback<BankResponseModel> {
            override fun onFailure(call: Call<BankResponseModel>, t: Throwable) {
                val gs = Gson()
                gs.toJson(t.localizedMessage)
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
            }

            override fun onResponse(call: Call<BankResponseModel>, response: Response<BankResponseModel>) {
                if (customDialog.isShowing) {
                    customDialog.dismiss()
                }
                var data = response.body()
                if (data != null) {
                    if (data.status.equals("true")) {
                        if (data.result.size > 0) {
                            setAccountSpinner(data.result)
                        }

                    }
                }

            }
        })
//        val call: Call<BankDetailsModel> = Uten.FetchServerData().bank_details(SharedHelper.getString(requireActivity(), Constants.TOKEN))
//        call.enqueue(object : Callback<BankDetailsModel> {
//            override fun onResponse(call: Call<BankDetailsModel>, response: Response<BankDetailsModel>) {
//
//                if (customDialog.isShowing) {
//                    customDialog.dismiss()
//                }
//                var data = response.body()
//                if (data != null) {
//                    //  Utilities.shortToast(data.message,requireActivity())
//                    if (data.status.equals("true")) {
//
//                        /* lateinit var banknameTV:TextView
//    lateinit var accnameTV:TextView
//    lateinit var accnumberTV:TextView
//    lateinit var accbbanTV:TextView
//*/
////                        if (data.result.size > 0) {
////                            banknameTV.text = data.result.get(0).bankName
////                            accnameTV.text = data.result.get(0).accountName
////                            accnumberTV.text = data.result.get(0).accountNumber
////                            accbbanTV.text = data.result.get(0).bban
////                        }
//
//                        bindAccountSp(data.result)
//
//
//                    } else {
//                        Utilities.CheckSessionValid(data.message, requireContext(), requireActivity())
//                    }
//                }
//            }
//
//            override fun onFailure(call: Call<BankDetailsModel>, t: Throwable) {
//                val gs = Gson()
//                gs.toJson(t.localizedMessage)
//                if (customDialog.isShowing) {
//                    customDialog.dismiss()
//                }
//            }
//
//        })
    }

    private fun setAccountSpinner(result: List<BankResponseModel.Result>) {
        val accountList = ArrayList<String>()
        result.forEach {
            accountList.add(it.text)
        }
        val adapter = ArrayAdapter<String>(requireActivity(), R.layout.spinner_text_second, accountList)
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown)
        spAccounts.adapter = adapter
        spAccounts.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                bankAccountId = result.get(p2).value
            }

        }

    }


    fun ShowDepositTransactionFlow() {

        val mLayoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)


        deposittransAdapter = DepositTransactionAdapter(depositListModel, requireActivity(), requireActivity())
        recyclerviewDeposit.adapter = deposittransAdapter
        recyclerviewDeposit.layoutManager = mLayoutManager
        recyclerviewDeposit.setHasFixedSize(true)
        deposittransAdapter.notifyDataSetChanged()

        nodataRecharge.visibility = View.GONE
        nodataDeposit.visibility = View.GONE
        recyclerviewDeposit.visibility = View.GONE
        recyclerviewRecharge.visibility = View.GONE


        if (recyclerviewDeposit.visibility == View.GONE) {
            recyclerviewDeposit.startAnimation(slide_up)
        }
        recyclerviewDeposit.visibility = View.VISIBLE




        recyclerviewDeposit.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0)
                //check for scroll down
                {
                    visibleItemCount_d = mLayoutManager.childCount
                    totalItemCount_d = mLayoutManager.itemCount
                    pastVisiblesItems_d = mLayoutManager.findFirstVisibleItemPosition()

                    if (loadings_d) {
                        if (visibleItemCount_d + pastVisiblesItems_d >= totalItemCount_d) {
                            loadings_d = false
                            pageDeposit++
                            Log.v("WalletFragment", "-------------------------------------------Last Item Wow !--------------------")
                            //Do pagination.. i.e. fetch new data
                            LoadDepositTransactionFragment()

                        }
                    }
                }
            }
        })

    }
}
